package com.groweasy.csvimporter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.groweasy.csvimporter.dto.CrmRecord;
import com.groweasy.csvimporter.dto.RawRecord;
import com.groweasy.csvimporter.exception.AiExtractionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sends batches of raw CSV rows to Gemini and asks it to intelligently map
 * whatever columns exist into GrowEasy's fixed CRM schema. Uses Gemini's
 * structured-output mode (responseSchema) so the model is constrained to
 * return valid JSON matching our contract, with a retry/backoff wrapper
 * for transient failures (rate limits, network blips).
 */
@Service
public class GeminiExtractionService {

    private static final Logger log = LoggerFactory.getLogger(GeminiExtractionService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model}")
    private String model;

    @Value("${gemini.api.base-url}")
    private String baseUrl;

    @Value("${app.batch.max-retries}")
    private int maxRetries;

    @Value("${app.batch.retry-backoff-ms}")
    private long retryBackoffMs;

    public GeminiExtractionService(WebClient geminiWebClient) {
        this.webClient = geminiWebClient;
    }

    /**
     * Extracts CRM records for a single batch of raw rows.
     * Returns a map keyed by the row's original index -> extracted CrmRecord.
     * Rows the AI could not confidently map at all will simply be absent
     * from the result map (caller treats missing index as "skip").
     */
    public Map<Integer, CrmRecord> extractBatch(List<RawRecord> batch) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiExtractionException(
                    "GEMINI_API_KEY is not configured on the server. Set it as an environment variable.");
        }

        String prompt = buildPrompt(batch);
        String requestBody = buildRequestBody(prompt);

        String url = baseUrl + "/" + model + ":generateContent";

        try {
            String responseJson = webClient.post()
                    .uri(url)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(retryBackoffMs))
                            .filter(this::isRetryable)
                            .doBeforeRetry(sig -> log.warn("Retrying Gemini call after error: {}", sig.failure().getMessage())))
                    .block();

            return parseGeminiResponse(responseJson, batch);

        } catch (Exception e) {
            log.error("AI extraction failed for batch after retries", e);
            throw new AiExtractionException("AI extraction failed: " + e.getMessage(), e);
        }
    }

    private boolean isRetryable(Throwable throwable) {
        // Retry on network errors / 429 rate limits / 5xx - not on 4xx client errors like bad API key
        String message = throwable.getMessage() == null ? "" : throwable.getMessage();
        return message.contains("429") || message.contains("500") || message.contains("502")
                || message.contains("503") || message.contains("timeout") || message.contains("Connection");
    }

    private String buildPrompt(List<RawRecord> batch) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                You are a data-mapping engine for a real-estate/sales CRM called GrowEasy.
                You will receive raw CSV rows exported from many different sources - Facebook
                Lead Ads, Google Ads, Excel sheets, other CRMs, marketing agencies, or manually
                built spreadsheets. Column names, order, and structure are NOT fixed and vary
                between uploads.

                Your job: map each raw row into this fixed CRM JSON schema, inferring the
                correct field even when column names are abbreviated, misspelled, in a
                different language, or ambiguous. Use context across all columns in a row
                (e.g. a column called "ph", "mobile", "contact", or "whatsapp_no" all mean
                mobile number; "lead date", "created", "date added" all mean created_at).

                CRM fields to produce for every row:
                - created_at: lead creation date/time. Must be a value that JavaScript's
                  `new Date(created_at)` can parse successfully (prefer "YYYY-MM-DD HH:mm:ss").
                  If no date exists in the row, leave blank.
                - name: the lead's full name.
                - email: the PRIMARY email only (see multi-value rule below).
                - country_code: phone country code, e.g. "+91". Infer "+91" if the numbers
                  look like Indian mobile numbers (10 digits) and no other country is implied.
                - mobile_without_country_code: the PRIMARY mobile number, digits only, without
                  the country code.
                - company: company or organization name.
                - city, state, country: location fields, only if present or confidently inferable.
                - lead_owner: the salesperson/agent/owner assigned to this lead, if present.
                - crm_status: MUST be exactly one of these values (or blank if none fit):
                  GOOD_LEAD_FOLLOW_UP, DID_NOT_CONNECT, BAD_LEAD, SALE_DONE
                - crm_note: free-text remarks, follow-up notes, extra phone numbers or extra
                  emails (see multi-value rule), and any useful info that doesn't fit elsewhere.
                - data_source: MUST be exactly one of these values (or blank if none fit
                  confidently - do NOT guess):
                  leads_on_demand, meridian_tower, eden_park, varah_swamy, sarjapur_plots
                - possession_time: property possession timeline, only for real-estate exports.
                - description: any additional descriptive text about the lead or their interest.

                Rules you MUST follow:
                1. If a row has MULTIPLE emails: put the first as "email", append the rest into
                   "crm_note" (e.g. "Additional email: x@y.com").
                2. If a row has MULTIPLE phone numbers: put the first as
                   "mobile_without_country_code", append the rest into "crm_note".
                3. If a row has NEITHER an email NOR a mobile number, you MUST still return an
                   object for it but set "skip": true and a short "skip_reason". Otherwise set
                   "skip": false.
                4. Never invent data that is not present or reasonably inferable from the row.
                   Leave a field as an empty string "" if you are not confident.
                5. crm_status and data_source must ONLY ever be one of the allowed values above,
                   or an empty string. Never output any other value for these two fields.
                6. Return EVERY row you are given, in the same order, exactly once - do not
                   merge, drop, or reorder rows (use "skip" for rows that don't qualify).

                Here are the raw rows, given as a JSON array. Each has "row_index" (which you
                must echo back unchanged) and "fields" (the original column-name -> value map
                exactly as uploaded):

                """);

        sb.append(rowsToJson(batch));
        return sb.toString();
    }

    private String rowsToJson(List<RawRecord> batch) {
        ArrayNode array = objectMapper.createArrayNode();
        for (RawRecord raw : batch) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("row_index", raw.getRowIndex());
            ObjectNode fields = objectMapper.createObjectNode();
            raw.getFields().forEach(fields::put);
            node.set("fields", fields);
            array.add(node);
        }
        return array.toString();
    }

    /**
     * Builds the Gemini generateContent request body, using responseSchema
     * to force strictly structured JSON output matching our CRM contract.
     */
    private String buildRequestBody(String prompt) {
        ObjectNode root = objectMapper.createObjectNode();

        ArrayNode contents = objectMapper.createArrayNode();
        ObjectNode content = objectMapper.createObjectNode();
        ArrayNode parts = objectMapper.createArrayNode();
        ObjectNode part = objectMapper.createObjectNode();
        part.put("text", prompt);
        parts.add(part);
        content.set("parts", parts);
        contents.add(content);
        root.set("contents", contents);

        ObjectNode generationConfig = objectMapper.createObjectNode();
        generationConfig.put("temperature", 0.1);
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.set("responseSchema", buildResponseSchema());
        root.set("generationConfig", generationConfig);

        return root.toString();
    }

    private ObjectNode buildResponseSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "ARRAY");

        ObjectNode items = objectMapper.createObjectNode();
        items.put("type", "OBJECT");

        ObjectNode props = objectMapper.createObjectNode();
        for (String field : new String[]{
                "name", "email", "country_code", "mobile_without_country_code",
                "company", "city", "state", "country", "lead_owner", "crm_status", "crm_note",
                "data_source", "possession_time", "description", "created_at", "skip_reason"
        }) {
            ObjectNode f = objectMapper.createObjectNode();
            f.put("type", "STRING");
            props.set(field, f);
        }
        ObjectNode rowIndexField = objectMapper.createObjectNode();
        rowIndexField.put("type", "INTEGER");
        props.set("row_index", rowIndexField);

        ObjectNode skipField = objectMapper.createObjectNode();
        skipField.put("type", "BOOLEAN");
        props.set("skip", skipField);

        items.set("properties", props);
        schema.set("items", items);
        return schema;
    }

    private Map<Integer, CrmRecord> parseGeminiResponse(String responseJson, List<RawRecord> batch) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new AiExtractionException("Gemini returned no candidates. Raw response: " + truncate(responseJson));
            }

            String text = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
            JsonNode arr = objectMapper.readTree(text);

            Map<Integer, CrmRecord> result = new java.util.LinkedHashMap<>();
            Map<Integer, Boolean> skipMap = new java.util.LinkedHashMap<>();

            for (JsonNode node : arr) {
                int rowIndex = node.path("row_index").asInt(-1);
                boolean skip = node.path("skip").asBoolean(false);
                skipMap.put(rowIndex, skip);
                if (skip || rowIndex < 0) continue;

                CrmRecord record = new CrmRecord();
                record.setCreatedAt(textOrEmpty(node, "created_at"));
                record.setName(textOrEmpty(node, "name"));
                record.setEmail(textOrEmpty(node, "email"));
                record.setCountryCode(textOrEmpty(node, "country_code"));
                record.setMobileWithoutCountryCode(textOrEmpty(node, "mobile_without_country_code"));
                record.setCompany(textOrEmpty(node, "company"));
                record.setCity(textOrEmpty(node, "city"));
                record.setState(textOrEmpty(node, "state"));
                record.setCountry(textOrEmpty(node, "country"));
                record.setLeadOwner(textOrEmpty(node, "lead_owner"));
                record.setCrmStatus(textOrEmpty(node, "crm_status"));
                record.setCrmNote(textOrEmpty(node, "crm_note"));
                record.setDataSource(textOrEmpty(node, "data_source"));
                record.setPossessionTime(textOrEmpty(node, "possession_time"));
                record.setDescription(textOrEmpty(node, "description"));

                result.put(rowIndex, record);
            }

            return result;

        } catch (AiExtractionException e) {
            throw e;
        } catch (Exception e) {
            throw new AiExtractionException("Could not parse the AI's response as valid JSON: " + e.getMessage(), e);
        }
    }

    private String textOrEmpty(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("");
    }

    private String truncate(String s) {
        return s == null ? "" : (s.length() > 300 ? s.substring(0, 300) + "..." : s);
    }
}

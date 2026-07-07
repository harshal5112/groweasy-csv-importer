package com.groweasy.csvimporter.service;

import com.groweasy.csvimporter.dto.CrmRecord;
import com.groweasy.csvimporter.dto.ImportResponse;
import com.groweasy.csvimporter.dto.RawRecord;
import com.groweasy.csvimporter.dto.SkippedRecord;
import com.groweasy.csvimporter.exception.AiExtractionException;
import com.groweasy.csvimporter.util.BatchUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class CsvImportService {

    private static final Logger log = LoggerFactory.getLogger(CsvImportService.class);

    private final CsvParserService csvParserService;
    private final GeminiExtractionService geminiExtractionService;
    private final CrmValidationService crmValidationService;

    @Value("${app.batch.size}")
    private int batchSize;

    public CsvImportService(CsvParserService csvParserService,
                             GeminiExtractionService geminiExtractionService,
                             CrmValidationService crmValidationService) {
        this.csvParserService = csvParserService;
        this.geminiExtractionService = geminiExtractionService;
        this.crmValidationService = crmValidationService;
    }

    public ImportResponse importCsv(MultipartFile file) {
        List<RawRecord> rawRecords = csvParserService.parse(file);
        List<List<RawRecord>> batches = BatchUtils.partition(rawRecords, batchSize);

        ImportResponse response = new ImportResponse();
        response.setTotalRows(rawRecords.size());

        int batchNumber = 0;
        for (List<RawRecord> batch : batches) {
            batchNumber++;
            try {
                Map<Integer, CrmRecord> extracted = geminiExtractionService.extractBatch(batch);
                processBatchResult(batch, extracted, response);
            } catch (AiExtractionException e) {
                log.error("Batch {} failed after retries, marking all rows in it as skipped", batchNumber, e);
                for (RawRecord raw : batch) {
                    response.getSkipped().add(new SkippedRecord(
                            raw.getRowIndex(),
                            "AI extraction failed for this batch: " + e.getMessage(),
                            raw.getFields()
                    ));
                }
                response.getWarnings().add("Batch " + batchNumber + " (rows "
                        + batch.get(0).getRowIndex() + "-" + batch.get(batch.size() - 1).getRowIndex()
                        + ") could not be processed by the AI and was skipped.");
            }
        }

        response.setTotalImported(response.getRecords().size());
        response.setTotalSkipped(response.getSkipped().size());
        return response;
    }

    private void processBatchResult(List<RawRecord> batch, Map<Integer, CrmRecord> extracted, ImportResponse response) {
        for (RawRecord raw : batch) {
            CrmRecord record = extracted.get(raw.getRowIndex());

            if (record == null) {
                response.getSkipped().add(new SkippedRecord(
                        raw.getRowIndex(),
                        "AI marked this row as not mappable (no usable contact info or unrecognizable data).",
                        raw.getFields()
                ));
                continue;
            }

            crmValidationService.sanitize(record);

            if (!crmValidationService.hasContactInfo(record)) {
                response.getSkipped().add(new SkippedRecord(
                        raw.getRowIndex(),
                        "Row has neither an email nor a mobile number.",
                        raw.getFields()
                ));
                continue;
            }

            response.getRecords().add(record);
        }
    }
}

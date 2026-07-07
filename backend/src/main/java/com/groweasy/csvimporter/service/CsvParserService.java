package com.groweasy.csvimporter.service;

import com.groweasy.csvimporter.dto.RawRecord;
import com.groweasy.csvimporter.exception.CsvProcessingException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses any valid CSV into a list of RawRecord objects, without assuming
 * any fixed set of column names. Column headers are read dynamically from
 * whatever the first row of the file contains.
 */
@Service
public class CsvParserService {

    private static final List<String> ACCEPTED_EXTENSIONS = List.of(".csv");

    public List<RawRecord> parse(MultipartFile file) {
        validateFile(file);

        List<RawRecord> records = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .setAllowMissingColumnNames(true)
                    .get();

            try (CSVParser parser = format.parse(reader)) {
                List<String> headers = parser.getHeaderNames();

                if (headers == null || headers.isEmpty()) {
                    throw new CsvProcessingException("The CSV file has no header row. Please include column names in the first row.");
                }

                int index = 0;
                for (CSVRecord csvRecord : parser) {
                    RawRecord raw = new RawRecord(index++);
                    for (String header : headers) {
                        if (header == null || header.isBlank()) continue;
                        String value = csvRecord.isMapped(header) ? csvRecord.get(header) : "";
                        raw.put(header, value);
                    }
                    records.add(raw);
                }
            }

        } catch (IOException e) {
            throw new CsvProcessingException("Unable to read the uploaded CSV file. Please make sure it is a valid, UTF-8 encoded CSV.", e);
        } catch (IllegalArgumentException e) {
            throw new CsvProcessingException("The CSV file appears malformed: " + e.getMessage(), e);
        }

        if (records.isEmpty()) {
            throw new CsvProcessingException("The CSV file has no data rows.");
        }

        return records;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CsvProcessingException("No file was uploaded, or the file is empty.");
        }
        String filename = file.getOriginalFilename();
        boolean validExtension = filename != null &&
                ACCEPTED_EXTENSIONS.stream().anyMatch(ext -> filename.toLowerCase().endsWith(ext));
        if (!validExtension) {
            throw new CsvProcessingException("Only .csv files are supported.");
        }
    }
}

package com.groweasy.csvimporter.controller;

import com.groweasy.csvimporter.dto.ImportResponse;
import com.groweasy.csvimporter.service.CsvImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/csv")
public class CsvImportController {

    private final CsvImportService csvImportService;

    public CsvImportController(CsvImportService csvImportService) {
        this.csvImportService = csvImportService;
    }

    /**
     * Accepts a CSV file, sends it through AI-powered field extraction, and
     * returns structured CRM records plus a summary of skipped rows.
     * Frontend calls this only after the user clicks "Confirm Import".
     */
    @PostMapping(value = "/import", consumes = "multipart/form-data")
    public ResponseEntity<ImportResponse> importCsv(@RequestParam("file") MultipartFile file) {
        ImportResponse response = csvImportService.importCsv(file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}

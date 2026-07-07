package com.groweasy.csvimporter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CsvProcessingException.class)
    public ResponseEntity<Object> handleCsvProcessing(CsvProcessingException ex) {
        return build(HttpStatus.BAD_REQUEST, "CSV_PROCESSING_ERROR", ex.getMessage());
    }

    @ExceptionHandler(AiExtractionException.class)
    public ResponseEntity<Object> handleAiExtraction(AiExtractionException ex) {
        return build(HttpStatus.BAD_GATEWAY, "AI_EXTRACTION_ERROR", ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxSize(MaxUploadSizeExceededException ex) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE", "Uploaded file exceeds the 5MB limit.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Something went wrong while processing your request.");
    }

    private ResponseEntity<Object> build(HttpStatus status, String code, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("errorCode", code);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}

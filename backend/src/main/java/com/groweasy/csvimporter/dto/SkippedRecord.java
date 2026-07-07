package com.groweasy.csvimporter.dto;

import java.util.Map;

public class SkippedRecord {
    private int rowIndex;
    private String reason;
    private Map<String, String> originalRow;

    public SkippedRecord() {}

    public SkippedRecord(int rowIndex, String reason, Map<String, String> originalRow) {
        this.rowIndex = rowIndex;
        this.reason = reason;
        this.originalRow = originalRow;
    }

    public int getRowIndex() { return rowIndex; }
    public void setRowIndex(int rowIndex) { this.rowIndex = rowIndex; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Map<String, String> getOriginalRow() { return originalRow; }
    public void setOriginalRow(Map<String, String> originalRow) { this.originalRow = originalRow; }
}

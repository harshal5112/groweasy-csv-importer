package com.groweasy.csvimporter.dto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A single raw row from the uploaded CSV, keyed by original column headers.
 * We keep the row index so we can report which original rows were skipped.
 */
public class RawRecord {

    private final int rowIndex;
    private final Map<String, String> fields;

    public RawRecord(int rowIndex) {
        this.rowIndex = rowIndex;
        this.fields = new LinkedHashMap<>();
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void put(String header, String value) {
        fields.put(header, value == null ? "" : value.trim());
    }
}

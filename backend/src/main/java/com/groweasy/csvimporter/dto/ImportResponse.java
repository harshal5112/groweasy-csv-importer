package com.groweasy.csvimporter.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResponse {

    private int totalRows;
    private int totalImported;
    private int totalSkipped;
    private List<CrmRecord> records = new ArrayList<>();
    private List<SkippedRecord> skipped = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getTotalImported() { return totalImported; }
    public void setTotalImported(int totalImported) { this.totalImported = totalImported; }

    public int getTotalSkipped() { return totalSkipped; }
    public void setTotalSkipped(int totalSkipped) { this.totalSkipped = totalSkipped; }

    public List<CrmRecord> getRecords() { return records; }
    public void setRecords(List<CrmRecord> records) { this.records = records; }

    public List<SkippedRecord> getSkipped() { return skipped; }
    public void setSkipped(List<SkippedRecord> skipped) { this.skipped = skipped; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
}

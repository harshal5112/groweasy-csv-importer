package com.groweasy.csvimporter.service;

import com.groweasy.csvimporter.dto.RawRecord;
import com.groweasy.csvimporter.exception.CsvProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserServiceTest {

    private final CsvParserService service = new CsvParserService();

    @Test
    void parsesArbitraryColumnNames() {
        String csv = "Full Name,Contact Email,Phone Number\nJohn Doe,john@x.com,9876543210\n";
        MockMultipartFile file = new MockMultipartFile("file", "leads.csv", "text/csv", csv.getBytes());

        List<RawRecord> records = service.parse(file);

        assertEquals(1, records.size());
        assertEquals("John Doe", records.get(0).getFields().get("Full Name"));
        assertEquals("john@x.com", records.get(0).getFields().get("Contact Email"));
    }

    @Test
    void throwsOnEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);
        assertThrows(CsvProcessingException.class, () -> service.parse(file));
    }

    @Test
    void throwsOnNonCsvExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "leads.txt", "text/plain", "a,b\n1,2".getBytes());
        assertThrows(CsvProcessingException.class, () -> service.parse(file));
    }

    @Test
    void throwsWhenNoDataRows() {
        MockMultipartFile file = new MockMultipartFile("file", "leads.csv", "text/csv", "name,email\n".getBytes());
        assertThrows(CsvProcessingException.class, () -> service.parse(file));
    }
}

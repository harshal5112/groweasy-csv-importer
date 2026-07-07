package com.groweasy.csvimporter.service;

import com.groweasy.csvimporter.dto.CrmRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CrmValidationServiceTest {

    private CrmValidationService service;

    @BeforeEach
    void setUp() {
        service = new CrmValidationService();
    }

    @Test
    void hasContactInfo_trueWhenEmailPresent() {
        CrmRecord r = new CrmRecord();
        r.setEmail("test@example.com");
        assertTrue(service.hasContactInfo(r));
    }

    @Test
    void hasContactInfo_trueWhenMobilePresent() {
        CrmRecord r = new CrmRecord();
        r.setMobileWithoutCountryCode("9876543210");
        assertTrue(service.hasContactInfo(r));
    }

    @Test
    void hasContactInfo_falseWhenNeitherPresent() {
        CrmRecord r = new CrmRecord();
        r.setName("John Doe");
        assertFalse(service.hasContactInfo(r));
    }

    @Test
    void sanitize_blanksInvalidCrmStatus() {
        CrmRecord r = new CrmRecord();
        r.setCrmStatus("NOT_A_REAL_STATUS");
        service.sanitize(r);
        assertEquals("", r.getCrmStatus());
    }

    @Test
    void sanitize_keepsValidCrmStatus() {
        CrmRecord r = new CrmRecord();
        r.setCrmStatus("SALE_DONE");
        service.sanitize(r);
        assertEquals("SALE_DONE", r.getCrmStatus());
    }

    @Test
    void sanitize_blanksInvalidDataSource() {
        CrmRecord r = new CrmRecord();
        r.setDataSource("some_random_source");
        service.sanitize(r);
        assertEquals("", r.getDataSource());
    }

    @Test
    void sanitize_keepsValidDataSource() {
        CrmRecord r = new CrmRecord();
        r.setDataSource("eden_park");
        service.sanitize(r);
        assertEquals("eden_park", r.getDataSource());
    }

    @Test
    void sanitize_handlesNullFieldsGracefully() {
        CrmRecord r = new CrmRecord();
        assertDoesNotThrow(() -> service.sanitize(r));
        assertEquals("", r.getName());
    }
}

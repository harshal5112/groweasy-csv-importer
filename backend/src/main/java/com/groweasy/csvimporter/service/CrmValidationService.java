package com.groweasy.csvimporter.service;

import com.groweasy.csvimporter.dto.CrmRecord;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * The AI is instructed to already follow these rules, but LLM output is
 * probabilistic - so we re-validate deterministically here as a safety net.
 * This guarantees the API contract is honored even if the model drifts.
 */
@Service
public class CrmValidationService {

    private static final Set<String> ALLOWED_STATUS = Set.of(
            "GOOD_LEAD_FOLLOW_UP", "DID_NOT_CONNECT", "BAD_LEAD", "SALE_DONE"
    );

    private static final Set<String> ALLOWED_SOURCE = Set.of(
            "leads_on_demand", "meridian_tower", "eden_park", "varah_swamy", "sarjapur_plots"
    );

    /**
     * Returns true if the record has at least an email or a mobile number,
     * per the assignment's mandatory skip rule.
     */
    public boolean hasContactInfo(CrmRecord record) {
        boolean hasEmail = record.getEmail() != null && !record.getEmail().isBlank();
        boolean hasMobile = record.getMobileWithoutCountryCode() != null && !record.getMobileWithoutCountryCode().isBlank();
        return hasEmail || hasMobile;
    }

    /**
     * Sanitizes a record in place: blanks out any enum field that doesn't
     * match the allowed set, and trims stray whitespace.
     */
    public void sanitize(CrmRecord record) {
        if (record.getCrmStatus() != null && !ALLOWED_STATUS.contains(record.getCrmStatus().trim())) {
            record.setCrmStatus("");
        }
        if (record.getDataSource() != null && !ALLOWED_SOURCE.contains(record.getDataSource().trim())) {
            record.setDataSource("");
        }
        trimAll(record);
    }

    private void trimAll(CrmRecord r) {
        r.setCreatedAt(safeTrim(r.getCreatedAt()));
        r.setName(safeTrim(r.getName()));
        r.setEmail(safeTrim(r.getEmail()));
        r.setCountryCode(safeTrim(r.getCountryCode()));
        r.setMobileWithoutCountryCode(safeTrim(r.getMobileWithoutCountryCode()));
        r.setCompany(safeTrim(r.getCompany()));
        r.setCity(safeTrim(r.getCity()));
        r.setState(safeTrim(r.getState()));
        r.setCountry(safeTrim(r.getCountry()));
        r.setLeadOwner(safeTrim(r.getLeadOwner()));
        r.setCrmStatus(safeTrim(r.getCrmStatus()));
        r.setCrmNote(safeTrim(r.getCrmNote()));
        r.setDataSource(safeTrim(r.getDataSource()));
        r.setPossessionTime(safeTrim(r.getPossessionTime()));
        r.setDescription(safeTrim(r.getDescription()));
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}

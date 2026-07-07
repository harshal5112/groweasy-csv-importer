package com.groweasy.csvimporter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a single lead in GrowEasy's CRM format.
 * This is the target shape that the AI must map arbitrary CSV columns into.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class CrmRecord {

    private String createdAt;
    private String name;
    private String email;
    private String countryCode;
    private String mobileWithoutCountryCode;
    private String company;
    private String city;
    private String state;
    private String country;
    private String leadOwner;
    private String crmStatus;
    private String crmNote;
    private String dataSource;
    private String possessionTime;
    private String description;

    public CrmRecord() {}

    // Getters and setters
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getMobileWithoutCountryCode() { return mobileWithoutCountryCode; }
    public void setMobileWithoutCountryCode(String mobileWithoutCountryCode) { this.mobileWithoutCountryCode = mobileWithoutCountryCode; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getLeadOwner() { return leadOwner; }
    public void setLeadOwner(String leadOwner) { this.leadOwner = leadOwner; }

    public String getCrmStatus() { return crmStatus; }
    public void setCrmStatus(String crmStatus) { this.crmStatus = crmStatus; }

    public String getCrmNote() { return crmNote; }
    public void setCrmNote(String crmNote) { this.crmNote = crmNote; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public String getPossessionTime() { return possessionTime; }
    public void setPossessionTime(String possessionTime) { this.possessionTime = possessionTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

package com.rehabai.plan_service.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Client for communicating with patient-service.
 * Validates patient profiles and medical history.
 */
@Component
public class PatientClient {

    private static final Logger log = LoggerFactory.getLogger(PatientClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public record PatientProfileDTO(
        UUID id,
        UUID userId,
        String preferredLanguage,
        String biologicalSex,
        String dateOfBirth,
        String notes
    ) {}

    public PatientClient(RestTemplate restTemplate,
                        @Value("${patient.service.url:http://patient-service:8083}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Get patient profile from patient-service.
     * Returns null if profile not found.
     */
    public PatientProfileDTO getPatientProfile(UUID userId) {
        String url = baseUrl + "/patients/" + userId + "/profile";
        try {
            return restTemplate.getForObject(url, PatientProfileDTO.class);
        } catch (HttpClientErrorException.NotFound nf) {
            log.warn("Patient profile not found for userId={}", userId);
            return null;
        } catch (HttpClientErrorException e) {
            log.warn("Patient service error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("patient_service_unavailable");
        } catch (Exception e) {
            log.error("Patient service call failed", e);
            throw new IllegalStateException("patient_service_error");
        }
    }

    /**
     * Validate that patient profile exists.
     * Throws exception if not found.
     */
    public void requirePatientProfile(UUID userId) {
        PatientProfileDTO profile = getPatientProfile(userId);
        if (profile == null) {
            throw new IllegalArgumentException(
                "patient_profile_not_found: Patient must have a profile before creating treatment plans. " +
                "Please complete patient registration in patient-service first."
            );
        }
        log.debug("Patient profile validated: userId={}", userId);
    }

    /**
     * Check if patient has a profile (non-throwing).
     */
    public boolean hasPatientProfile(UUID userId) {
        try {
            return getPatientProfile(userId) != null;
        } catch (Exception e) {
            log.warn("Failed to check patient profile existence: {}", e.getMessage());
            return false;
        }
    }
}


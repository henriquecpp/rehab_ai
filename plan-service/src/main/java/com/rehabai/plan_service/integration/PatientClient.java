package com.rehabai.plan_service.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

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
                        @Value("${patient.service.url:http://patient-service:8087}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public PatientProfileDTO getPatientProfile(UUID userId) {
        String url = baseUrl + "/patients/" + userId + "/profile";
        long start = System.currentTimeMillis();
        try {
            PatientProfileDTO dto = restTemplate.getForObject(url, PatientProfileDTO.class);
            log.debug("Patient profile fetched userId={} latency={}ms", userId, System.currentTimeMillis() - start);
            return dto;
        } catch (HttpClientErrorException.NotFound nf) {
            log.warn("Patient profile not found userId={} url={} latency={}ms", userId, url, System.currentTimeMillis() - start);
            return null;
        } catch (HttpClientErrorException e) {
            log.error("Patient service client error status={} body={} userId={} url={} latency={}ms", e.getStatusCode(), e.getResponseBodyAsString(), userId, url, System.currentTimeMillis() - start);
            throw new IllegalStateException("patient_service_unavailable");
        } catch (IllegalStateException ie) {
            log.error("Patient service illegal state userId={} latency={}ms msg={}", userId, System.currentTimeMillis() - start, ie.getMessage());
            throw ie;
        } catch (Exception e) {
            log.error("Patient service call failed userId={} latency={}ms msg={}", userId, System.currentTimeMillis() - start, e.getMessage());
            throw new IllegalStateException("patient_service_error");
        }
    }

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

    public boolean hasPatientProfile(UUID userId) {
        try {
            return getPatientProfile(userId) != null;
        } catch (Exception e) {
            log.warn("Failed to check patient profile existence: {}", e.getMessage());
            return false;
        }
    }
}

package com.rehabai.patient_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@Component
public class UserClient {
    private static final Logger log = LoggerFactory.getLogger(UserClient.class);

    public record UserDTO(UUID id, String email, String fullName, String role, Boolean active) {}

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UserClient(RestTemplate restTemplate,
                      @Value("${user.service.url:http://user-service:8082}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public UserDTO getUser(UUID userId) {
        try {
            return restTemplate.getForObject(baseUrl + "/users/" + userId, UserDTO.class);
        } catch (HttpClientErrorException.NotFound nf) {
            return null;
        } catch (Exception e) {
            log.warn("User service error: {}", e.getMessage());
            throw new IllegalStateException("user_service_error");
        }
    }

    public void requireActivePatient(UUID userId) {
        UserDTO u = getUser(userId);
        if (u == null) throw new IllegalArgumentException("user_not_found");
        if (u.active() == null || !u.active()) throw new IllegalArgumentException("user_inactive");
        if (!"PATIENT".equalsIgnoreCase(u.role())) throw new IllegalArgumentException("user_not_patient");
    }
}


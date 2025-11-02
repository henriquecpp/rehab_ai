package com.rehabai.plan_service.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public record UserDTO(UUID id, String email, String fullName, String role, Boolean active) {}

    public UserClient(RestTemplate restTemplate,
                      @Value("${user.service.url:http://user-service:8082}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public UserDTO getUser(UUID userId) {
        String url = baseUrl + "/users/" + userId;
        try {
            return restTemplate.getForObject(url, UserDTO.class);
        } catch (HttpClientErrorException.NotFound nf) {
            throw new IllegalArgumentException("user_not_found: " + userId);
        } catch (HttpClientErrorException e) {
            log.warn("User service error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("user_service_unavailable");
        } catch (Exception e) {
            log.warn("User service call failed: {}", e.getMessage());
            throw new IllegalStateException("user_service_error");
        }
    }

    public void requireActivePatient(UUID userId) {
        UserDTO u = getUser(userId);
        if (u == null) {
            throw new IllegalArgumentException("user_not_found: " + userId);
        }
        if (u.active() == null || !u.active()) {
            throw new IllegalArgumentException("user_inactive");
        }
        String role = u.role() == null ? "" : u.role().toUpperCase();
        if (!role.equals("PATIENT")) {
            throw new IllegalArgumentException("user_not_patient");
        }
    }
}

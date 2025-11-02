package com.rehabai.auth_service.service;

import com.rehabai.auth_service.model.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UserServiceClient(RestTemplate restTemplate,
                             @Value("${user.service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public UserResponse createUser(CreateUserRequest req) {
        String url = baseUrl + "/users";
        ResponseEntity<UserResponse> resp = restTemplate.postForEntity(url, req, UserResponse.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("User creation failed with status: " + resp.getStatusCode());
        }

        UserResponse body = resp.getBody();
        if (body == null) {
            throw new IllegalStateException("User service returned empty response body");
        }

        return body;
    }

    public CredentialsResponse getCredentialsByEmail(String email) {
        String url = baseUrl + "/internal/users/credentials?email={email}";
        CredentialsResponse response = restTemplate.getForObject(url, CredentialsResponse.class, email);

        if (response == null) {
            throw new IllegalStateException("User service returned null credentials for email: " + email);
        }

        return response;
    }

    public UserResponse getByEmail(String email) {
        String url = baseUrl + "/users/email/{email}";
        UserResponse response = restTemplate.getForObject(url, UserResponse.class, email);

        if (response == null) {
            throw new IllegalStateException("User service returned null user for email: " + email);
        }

        return response;
    }

    public UserResponse getById(UUID id) {
        String url = baseUrl + "/users/{id}";
        UserResponse response = restTemplate.getForObject(url, UserResponse.class, id);

        if (response == null) {
            throw new IllegalStateException("User service returned null user for id: " + id);
        }

        return response;
    }

    public long countUsers() {
        String url = baseUrl + "/internal/users/count";
        Long c = restTemplate.getForObject(url, Long.class);
        return c != null ? c : 0L;
    }

    public boolean anyAdmin() {
        String url = baseUrl + "/internal/users/any-admin";
        Boolean val = restTemplate.getForObject(url, Boolean.class);
        return Boolean.TRUE.equals(val);
    }

    // DTOs used to communicate with user-service
    public record CreateUserRequest(String email, String fullName, String passwordHash, UserRole role) {}
    public record UserResponse(UUID id, String email, String fullName, UserRole role, Boolean active) {}
    public record CredentialsResponse(UUID id, String email, String passwordHash, UserRole role, Boolean active) {}
}

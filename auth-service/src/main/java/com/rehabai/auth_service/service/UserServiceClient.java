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
        return resp.getBody();
    }

    public CredentialsResponse getCredentialsByEmail(String email) {
        String url = baseUrl + "/internal/users/credentials?email={email}";
        return restTemplate.getForObject(url, CredentialsResponse.class, email);
    }

    public UserResponse getByEmail(String email) {
        String url = baseUrl + "/users/email/{email}";
        return restTemplate.getForObject(url, UserResponse.class, email);
    }

    public UserResponse getById(UUID id) {
        String url = baseUrl + "/users/{id}";
        return restTemplate.getForObject(url, UserResponse.class, id);
    }

    // DTOs used to communicate with user-service
    public record CreateUserRequest(String email, String fullName, String passwordHash, UserRole role) {}
    public record UserResponse(UUID id, String email, String fullName, UserRole role, Boolean active) {}
    public record CredentialsResponse(UUID id, String email, String passwordHash, UserRole role, Boolean active) {}
}

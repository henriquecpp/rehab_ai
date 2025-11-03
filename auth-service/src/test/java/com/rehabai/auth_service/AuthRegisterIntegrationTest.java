package com.rehabai.auth_service;

import com.rehabai.auth_service.dto.AuthResponse;
import com.rehabai.auth_service.dto.RegisterRequest;
import com.rehabai.auth_service.model.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthRegisterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setup() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void register_shouldCreateUserViaUserService_andReturnTokens() throws Exception {
        UUID userId = UUID.randomUUID();
        String email = "admin@example.com";
        String fullName = "Admin User";
        String createUserResp = "{" +
                "\"id\":\"" + userId + "\"," +
                "\"email\":\"" + email + "\"," +
                "\"fullName\":\"" + fullName + "\"," +
                "\"role\":\"ADMIN\"," +
                "\"active\":true" +
                "}";
        String credentialsResp = "{" +
                "\"id\":\"" + userId + "\"," +
                "\"email\":\"" + email + "\"," +
                "\"passwordHash\":\"$2a$10$abcdefghijkABCDEFGHIJK1234567890lmnopqrstuv\"," +
                "\"role\":\"ADMIN\"," +
                "\"active\":true" +
                "}";
        String userByEmailResp = createUserResp;

        mockServer.expect(requestTo("http://user-service:8082/internal/users/count"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("0", MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo("http://user-service:8082/users"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(createUserResp, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(new URI("http://user-service:8082/internal/users/credentials?email=" + email)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(credentialsResp, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo("http://user-service:8082/users/email/" + email))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(userByEmailResp, MediaType.APPLICATION_JSON));

        RegisterRequest req = new RegisterRequest(email, "StrongPass!2025", fullName, UserRole.ADMIN);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()));

        mockServer.verify();
    }
}

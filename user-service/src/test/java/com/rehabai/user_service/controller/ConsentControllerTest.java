package com.rehabai.user_service.controller;

import com.rehabai.user_service.dto.ConsentDtos;
import com.rehabai.user_service.dto.UserDtos;
import com.rehabai.user_service.service.ConsentService;
import com.rehabai.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConsentController.class)
class ConsentControllerTest {

    @Autowired MockMvc mvc;

    @MockBean ConsentService consentService;
    @MockBean UserService userService;

    @Test
    void latest_whenConsentNotFound_returns404() throws Exception {
        UUID userId = UUID.randomUUID();
        Mockito.when(consentService.latestByType(eq(userId), eq("privacy")))
                .thenThrow(new IllegalArgumentException("consent_not_found"));

        mvc.perform(get("/users/" + userId + "/consents/latest?type=privacy"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("consent_not_found"));
    }

    @Test
    void listForMe_whenAuthenticatedWithoutUserIdClaim_usesEmailFallback() throws Exception {
        UUID uid = UUID.randomUUID();
        Mockito.when(userService.getByEmail("me@example.com"))
                .thenReturn(new UserDtos.Response(uid, "me@example.com", "Me", null, true));
        Mockito.when(consentService.listByUser(eq(uid), isNull()))
                .thenReturn(List.of());

        mvc.perform(get("/users/me/consents")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt.subject("me@example.com")))
                )
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void createForMe_whenServiceThrowsTypeRequired_returns400() throws Exception {
        UUID uid = UUID.randomUUID();
        Mockito.when(userService.getByEmail("me@example.com"))
                .thenReturn(new UserDtos.Response(uid, "me@example.com", "Me", null, true));
        Mockito.when(consentService.create(eq(uid), any(ConsentDtos.CreateRequest.class)))
                .thenThrow(new IllegalArgumentException("type_required"));

        mvc.perform(post("/users/me/consents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"granted\":true}")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt.subject("me@example.com"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("type_required"));
    }
}


package com.rehabai.user_service.controller;

import com.rehabai.user_service.security.SecurityHelper;
import com.rehabai.user_service.dto.ConsentDtos;
import com.rehabai.user_service.service.ConsentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

    @MockitoBean ConsentService consentService;
    @MockitoBean SecurityHelper securityHelper;

    @Test
    void latest_whenConsentNotFound_returns400() throws Exception {
        UUID userId = UUID.randomUUID();

        Mockito.when(securityHelper.getAuthenticatedUserId()).thenReturn(userId);
        Mockito.doNothing().when(securityHelper).validateResourceAccess(userId);

        Mockito.when(consentService.latestByType(eq(userId), eq("privacy")))
                .thenThrow(new IllegalArgumentException("consent_not_found"));

        mvc.perform(get("/users/" + userId + "/consents/latest?type=privacy")
                        .header("X-User-Id", userId.toString())
                        .header("X-User-Roles", "ROLE_PATIENT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("consent_not_found"));
    }

    @Test
    void listForMe_withAuthenticatedUser_returnsConsentList() throws Exception {
        UUID uid = UUID.randomUUID();

        Mockito.when(securityHelper.getAuthenticatedUserId()).thenReturn(uid);
        Mockito.when(consentService.listByUser(eq(uid), isNull()))
                .thenReturn(List.of());

        mvc.perform(get("/users/me/consents")
                        .header("X-User-Id", uid.toString())
                        .header("X-User-Roles", "ROLE_PATIENT"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void createForMe_whenServiceThrowsTypeRequired_returns400() throws Exception {
        UUID uid = UUID.randomUUID();

        Mockito.when(securityHelper.getAuthenticatedUserId()).thenReturn(uid);

        Mockito.when(consentService.create(eq(uid), any(ConsentDtos.CreateRequest.class)))
                .thenThrow(new IllegalArgumentException("type_required"));

        mvc.perform(post("/users/me/consents")
                        .header("X-User-Id", uid.toString())
                        .header("X-User-Roles", "ROLE_PATIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"granted\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("type_required"));
    }

    @Test
    void create_withDifferentUserId_whenPatient_shouldValidateAccess() throws Exception {
        UUID authenticatedUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        Mockito.when(securityHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        Mockito.doThrow(new IllegalArgumentException("Access denied: You can only access your own resources"))
                .when(securityHelper).validateResourceAccess(targetUserId);

        mvc.perform(post("/users/" + targetUserId + "/consents")
                        .header("X-User-Id", authenticatedUserId.toString())
                        .header("X-User-Roles", "ROLE_PATIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"DATA_PROCESSING\",\"granted\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Access denied: You can only access your own resources"));
    }

    @Test
    void create_withSameUserId_whenPatient_shouldSucceed() throws Exception {
        UUID userId = UUID.randomUUID();
        ConsentDtos.Response response = new ConsentDtos.Response(
                UUID.randomUUID(),
                userId,
                "DATA_PROCESSING",
                true,
                null
        );

        Mockito.when(securityHelper.getAuthenticatedUserId()).thenReturn(userId);
        Mockito.doNothing().when(securityHelper).validateResourceAccess(userId);

        Mockito.when(consentService.create(eq(userId), any(ConsentDtos.CreateRequest.class)))
                .thenReturn(response);

        mvc.perform(post("/users/" + userId + "/consents")
                        .header("X-User-Id", userId.toString())
                        .header("X-User-Roles", "ROLE_PATIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"DATA_PROCESSING\",\"granted\":true}"))
                .andExpect(status().isCreated());
    }

    @Test
    void create_withDifferentUserId_whenClinician_shouldSucceed() throws Exception {
        UUID clinicianId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        ConsentDtos.Response response = new ConsentDtos.Response(
                UUID.randomUUID(),
                patientId,
                "DATA_PROCESSING",
                true,
                null
        );

        Mockito.when(securityHelper.getAuthenticatedUserId()).thenReturn(clinicianId);
        Mockito.doNothing().when(securityHelper).validateResourceAccess(patientId);

        Mockito.when(consentService.create(eq(patientId), any(ConsentDtos.CreateRequest.class)))
                .thenReturn(response);

        mvc.perform(post("/users/" + patientId + "/consents")
                        .header("X-User-Id", clinicianId.toString())
                        .header("X-User-Roles", "ROLE_CLINICIAN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"DATA_PROCESSING\",\"granted\":true}"))
                .andExpect(status().isCreated());
    }
}


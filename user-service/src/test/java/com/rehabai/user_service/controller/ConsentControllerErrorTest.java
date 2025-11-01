package com.rehabai.user_service.controller;

import com.rehabai.user_service.service.ConsentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConsentController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConsentControllerErrorTest {

    @Autowired MockMvc mvc;

    @MockitoBean ConsentService consentService;

    @Test
    void latest_missingType_returns400() throws Exception {
        UUID userId = UUID.randomUUID();
        mvc.perform(get("/users/" + userId + "/consents/latest"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_ok_withType() throws Exception {
        UUID userId = UUID.randomUUID();
        Mockito.when(consentService.listByUser(Mockito.eq(userId), Mockito.eq("privacy")))
                .thenReturn(List.of());
        mvc.perform(get("/users/" + userId + "/consents").param("type", "privacy"))
                .andExpect(status().isOk());
    }

    @Test
    void latest_whenNotFound_returns404() throws Exception {
        UUID userId = UUID.randomUUID();
        Mockito.when(consentService.latestByType(Mockito.eq(userId), Mockito.eq("privacy")))
                .thenThrow(new IllegalArgumentException("consent_not_found"));
        mvc.perform(get("/users/" + userId + "/consents/latest").param("type", "privacy"))
                .andExpect(status().isNotFound());
    }
}

package com.rehabai.patient_service.controller;

import com.rehabai.patient_service.service.PatientProfileService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class PatientProfileControllerErrorTest {

    @Autowired MockMvc mvc;

    @MockitoBean PatientProfileService service;

    @Test
    void getProfile_whenNotFound_returns400() throws Exception {
        UUID userId = UUID.randomUUID();
        Mockito.when(service.get(userId)).thenThrow(new IllegalArgumentException("profile_not_found"));
        mvc.perform(get("/patients/" + userId + "/profile"))
                .andExpect(status().isBadRequest());
    }
}

package com.rehabai.patient_service.controller;

import com.rehabai.patient_service.service.PatientService;
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

@WebMvcTest(PatientController.class)
@AutoConfigureMockMvc(addFilters = false)
class PatientControllerErrorTest {

    @Autowired MockMvc mvc;

    @MockitoBean PatientService service;

    @Test
    void getPatient_whenNotFound_returns400() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(service.get(id)).thenThrow(new IllegalArgumentException("patient_not_found"));
        mvc.perform(get("/patients/" + id))
                .andExpect(status().isBadRequest());
    }
}

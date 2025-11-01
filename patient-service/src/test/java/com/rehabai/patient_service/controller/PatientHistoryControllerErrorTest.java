package com.rehabai.patient_service.controller;

import com.rehabai.patient_service.service.PatientHistoryService;
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

@WebMvcTest(PatientHistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class PatientHistoryControllerErrorTest {

    @Autowired MockMvc mvc;

    @MockitoBean PatientHistoryService service;

    @Test
    void getCondition_whenNotFound_returns400() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID conditionId = UUID.randomUUID();
        Mockito.when(service.getCondition(userId, conditionId)).thenThrow(new IllegalArgumentException("condition_not_found"));
        mvc.perform(get("/patients/" + userId + "/history/conditions/" + conditionId))
                .andExpect(status().isBadRequest());
    }
}

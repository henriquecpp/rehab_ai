package com.rehabai.prescription_service.controller;

import com.rehabai.prescription_service.repository.ExtractionRepository;
import com.rehabai.prescription_service.repository.NormalizationRepository;
import com.rehabai.prescription_service.repository.PrescriptionRepository;
import com.rehabai.prescription_service.repository.WorkflowRunRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowController.class)
class WorkflowControllerTest {

    @Autowired MockMvc mvc;

    @MockBean WorkflowRunRepository runRepo;
    @MockBean ExtractionRepository extractionRepo;
    @MockBean NormalizationRepository normalizationRepo;
    @MockBean PrescriptionRepository prescriptionRepo;

    @Test
    void latestWorkflow_whenNotFound_returns404() throws Exception {
        Mockito.when(runRepo.findTopByFileIdOrderByCreatedAtDesc(Mockito.any(UUID.class))).thenReturn(Optional.empty());
        mvc.perform(get("/prescriptions/workflows/latest?fileId=" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}


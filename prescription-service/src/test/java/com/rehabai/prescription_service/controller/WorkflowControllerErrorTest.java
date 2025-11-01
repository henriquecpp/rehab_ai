package com.rehabai.prescription_service.controller;

import com.rehabai.prescription_service.repository.ExtractionRepository;
import com.rehabai.prescription_service.repository.NormalizationRepository;
import com.rehabai.prescription_service.repository.PrescriptionRepository;
import com.rehabai.prescription_service.repository.WorkflowRunRepository;
import com.rehabai.prescription_service.repository.AiTraceRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkflowControllerErrorTest {

    @Autowired MockMvc mvc;

    @MockitoBean WorkflowRunRepository runRepo;
    @MockitoBean ExtractionRepository extractionRepo;
    @MockitoBean NormalizationRepository normalizationRepo;
    @MockitoBean PrescriptionRepository prescriptionRepo;
    @MockitoBean AiTraceRepository aiTraceRepo;

    @Test
    void latestWorkflow_whenNotFound_returns404() throws Exception {
        UUID fileId = UUID.randomUUID();
        Mockito.when(runRepo.findTopByFileIdOrderByCreatedAtDesc(fileId)).thenReturn(Optional.empty());
        mvc.perform(get("/prescriptions/workflows/latest").param("fileId", fileId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void latestStages_whenNoExtraction_returns404() throws Exception {
        UUID fileId = UUID.randomUUID();
        Mockito.when(extractionRepo.findTopByFileIdOrderByCreatedAtDesc(fileId)).thenReturn(Optional.empty());
        mvc.perform(get("/prescriptions/stages/latest").param("fileId", fileId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getExtraction_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(extractionRepo.findById(id)).thenReturn(Optional.empty());
        mvc.perform(get("/prescriptions/extractions/" + id))
                .andExpect(status().isNotFound());
    }
}

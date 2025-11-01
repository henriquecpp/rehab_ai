package com.rehabai.file_service.controller;

import com.rehabai.file_service.model.FileStatus;
import com.rehabai.file_service.model.IngestionFile;
import com.rehabai.file_service.service.AnonymizationLogService;
import com.rehabai.file_service.service.StorageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
@AutoConfigureMockMvc(addFilters = false)
class FileControllerPseudonymizeTest {

    @Autowired MockMvc mvc;

    @MockitoBean StorageService storageService;
    @MockitoBean AnonymizationLogService anonymizationLogService;

    @Test
    void pseudonymize_ok_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        IngestionFile f = new IngestionFile();
        f.setStatus(FileStatus.ANONYMIZED);
        Mockito.when(storageService.pseudonymize(id)).thenReturn(f);
        mvc.perform(post("/files/" + id + "/pseudonymize"))
                .andExpect(status().isOk());
    }
}


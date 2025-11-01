package com.rehabai.file_service.controller;

import com.rehabai.file_service.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
@AutoConfigureMockMvc(addFilters = false)
class FileControllerErrorTest {

    @Autowired MockMvc mvc;

    @MockitoBean StorageService storageService;

    @Test
    void upload_missingPart_returns400() throws Exception {
        mvc.perform(multipart("/files/upload"))
                .andExpect(status().isBadRequest());
    }
}

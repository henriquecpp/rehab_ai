package com.rehabai.file_service.controller;

import com.rehabai.file_service.security.SecurityHelper;
import com.rehabai.file_service.model.FileStatus;
import com.rehabai.file_service.model.IngestionFile;
import com.rehabai.file_service.service.AnonymizationLogService;
import com.rehabai.file_service.service.StorageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean StorageService storageService;
    @MockitoBean AnonymizationLogService anonymizationLogService;
    @MockitoBean SecurityHelper securityHelper;

    @Test
    void upload_withoutFile_returns400() throws Exception {
        UUID clinicianId = UUID.randomUUID();

        Mockito.doNothing().when(securityHelper).requireClinician();

        mvc.perform(multipart("/files/upload")
                        .header("X-User-Id", clinicianId.toString())
                        .header("X-User-Roles", "ROLE_CLINICIAN")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upload_withFile_whenClinician_returns200() throws Exception {
        UUID clinicianId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        IngestionFile savedFile = new IngestionFile();
        savedFile.setId(fileId);
        savedFile.setUserId(patientId);
        savedFile.setOriginalName("test.pdf");
        savedFile.setStatus(FileStatus.UPLOADED);

        Mockito.doNothing().when(securityHelper).requireClinician();

        Mockito.when(storageService.upload(any(), eq(patientId)))
                .thenReturn(savedFile);

        mvc.perform(multipart("/files/upload")
                        .file(file)
                        .param("userId", patientId.toString())
                        .header("X-User-Id", clinicianId.toString())
                        .header("X-User-Roles", "ROLE_CLINICIAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fileId.toString()))
                .andExpect(jsonPath("$.userId").value(patientId.toString()));
    }

    @Test
    void upload_whenPatient_shouldReject() throws Exception {
        UUID patientId = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        Mockito.doThrow(new IllegalArgumentException("Access denied: This operation requires CLINICIAN or ADMIN role"))
                .when(securityHelper).requireClinician();

        mvc.perform(multipart("/files/upload")
                        .file(file)
                        .param("userId", patientId.toString())
                        .header("X-User-Id", patientId.toString())
                        .header("X-User-Roles", "ROLE_PATIENT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Access denied: This operation requires CLINICIAN or ADMIN role"));
    }

    @Test
    void get_whenOwnFile_shouldSucceed() throws Exception {
        UUID patientId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        IngestionFile file = new IngestionFile();
        file.setId(fileId);
        file.setUserId(patientId);
        file.setOriginalName("test.pdf");
        file.setStatus(FileStatus.UPLOADED);

        Mockito.when(securityHelper.getAuthenticatedUserId()).thenReturn(patientId);
        Mockito.doNothing().when(securityHelper).validateResourceAccess(patientId);

        Mockito.when(storageService.get(fileId)).thenReturn(file);

        mvc.perform(get("/files/" + fileId)
                        .header("X-User-Id", patientId.toString())
                        .header("X-User-Roles", "ROLE_PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fileId.toString()))
                .andExpect(jsonPath("$.userId").value(patientId.toString()));
    }

    @Test
    void get_whenOtherPatientFile_shouldReject() throws Exception {
        UUID patientAId = UUID.randomUUID();
        UUID patientBId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        IngestionFile file = new IngestionFile();
        file.setId(fileId);
        file.setUserId(patientBId);
        file.setOriginalName("test.pdf");

        Mockito.when(securityHelper.getAuthenticatedUserId()).thenReturn(patientAId);
        Mockito.doThrow(new IllegalArgumentException("Access denied: You can only access your own resources"))
                .when(securityHelper).validateResourceAccess(patientBId);

        Mockito.when(storageService.get(fileId)).thenReturn(file);

        mvc.perform(get("/files/" + fileId)
                        .header("X-User-Id", patientAId.toString())
                        .header("X-User-Roles", "ROLE_PATIENT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Access denied: You can only access your own resources"));
    }

    @Test
    void list_whenPatient_shouldOnlyListOwnFiles() throws Exception {
        UUID patientId = UUID.randomUUID();

        IngestionFile file1 = new IngestionFile();
        file1.setId(UUID.randomUUID());
        file1.setUserId(patientId);

        Mockito.when(securityHelper.getAuthenticatedUserId()).thenReturn(patientId);
        Mockito.when(securityHelper.hasAnyRole("ADMIN", "CLINICIAN")).thenReturn(false);

        Mockito.when(storageService.list(patientId, null)).thenReturn(List.of(file1));

        mvc.perform(get("/files")
                        .header("X-User-Id", patientId.toString())
                        .header("X-User-Roles", "ROLE_PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(patientId.toString()));
    }

    @Test
    void list_whenClinician_shouldListPatientFiles() throws Exception {
        UUID clinicianId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        IngestionFile file1 = new IngestionFile();
        file1.setId(UUID.randomUUID());
        file1.setUserId(patientId);

        Mockito.when(securityHelper.getAuthenticatedUserId()).thenReturn(clinicianId);
        Mockito.when(securityHelper.hasAnyRole("ADMIN", "CLINICIAN")).thenReturn(true);

        Mockito.when(storageService.list(patientId, null)).thenReturn(List.of(file1));

        mvc.perform(get("/files")
                        .param("userId", patientId.toString())
                        .header("X-User-Id", clinicianId.toString())
                        .header("X-User-Roles", "ROLE_CLINICIAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(patientId.toString()));
    }

    @Test
    void pseudonymize_whenClinician_shouldSucceed() throws Exception {
        UUID clinicianId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        IngestionFile updatedFile = new IngestionFile();
        updatedFile.setId(fileId);
        updatedFile.setStatus(FileStatus.ANONYMIZED);

        Mockito.doNothing().when(securityHelper).requireClinician();

        Mockito.when(storageService.pseudonymize(fileId)).thenReturn(updatedFile);

        mvc.perform(post("/files/" + fileId + "/pseudonymize")
                        .header("X-User-Id", clinicianId.toString())
                        .header("X-User-Roles", "ROLE_CLINICIAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ANONYMIZED"));
    }

    @Test
    void pseudonymize_whenPatient_shouldReject() throws Exception {
        UUID patientId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        Mockito.doThrow(new IllegalArgumentException("Access denied: This operation requires CLINICIAN or ADMIN role"))
                .when(securityHelper).requireClinician();

        mvc.perform(post("/files/" + fileId + "/pseudonymize")
                        .header("X-User-Id", patientId.toString())
                        .header("X-User-Roles", "ROLE_PATIENT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Access denied: This operation requires CLINICIAN or ADMIN role"));
    }
}


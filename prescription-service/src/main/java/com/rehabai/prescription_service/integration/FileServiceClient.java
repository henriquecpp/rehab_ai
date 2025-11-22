package com.rehabai.prescription_service.integration;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileServiceClient {

    private final RestTemplate restTemplate;

    @Value("${file-service.url:http://file-service:8083}")
    private String fileServiceUrl;

    public FileMetadataDTO getFileMetadata(UUID fileId, String authToken, String userId, String userRoles, String userEmail) {
        String url = fileServiceUrl + "/files/" + fileId;

        try {
            HttpHeaders headers = new HttpHeaders();
            if (authToken != null) {
                headers.set("Authorization", authToken);
            }
            if (userId != null) {
                headers.set("X-User-Id", userId);
            }
            if (userRoles != null) {
                headers.set("X-User-Roles", userRoles);
            }
            if (userEmail != null) {
                headers.set("X-User-Email", userEmail);
            }

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            long startMs = System.currentTimeMillis();
            ResponseEntity<FileMetadataDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                FileMetadataDTO.class
            );
            long latency = System.currentTimeMillis() - startMs;

            log.debug("File metadata fetched fileId={} latency={}ms", fileId, latency);
            return response.getBody();

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("File not found fileId={}", fileId);
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch file metadata fileId={} error={}", fileId, e.getMessage(), e);
            return null;
        }
    }

    @Data
    public static class FileMetadataDTO {
        private UUID id;
        private UUID userId;
        private String originalName;
        private String s3Path;
        private String status;
        private String fileType;
        private Long sizeBytes;
        private String hashSha256;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }
}


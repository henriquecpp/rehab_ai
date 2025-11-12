package com.rehabai.prescription_service.events;

import java.util.UUID;

public record FileUploadedEvent(
        UUID id,
        UUID userId,  // ⭐ Adicionado
        String bucket,
        String s3Path,
        String originalName,
        long sizeBytes,
        String hashSha256,
        FileType fileType
) {}


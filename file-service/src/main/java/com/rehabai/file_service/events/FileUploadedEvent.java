package com.rehabai.file_service.events;

import java.util.UUID;

public record FileUploadedEvent(
        UUID id,
        String bucket,
        String s3Path,
        String originalName,
        long sizeBytes,
        String hashSha256
) {}

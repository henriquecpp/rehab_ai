package com.rehabai.file_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ingestion_files", indexes = {
        @Index(name = "idx_ingestion_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class IngestionFile {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "original_name", length = 255)
    private String originalName;

    @Column(name = "s3_path", nullable = false, length = 500)
    private String s3Path;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private FileStatus status = FileStatus.UPLOADED;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "hash_sha256", length = 64)
    private String hashSha256;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    public void onUpdate() { this.updatedAt = OffsetDateTime.now(); }
}

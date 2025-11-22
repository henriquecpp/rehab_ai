package com.rehabai.file_service.controller;

import com.rehabai.file_service.security.SecurityHelper;
import com.rehabai.file_service.model.AnonymizationLog;
import com.rehabai.file_service.model.FileStatus;
import com.rehabai.file_service.model.FileType;
import com.rehabai.file_service.model.IngestionFile;
import com.rehabai.file_service.service.AnonymizationLogService;
import com.rehabai.file_service.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(
    name = "File Management",
    description = """
        # 📁 Gerenciamento de Arquivos Médicos
        
        Upload, armazenamento, pseudonimização e gerenciamento completo de arquivos médicos.
        
        ## Recursos:
        - Upload multipart (PDF, imagens, documentos)
        - Armazenamento seguro em S3
        - Pseudonimização para LGPD/GDPR
        - Download e deleção
        - Logs de auditoria
        """
)
public class FileController {

    private final StorageService storageService;
    private final AnonymizationLogService anonymizationLogService;
    private final SecurityHelper securityHelper;

    @Operation(
        summary = "Health Check",
        description = "Endpoint público para verificar se o serviço está online."
    )
    @ApiResponse(responseCode = "200", description = "✅ Serviço saudável")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }

    @Operation(
        summary = "Upload de arquivo",
        description = """
            # 📤 Upload de Arquivo Médico
            
            Upload de arquivo (PDF, imagem, documento) com armazenamento em S3 e publicação de evento.
            
            ## 🔒 Requer: CLINICIAN ou ADMIN
            
            ## Processo:
            1. Validação do arquivo
            2. Cálculo de hash SHA-256
            3. Upload para S3
            4. Salvar metadata no banco
            5. Publicar evento no RabbitMQ
            
            ## Tipos Suportados:
            - MEDICAL_REPORT - Laudos médicos
            - PRESCRIPTION - Prescrições
            - IMAGE - Imagens (raio-x, etc.)
            - OTHER - Outros documentos
            
            ## Exemplo cURL:
            ```bash
            curl -X POST http://localhost:8085/files/upload \\
              -H "Authorization: Bearer <token>" \\
              -F "file=@laudo.pdf" \\
              -F "userId=550e8400-e29b-41d4-a716-446655440000" \\
              -F "fileType=MEDICAL_REPORT"
            ```
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Upload realizado com sucesso"),
        @ApiResponse(responseCode = "403", description = "🔒 Acesso negado - Requer CLINICIAN"),
        @ApiResponse(responseCode = "400", description = "❌ Arquivo inválido")
    })
    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<IngestionFile> upload(
            @Parameter(description = "Arquivo a ser enviado", required = true)
            @RequestPart("file") MultipartFile file,

            @Parameter(description = "UUID do usuário/paciente", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam("userId") UUID userId,

            @Parameter(
                description = "Tipo do arquivo",
                example = "MEDICAL_REPORT",
                schema = @Schema(allowableValues = {"MEDICAL_REPORT", "PRESCRIPTION", "IMAGE", "OTHER"})
            )
            @RequestParam(value = "fileType", required = false, defaultValue = "OTHER") FileType fileType) throws IOException {
        securityHelper.requireClinician();
        IngestionFile saved = storageService.upload(file, userId, fileType);
        return ResponseEntity.ok(saved);
    }

    @Operation(
        summary = "Pseudonimizar arquivo",
        description = """
            # 🔒 Pseudonimização (LGPD/GDPR)
            
            Remove dados identificáveis do arquivo para compliance com LGPD e GDPR.
            
            ## 🔒 Requer: CLINICIAN ou ADMIN
            
            ## Processo:
            1. Recupera arquivo do S3
            2. Remove metadados EXIF (imagens)
            3. Remove propriedades do documento (PDFs)
            4. Atualiza status para PSEUDONYMIZED
            5. Registra log de auditoria
            
            **Nota:** Este endpoint não recebe body, apenas o ID no path.
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Arquivo pseudonimizado"),
        @ApiResponse(responseCode = "403", description = "🔒 Acesso negado"),
        @ApiResponse(responseCode = "404", description = "❌ Arquivo não encontrado")
    })
    @PostMapping("/{id}/pseudonymize")
    public ResponseEntity<IngestionFile> pseudonymize(
            @Parameter(description = "UUID do arquivo", required = true) @PathVariable UUID id) {
        securityHelper.requireClinician();
        IngestionFile updated = storageService.pseudonymize(id);
        return ResponseEntity.ok(updated);
    }

    @Operation(
        summary = "Listar logs de pseudonimização",
        description = "🔒 **CLINICIAN/ADMIN** - Retorna histórico de pseudonimização do arquivo.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Lista de logs")
    @GetMapping("/{id}/anonymization-logs")
    public ResponseEntity<List<AnonymizationLog>> listAnonymizationLogs(
            @Parameter(description = "UUID do arquivo") @PathVariable UUID id) {
        securityHelper.requireClinician();
        return ResponseEntity.ok(anonymizationLogService.listByFile(id));
    }

    @Operation(
        summary = "Buscar arquivo por ID",
        description = "Retorna metadata do arquivo (sem o conteúdo binário).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Arquivo encontrado"),
        @ApiResponse(responseCode = "403", description = "🔒 Acesso negado"),
        @ApiResponse(responseCode = "404", description = "❌ Arquivo não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<IngestionFile> get(
            @Parameter(description = "UUID do arquivo") @PathVariable UUID id) {
        IngestionFile f = storageService.get(id);
        securityHelper.validateResourceAccess(f.getUserId());
        return ResponseEntity.ok(f);
    }

    @Operation(
        summary = "Listar arquivos",
        description = """
            # 📋 Listar Arquivos
            
            Lista arquivos com filtros opcionais.
            
            ## Regras de Acesso:
            - **PATIENT**: Apenas próprios arquivos
            - **CLINICIAN/ADMIN**: Requer especificar userId
            
            ## Filtros:
            - `userId` - Filtrar por usuário (obrigatório para CLINICIAN/ADMIN)
            - `status` - Filtrar por status (UPLOADED, PSEUDONYMIZED, etc.)
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Lista de arquivos")
    @GetMapping
    public ResponseEntity<List<IngestionFile>> list(
            @Parameter(description = "UUID do usuário") @RequestParam(required = false) UUID userId,
            @Parameter(
                description = "Status do arquivo",
                schema = @Schema(allowableValues = {"UPLOADED", "PSEUDONYMIZED", "PROCESSING", "READY", "ERROR"})
            )
            @RequestParam(required = false) FileStatus status) {
        UUID authenticatedUserId = securityHelper.getAuthenticatedUserId();
        boolean isStaff = securityHelper.hasAnyRole("ADMIN", "CLINICIAN");

        if (!isStaff) {
            if (userId != null && !userId.equals(authenticatedUserId)) {
                throw new IllegalArgumentException("Access denied: You can only list your own files");
            }
            userId = authenticatedUserId;
        } else {
            if (userId == null) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok(storageService.list(userId, status));
    }

    @Operation(
        summary = "Download de arquivo",
        description = """
            # 📥 Download de Arquivo
            
            Recupera o conteúdo binário do arquivo do S3.
            
            ## Retorna:
            - Content-Type: application/octet-stream
            - Content-Disposition: attachment
            - Bytes do arquivo
            
            ## Acesso:
            - Dono do arquivo
            - ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Download iniciado"),
        @ApiResponse(responseCode = "403", description = "🔒 Acesso negado"),
        @ApiResponse(responseCode = "404", description = "❌ Arquivo não encontrado")
    })
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(
            @Parameter(description = "UUID do arquivo") @PathVariable UUID id) throws IOException {
        IngestionFile f = storageService.get(id);
        securityHelper.validateResourceAccess(f.getUserId());

        byte[] data = storageService.download(id);
        String filename = f.getOriginalName() != null ? f.getOriginalName() : "file";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(data);
    }

    @Operation(
        summary = "Visualizar arquivo inline",
        description = """
            # 👁️ Visualizar Arquivo (Inline)
            
            Retorna o arquivo para visualização inline no navegador (sem download).
            
            ## Ideal para:
            - PDFs - Visualização no navegador
            - Imagens - Exibição direta
            - Documentos - Preview
            
            ## Retorna:
            - Content-Type: Tipo MIME apropriado (application/pdf, image/jpeg, etc.)
            - Content-Disposition: inline
            - Bytes do arquivo
            
            ## Acesso:
            - Dono do arquivo
            - CLINICIAN
            - ADMIN
            
            ## Exemplo de uso no front-end:
            ```html
            <iframe src="/files/{id}/view" width="100%" height="600px"></iframe>
            <img src="/files/{id}/view" alt="Medical Image" />
            ```
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Arquivo retornado para visualização"),
        @ApiResponse(responseCode = "403", description = "🔒 Acesso negado"),
        @ApiResponse(responseCode = "404", description = "❌ Arquivo não encontrado")
    })
    @GetMapping("/{id}/view")
    public ResponseEntity<byte[]> view(
            @Parameter(description = "UUID do arquivo") @PathVariable UUID id) throws IOException {
        IngestionFile f = storageService.get(id);
        securityHelper.validateResourceAccess(f.getUserId());

        byte[] data = storageService.download(id);
        String filename = f.getOriginalName() != null ? f.getOriginalName() : "file";

        MediaType contentType = determineContentType(filename);

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(data);
    }


    private MediaType determineContentType(String filename) {
        if (filename == null) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF;
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (lower.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        } else if (lower.endsWith(".bmp")) {
            return MediaType.parseMediaType("image/bmp");
        } else if (lower.endsWith(".tiff") || lower.endsWith(".tif")) {
            return MediaType.parseMediaType("image/tiff");
        } else if (lower.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        } else if (lower.endsWith(".svg")) {
            return MediaType.parseMediaType("image/svg+xml");
        } else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    @Operation(
        summary = "Deletar arquivo",
        description = """
            # 🗑️ Deletar Arquivo
            
            Remove arquivo do S3 e metadata do banco de dados.
            
            ## Acesso:
            - Dono do arquivo
            - ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "✅ Arquivo deletado"),
        @ApiResponse(responseCode = "403", description = "🔒 Acesso negado"),
        @ApiResponse(responseCode = "404", description = "❌ Arquivo não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID do arquivo") @PathVariable UUID id) {
        IngestionFile f = storageService.get(id);
        securityHelper.validateResourceAccess(f.getUserId());
        storageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

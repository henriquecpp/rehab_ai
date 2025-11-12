package com.rehabai.user_service.controller;

import com.rehabai.user_service.security.SecurityHelper;
import com.rehabai.user_service.dto.ConsentDtos;
import com.rehabai.user_service.service.ConsentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for user consent management (LGPD/GDPR compliance).
 * Security handled by API Gateway (validates JWT and injects headers).
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(
    name = "Consent Management",
    description = """
        # 📜 Gerenciamento de Consentimentos (LGPD/GDPR)
        
        Gerenciamento completo de consentimentos de usuários para conformidade com LGPD e GDPR.
        
        ## Tipos de Consentimento:
        - **data_processing**: Consentimento para processamento de dados
        - **data_sharing**: Compartilhamento de dados com terceiros
        - **marketing**: Comunicações de marketing
        - **analytics**: Uso de dados para análises
        
        ## Duas Formas de Uso:
        
        ### 1. Por userId (Admin/Sistema)
        - `/users/{userId}/consents` - Gerenciar consents de qualquer usuário
        - Requer validação de acesso
        
        ### 2. Para Usuário Autenticado (/me)
        - `/users/me/consents` - Gerenciar próprios consents
        - Usa X-User-Id do JWT automaticamente
        """
)
public class ConsentController {

    private final ConsentService service;
    private final SecurityHelper securityHelper;

    @Operation(
        summary = "Criar consentimento",
        description = "Registra novo consentimento para usuário específico.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "✅ Consentimento criado"),
        @ApiResponse(responseCode = "403", description = "🔒 Acesso negado")
    })
    @PostMapping("/users/{userId}/consents")
    public ResponseEntity<ConsentDtos.Response> create(
            @Parameter(description = "UUID do usuário") @PathVariable UUID userId,
            @Valid @RequestBody ConsentDtos.CreateRequest req) {
        // Validate access: user can only manage their own consents
        securityHelper.validateResourceAccess(userId);
        ConsentDtos.Response created = service.create(userId, req);
        return ResponseEntity.created(URI.create("/users/" + userId + "/consents/" + created.id())).body(created);
    }

    @Operation(
        summary = "Revogar consentimento",
        description = "Revoga um consentimento específico (muda granted para false).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Consentimento revogado")
    @PostMapping("/users/{userId}/consents/revoke")
    public ResponseEntity<ConsentDtos.Response> revoke(
            @Parameter(description = "UUID do usuário") @PathVariable UUID userId,
            @Valid @RequestBody ConsentDtos.RevokeRequest req) {
        // Validate access: user can only revoke their own consents
        securityHelper.validateResourceAccess(userId);
        ConsentDtos.Response revoked = service.revoke(userId, req);
        return ResponseEntity.ok(revoked);
    }

    @Operation(
        summary = "Listar consentimentos",
        description = "Lista todos os consentimentos do usuário, com filtro opcional por tipo.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Lista de consentimentos")
    @GetMapping("/users/{userId}/consents")
    public ResponseEntity<List<ConsentDtos.Response>> list(
            @Parameter(description = "UUID do usuário") @PathVariable UUID userId,
            @Parameter(description = "Filtrar por tipo de consentimento") @RequestParam(required = false) String type) {
        // Validate access: user can only see their own consents
        securityHelper.validateResourceAccess(userId);
        return ResponseEntity.ok(service.listByUser(userId, type));
    }

    @Operation(
        summary = "Buscar consentimento mais recente",
        description = "Retorna o consentimento mais recente de um tipo específico.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Consentimento encontrado")
    @GetMapping("/users/{userId}/consents/latest")
    public ResponseEntity<ConsentDtos.Response> latest(
            @Parameter(description = "UUID do usuário") @PathVariable UUID userId,
            @Parameter(description = "Tipo de consentimento", required = true) @RequestParam String type) {
        // Validate access: user can only see their own consents
        securityHelper.validateResourceAccess(userId);
        return ResponseEntity.ok(service.latestByType(userId, type));
    }

    @Operation(
        summary = "Criar consentimento (próprio usuário)",
        description = "Registra novo consentimento para o usuário autenticado.",
        security = @SecurityRequirement(name = "bearerAuth"),
        tags = {"My Consents"}
    )
    @ApiResponse(responseCode = "201", description = "✅ Consentimento criado")
    @PostMapping("/users/me/consents")
    public ResponseEntity<ConsentDtos.Response> createForMe(@Valid @RequestBody ConsentDtos.CreateRequest req) {
        UUID userId = securityHelper.getAuthenticatedUserId();
        ConsentDtos.Response created = service.create(userId, req);
        return ResponseEntity.created(URI.create("/users/me/consents/" + created.id())).body(created);
    }

    @Operation(
        summary = "Revogar consentimento (próprio usuário)",
        description = "Revoga consentimento do usuário autenticado.",
        security = @SecurityRequirement(name = "bearerAuth"),
        tags = {"My Consents"}
    )
    @ApiResponse(responseCode = "200", description = "✅ Consentimento revogado")
    @PostMapping("/users/me/consents/revoke")
    public ResponseEntity<ConsentDtos.Response> revokeForMe(@Valid @RequestBody ConsentDtos.RevokeRequest req) {
        UUID userId = securityHelper.getAuthenticatedUserId();
        return ResponseEntity.ok(service.revoke(userId, req));
    }

    @Operation(
        summary = "Listar consentimentos (próprio usuário)",
        description = "Lista consentimentos do usuário autenticado.",
        security = @SecurityRequirement(name = "bearerAuth"),
        tags = {"My Consents"}
    )
    @ApiResponse(responseCode = "200", description = "✅ Lista de consentimentos")
    @GetMapping("/users/me/consents")
    public ResponseEntity<List<ConsentDtos.Response>> listForMe(
            @Parameter(description = "Filtrar por tipo") @RequestParam(required = false) String type) {
        UUID userId = securityHelper.getAuthenticatedUserId();
        return ResponseEntity.ok(service.listByUser(userId, type));
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "bad_request";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (msg.startsWith("user_not_found") || msg.equals("consent_not_found")) {
            status = HttpStatus.NOT_FOUND;
        } else if (msg.equals("type_required")) {
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity.status(status).body(Map.of("error", msg));
    }
}

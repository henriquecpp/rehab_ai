package com.rehabai.auth_service.controller;

import com.rehabai.auth_service.model.OAuthClient;
import com.rehabai.auth_service.repository.OAuthClientRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/oauth/clients")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(
    name = "OAuth Client Admin",
    description = """
        # 🔐 Gerenciamento de Clientes OAuth (ADMIN ONLY)
        
        CRUD completo de clientes OAuth para integração externa.
        
        ## 🔒 Requer: ADMIN apenas
        
        Gerenciar aplicações externas que podem se autenticar via OAuth 2.0.
        
        ## Recursos:
        - Client ID e Client Secret
        - Scopes (permissões)
        - Grant Types (authorization_code, client_credentials, etc.)
        - Redirect URIs para callbacks
        """
)
public class OAuthClientAdminController {

    private final OAuthClientRepository repo;

    @Operation(
        summary = "Listar todos os OAuth clients",
        description = "🔒 **ADMIN** - Lista todos os clientes OAuth cadastrados no sistema",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "✅ Lista de clientes OAuth")
    @GetMapping
    public ResponseEntity<List<OAuthClient>> list() {
        return ResponseEntity.ok(repo.findAll());
    }

    @Operation(
        summary = "Buscar OAuth client por ID",
        description = "🔒 **ADMIN** - Retorna detalhes de um cliente OAuth específico",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "❌ Cliente não encontrado")
    })
    @GetMapping("/{clientId}")
    public ResponseEntity<OAuthClient> get(
            @Parameter(description = "Client ID do OAuth client", example = "mobile-app-v1")
            @PathVariable String clientId) {
        return repo.findById(clientId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Criar novo OAuth client",
        description = """
            # 🔒 **ADMIN** - Criar Cliente OAuth
            
            Registra nova aplicação externa para autenticação OAuth 2.0.
            
            ## Grant Types Suportados:
            - `authorization_code` - Para aplicações web
            - `client_credentials` - Para serviços backend
            - `refresh_token` - Para renovar tokens
            
            ## Scopes Comuns:
            - `read` - Leitura de dados
            - `write` - Escrita de dados
            - `admin` - Acesso administrativo
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "✅ Cliente OAuth criado"),
        @ApiResponse(responseCode = "409", description = "❌ Client ID já existe")
    })
    @PostMapping
    public ResponseEntity<OAuthClient> create(@Valid @RequestBody OAuthClient client) {
        if (repo.existsById(client.getClientId())) {
            return ResponseEntity.status(409).build();
        }
        OAuthClient saved = repo.save(client);
        return ResponseEntity.created(URI.create("/admin/oauth/clients/" + saved.getClientId())).body(saved);
    }

    @Operation(
        summary = "Atualizar OAuth client",
        description = "🔒 **ADMIN** - Atualiza configurações de um cliente OAuth (secret, scopes, URIs)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Cliente atualizado"),
        @ApiResponse(responseCode = "404", description = "❌ Cliente não encontrado")
    })
    @PutMapping("/{clientId}")
    public ResponseEntity<OAuthClient> update(
            @Parameter(description = "Client ID") @PathVariable String clientId,
            @Valid @RequestBody OAuthClient update) {
        return repo.findById(clientId).map(existing -> {
            existing.setClientSecret(update.getClientSecret());
            existing.setScopes(update.getScopes());
            existing.setGrantTypes(update.getGrantTypes());
            existing.setRedirectUris(update.getRedirectUris());
            return ResponseEntity.ok(repo.save(existing));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Deletar OAuth client",
        description = "🔒 **ADMIN** - Remove um cliente OAuth do sistema",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "✅ Cliente deletado"),
        @ApiResponse(responseCode = "404", description = "❌ Cliente não encontrado")
    })
    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Client ID") @PathVariable String clientId) {
        if (!repo.existsById(clientId)) return ResponseEntity.notFound().build();
        repo.deleteById(clientId);
        return ResponseEntity.noContent().build();
    }
}

package com.rehabai.user_service.controller;

import com.rehabai.user_service.security.SecurityHelper;
import com.rehabai.user_service.dto.UserDtos;
import com.rehabai.user_service.model.UserRole;
import com.rehabai.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
@Tag(
    name = "User Management",
    description = """
        # 👥 Gerenciamento de Usuários
        
        CRUD completo de usuários do sistema, incluindo operações de ativação/desativação,
        troca de senha e role.
        
        ## Tipos de Usuários:
        - **ADMIN**: Administrador do sistema
        - **CLINICIAN**: Profissional de saúde (fisioterapeuta, médico)
        - **PATIENT**: Paciente
        
        ## Permissões:
        - Criar usuário: Qualquer pessoa (via auth-service)
        - Listar/Buscar: CLINICIAN ou ADMIN
        - Atualizar/Deletar: ADMIN apenas
        - Ativar/Desativar: ADMIN apenas
        - Trocar Role/Senha: ADMIN apenas
        """
)
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService service;
    private final SecurityHelper securityHelper;

    @Operation(
        summary = "Health Check",
        description = """
            # 🏥 Verificação de Saúde do Serviço
            
            Endpoint público para verificar se o serviço está online e respondendo.
            
            ## Uso:
            - Monitoramento de disponibilidade
            - Health checks de containers
            - Load balancer health probes
            
            ## Não Requer Autenticação
            
            Este é o único endpoint público do user-service.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "✅ Serviço está saudável",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(value = "ok")
            )
        )
    })
    @GetMapping("/users/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }

    @Operation(
        summary = "Criar novo usuário",
        description = """
            # ➕ Criar Usuário
            
            Cria um novo usuário no sistema com email, nome completo, senha hash e role.
            
            ## ⚠️ Uso Interno
            Este endpoint é tipicamente chamado pelo **auth-service** durante o registro.
            O `passwordHash` deve ser o hash BCrypt da senha, não a senha em texto puro.
            
            ## Validações:
            - Email deve ser único
            - Email deve ser válido
            - Nome completo obrigatório
            - Password hash obrigatório
            
            ## Retorno:
            - Dados do usuário criado (sem password hash)
            - Location header com URI do recurso
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "✅ Usuário criado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDtos.Response.class),
                examples = @ExampleObject(
                    name = "Sucesso",
                    value = """
                        {
                          "id": "550e8400-e29b-41d4-a716-446655440000",
                          "email": "joao.silva@example.com",
                          "fullName": "João Silva",
                          "role": "PATIENT",
                          "active": true
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "❌ Dados inválidos ou email já cadastrado"
        )
    })
    @PostMapping("/users")
    public ResponseEntity<UserDtos.Response> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados do novo usuário",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = UserDtos.CreateRequest.class),
                    examples = {
                        @ExampleObject(
                            name = "Patient",
                            description = "Criar paciente",
                            value = """
                                {
                                  "email": "maria.santos@example.com",
                                  "fullName": "Maria Santos",
                                  "passwordHash": "$2a$10$N9qo8uLOickgx2ZMRZoMye/7VgMHv.XCwPPMz.PGXzZ6zb4iVmZqK",
                                  "role": "PATIENT"
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "Clinician",
                            description = "Criar profissional",
                            value = """
                                {
                                  "email": "dr.joao@clinic.com",
                                  "fullName": "Dr. João Silva",
                                  "passwordHash": "$2a$10$N9qo8uLOickgx2ZMRZoMye/7VgMHv.XCwPPMz.PGXzZ6zb4iVmZqK",
                                  "role": "CLINICIAN"
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "Admin",
                            description = "Criar administrador",
                            value = """
                                {
                                  "email": "admin@rehabai.com",
                                  "fullName": "Admin System",
                                  "passwordHash": "$2a$10$N9qo8uLOickgx2ZMRZoMye/7VgMHv.XCwPPMz.PGXzZ6zb4iVmZqK",
                                  "role": "ADMIN"
                                }
                                """
                        )
                    }
                )
            )
            @Valid @RequestBody UserDtos.CreateRequest req) {
        UserDtos.Response created = service.create(req);
        return ResponseEntity.created(URI.create("/users/" + created.id())).body(created);
    }

    @Operation(
        summary = "Listar usuários",
        description = """
            # 📋 Listar Todos os Usuários
            
            Lista usuários do sistema com filtros opcionais.
            
            ## 🔒 Requer: CLINICIAN ou ADMIN
            
            ## Filtros Disponíveis:
            
            ### Por Role:
            - `?role=PATIENT` - Apenas pacientes
            - `?role=CLINICIAN` - Apenas profissionais
            - `?role=ADMIN` - Apenas administradores
            
            ### Por Status:
            - `?activeOnly=true` - Apenas usuários ativos
            - `?activeOnly=false` - Todos (ativos e inativos)
            
            ### Sem Filtros:
            - Retorna todos os usuários
            
            ## Exemplos:
            
            ```bash
            # Todos os usuários
            GET /users
            
            # Apenas pacientes
            GET /users?role=PATIENT
            
            # Apenas ativos
            GET /users?activeOnly=true
            
            # Pacientes ativos
            GET /users?role=PATIENT&activeOnly=true
            ```
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "✅ Lista de usuários retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDtos.Response.class),
                examples = @ExampleObject(
                    name = "Lista de Usuários",
                    value = """
                        [
                          {
                            "id": "550e8400-e29b-41d4-a716-446655440000",
                            "email": "maria@example.com",
                            "fullName": "Maria Santos",
                            "role": "PATIENT",
                            "active": true
                          },
                          {
                            "id": "660e8400-e29b-41d4-a716-446655440000",
                            "email": "dr.joao@clinic.com",
                            "fullName": "Dr. João Silva",
                            "role": "CLINICIAN",
                            "active": true
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "🔒 Acesso negado - Requer CLINICIAN ou ADMIN"
        )
    })
    @GetMapping("/users")
    public ResponseEntity<List<UserDtos.Response>> list(
            @Parameter(
                description = "Filtrar por role (PATIENT, CLINICIAN, ADMIN)",
                example = "PATIENT"
            )
            @RequestParam(required = false) UserRole role,

            @Parameter(
                description = "Retornar apenas usuários ativos",
                example = "true"
            )
            @RequestParam(required = false) Boolean activeOnly) {

        log.info("Parâmetros: role={}, activeOnly={}", role, activeOnly);

        try {
            log.info("Verificando permissões...");
            boolean hasPermission = securityHelper.hasAnyRole("CLINICIAN", "ADMIN");
            log.info("Resultado da verificação de permissão: {}", hasPermission);

            if (!hasPermission) {
                log.warn("Acesso negado - usuário não tem role CLINICIAN ou ADMIN");
                throw new IllegalArgumentException("Access denied: CLINICIAN or ADMIN role required");
            }

            log.info("Permissão concedida, buscando usuários...");
            if (role != null) {
                return ResponseEntity.ok(service.listByRole(role));
            }
            if (Boolean.TRUE.equals(activeOnly)) {
                return ResponseEntity.ok(service.listActive());
            }
            List<UserDtos.Response> users = service.list();
            log.info("Retornando {} usuários", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("ERRO ao listar usuários: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(
        summary = "Buscar usuário por ID",
        description = "Retorna dados de um usuário específico pelo UUID.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Usuário encontrado"),
        @ApiResponse(responseCode = "404", description = "❌ Usuário não encontrado")
    })
    @GetMapping("/users/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
    public ResponseEntity<UserDtos.Response> get(
            @Parameter(description = "UUID do usuário", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @Operation(
        summary = "Buscar usuário por email",
        description = "Retorna dados de um usuário específico pelo endereço de email.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Usuário encontrado"),
        @ApiResponse(responseCode = "404", description = "❌ Usuário não encontrado")
    })
    @GetMapping("/users/email/{email:.+@.+\\..+}")
    public ResponseEntity<UserDtos.Response> getByEmail(
            @Parameter(description = "Email do usuário", example = "maria@example.com")
            @PathVariable String email) {
        return ResponseEntity.ok(service.getByEmail(email));
    }

    @Operation(
        summary = "Atualizar usuário",
        description = "🔒 **ADMIN apenas** - Atualiza dados do usuário (nome, role, status ativo).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Usuário atualizado"),
        @ApiResponse(responseCode = "403", description = "🔒 Acesso negado - Requer ADMIN"),
        @ApiResponse(responseCode = "404", description = "❌ Usuário não encontrado")
    })
    @PutMapping("/users/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
    public ResponseEntity<UserDtos.Response> update(
            @Parameter(description = "UUID do usuário") @PathVariable UUID id,
            @Valid @RequestBody UserDtos.UpdateRequest req) {
        securityHelper.requireAdmin();
        return ResponseEntity.ok(service.update(id, req));
    }

    @Operation(
        summary = "Deletar usuário",
        description = "🔒 **ADMIN apenas** - Remove usuário do sistema (delete físico).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "✅ Usuário deletado"),
        @ApiResponse(responseCode = "403", description = "🔒 Acesso negado - Requer ADMIN"),
        @ApiResponse(responseCode = "404", description = "❌ Usuário não encontrado")
    })
    @DeleteMapping("/users/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID do usuário") @PathVariable UUID id) {
        securityHelper.requireAdmin();
        UUID authenticatedUserId = securityHelper.getAuthenticatedUserId();
        service.delete(id, authenticatedUserId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "🔒 INTERNO - Obter credenciais por email",
        description = "⚠️ **Endpoint interno** usado pelo auth-service para autenticação. Retorna passwordHash.",
        tags = {"Internal APIs"}
    )
    @ApiResponse(responseCode = "200", description = "✅ Credenciais encontradas")
    @GetMapping("/internal/users/credentials")
    public ResponseEntity<UserDtos.CredentialsResponse> getCredentials(
            @Parameter(description = "Email do usuário") @Email @RequestParam String email) {
        return ResponseEntity.ok(service.getCredentialsByEmail(email));
    }

    @Operation(
        summary = "🔒 INTERNO - Contar total de usuários",
        description = "⚠️ **Endpoint interno** - Retorna contagem total de usuários no sistema.",
        tags = {"Internal APIs"}
    )
    @ApiResponse(responseCode = "200", description = "✅ Contagem retornada")
    @GetMapping("/internal/users/count")
    public ResponseEntity<Long> countUsers() {
        return ResponseEntity.ok(service.count());
    }

    @Operation(
        summary = "🔒 INTERNO - Verificar se existe admin",
        description = "⚠️ **Endpoint interno** usado pelo auth-service para verificar bootstrap de admin.",
        tags = {"Internal APIs"}
    )
    @ApiResponse(responseCode = "200", description = "✅ Boolean retornado")
    @GetMapping("/internal/users/any-admin")
    public ResponseEntity<Boolean> anyAdmin() {
        return ResponseEntity.ok(service.anyAdminExists());
    }

    @Operation(
        summary = "Ativar usuário",
        description = "🔒 **ADMIN apenas** - Marca usuário como ativo (pode fazer login).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Usuário ativado"),
        @ApiResponse(responseCode = "403", description = "🔒 Acesso negado - Requer ADMIN")
    })
    @PostMapping("/users/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/activate")
    public ResponseEntity<UserDtos.Response> activate(
            @Parameter(description = "UUID do usuário") @PathVariable UUID id) {
        securityHelper.requireAdmin();
        return ResponseEntity.ok(service.activate(id));
    }

    @Operation(
        summary = "Desativar usuário",
        description = "🔒 **ADMIN apenas** - Marca usuário como inativo (não pode fazer login).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Usuário desativado"),
        @ApiResponse(responseCode = "403", description = "🔒 Acesso negado - Requer ADMIN")
    })
    @PostMapping("/users/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/deactivate")
    public ResponseEntity<UserDtos.Response> deactivate(
            @Parameter(description = "UUID do usuário") @PathVariable UUID id) {
        securityHelper.requireAdmin();
        return ResponseEntity.ok(service.deactivate(id));
    }

    @Operation(
        summary = "Trocar role do usuário",
        description = "🔒 **ADMIN apenas** - Altera a role/perfil do usuário (PATIENT, CLINICIAN, ADMIN).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Role alterada"),
        @ApiResponse(responseCode = "403", description = "🔒 Acesso negado - Requer ADMIN")
    })
    @PostMapping("/users/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/role")
    public ResponseEntity<UserDtos.Response> changeRole(
            @Parameter(description = "UUID do usuário") @PathVariable UUID id,
            @Valid @RequestBody UserDtos.ChangeRoleRequest req) {
        securityHelper.requireAdmin();
        return ResponseEntity.ok(service.changeRole(id, req.role()));
    }

    @Operation(
        summary = "Trocar senha do usuário",
        description = "🔒 **ADMIN apenas** - Atualiza o hash da senha do usuário.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Senha alterada"),
        @ApiResponse(responseCode = "403", description = "🔒 Acesso negado - Requer ADMIN")
    })
    @PostMapping("/users/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/password")
    public ResponseEntity<UserDtos.Response> changePassword(
            @Parameter(description = "UUID do usuário") @PathVariable UUID id,
            @Valid @RequestBody UserDtos.ChangePasswordRequest req) {
        securityHelper.requireAdmin();
        return ResponseEntity.ok(service.changePassword(id, req.passwordHash()));
    }
}

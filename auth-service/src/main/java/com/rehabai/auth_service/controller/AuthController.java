package com.rehabai.auth_service.controller;

import com.rehabai.auth_service.dto.AuthResponse;
import com.rehabai.auth_service.dto.LoginRequest;
import com.rehabai.auth_service.dto.RefreshRequest;
import com.rehabai.auth_service.dto.RegisterRequest;
import com.rehabai.auth_service.dto.LogoutRequest;
import com.rehabai.auth_service.security.JwtUtil;
import com.rehabai.auth_service.service.RefreshTokenService;
import com.rehabai.auth_service.service.UserService;
import com.rehabai.auth_service.service.UserServiceClient;
import com.rehabai.auth_service.model.UserRole;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(
    name = "Authentication",
    description = """
        # 🔐 Endpoints de Autenticação e Autorização
        
        Gerenciamento completo de autenticação JWT, incluindo registro, login, 
        refresh de tokens e logout.
        
        ## Fluxo de Autenticação
        
        1. **Registro**: Crie uma conta com `/auth/register`
        2. **Login**: Autentique com `/auth/login`
        3. **Usar API**: Use o `accessToken` em outros endpoints
        4. **Renovar**: Quando expirar, use `/auth/refresh`
        5. **Logout**: Revogue tokens com `/auth/logout`
        """
)
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Operation(
        summary = "Registrar novo usuário",
        description = """
            # 📝 Registro de Novo Usuário
            
            Cria uma nova conta no sistema com email, senha e role.
            
            ## Regras de Criação de Roles:
            
            - **PATIENT**: Qualquer pessoa pode criar (não requer autenticação)
            - **CLINICIAN**: Qualquer pessoa pode criar (não requer autenticação)
            - **ADMIN**: Requer ser ADMIN ou ser o primeiro usuário do sistema (bootstrap)
            
            ## Validações:
            
            - Email deve ser único
            - Senha deve ter mínimo 6 caracteres
            - Nome completo obrigatório
            
            ## Retorno:
            
            - `accessToken`: Token JWT para autenticação (válido por 1 hora)
            - `refreshToken`: Token para renovação (válido por 7 dias)
            
            ## Exemplo de Uso:
            
            ```bash
            curl -X POST http://localhost:8081/auth/register \\
              -H "Content-Type: application/json" \\
              -d '{
                "fullName": "João Silva",
                "email": "joao@example.com",
                "password": "senha123",
                "role": "PATIENT"
              }'
            ```
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "✅ Usuário registrado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Sucesso",
                    value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "tokenType": "Bearer",
                          "expiresIn": 3600000,
                          "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
                          "refreshExpiresIn": 604800000
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "❌ Dados inválidos ou email já cadastrado",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Email Duplicado",
                        value = """
                            {
                              "error": "email_already_exists",
                              "message": "Email já cadastrado"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Dados Inválidos",
                        value = """
                            {
                              "error": "invalid_data",
                              "message": "Senha deve ter no mínimo 6 caracteres"
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "🔒 Não autorizado a criar ADMIN",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "\"admin_only\""
                )
            )
        )
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados do novo usuário",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = RegisterRequest.class),
                    examples = {
                        @ExampleObject(
                            name = "Patient",
                            description = "Registro de paciente",
                            value = """
                                {
                                  "fullName": "Maria Santos",
                                  "email": "maria@example.com",
                                  "password": "senha123",
                                  "role": "PATIENT"
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "Clinician",
                            description = "Registro de profissional de saúde",
                            value = """
                                {
                                  "fullName": "Dr. João Silva",
                                  "email": "joao.silva@clinic.com",
                                  "password": "senha456",
                                  "role": "CLINICIAN"
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "Admin",
                            description = "Registro de administrador (requer permissão)",
                            value = """
                                {
                                  "fullName": "Admin System",
                                  "email": "admin@rehabai.com",
                                  "password": "admin123",
                                  "role": "ADMIN"
                                }
                                """
                        )
                    }
                )
            )
            @Valid @RequestBody RegisterRequest req,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(hidden = true) Authentication authentication) {
        try {
            // Enforce role creation rules
            if (req.role() == UserRole.ADMIN) {
                boolean bootstrap = userService.noAdminsExist();
                boolean callerIsAdmin = authentication != null && authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                if (!bootstrap && !callerIsAdmin) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("admin_only");
                }
            }

            userService.registerNewUser(req);
            UserDetails ud = userService.loadUserByUsername(req.email());

            UserServiceClient.CredentialsResponse creds = userService.getCredentialsByEmail(req.email());
            String token = jwtUtil.generateToken(ud, Map.of("user_id", creds.id().toString()));
            long expiresIn = jwtUtil.getExpirationMs();

            var rt = refreshTokenService.issueForUser(creds.id());
            long refreshExpiresIn = refreshTokenService.getRefreshExpirationMs();

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponse(token, "Bearer", expiresIn, rt.getTokenId().toString(), refreshExpiresIn));
        } catch (org.springframework.web.client.HttpClientErrorException.BadRequest ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "email_already_exists", "message", "Email já cadastrado"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(AuthController.class)
                .error("Unexpected error during registration for {}", req.email(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "internal_error", "message", "An unexpected error occurred"));
        }
    }

    @Operation(
        summary = "Login no sistema",
        description = """
            # 🔓 Autenticação de Usuário
            
            Autentica um usuário existente e retorna tokens JWT.
            
            ## Como Funciona:
            
            1. Valida email e senha
            2. Gera `accessToken` JWT (válido por 1 hora)
            3. Gera `refreshToken` UUID (válido por 7 dias)
            4. Retorna ambos os tokens
            
            ## Usar o Access Token:
            
            Inclua o token no header de todas as requisições autenticadas:
            
            ```
            Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
            ```
            
            ## Renovar Token Expirado:
            
            Quando o `accessToken` expirar (após 1 hora), use `/auth/refresh` 
            com o `refreshToken` para obter novos tokens.
            
            ## Exemplo de Uso:
            
            ```bash
            curl -X POST http://localhost:8081/auth/login \\
              -H "Content-Type: application/json" \\
              -d '{
                "email": "joao@example.com",
                "password": "senha123"
              }'
            ```
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "✅ Login realizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Sucesso",
                    value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2FvQGV4YW1wbGUuY29tIiwidXNlcl9pZCI6IjU1MGU4NDAwLWUyOWItNDFkNC1hNzE2LTQ0NjY1NTQ0MDAwMCIsInJvbGVzIjpbIlJPTEVfUEFUSUVOVCJdLCJpYXQiOjE2OTk1MzYwMDAsImV4cCI6MTY5OTUzOTYwMH0.abc123",
                          "tokenType": "Bearer",
                          "expiresIn": 3600000,
                          "refreshToken": "660e8400-e29b-41d4-a716-446655440000",
                          "refreshExpiresIn": 604800000
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "❌ Credenciais inválidas",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "\"invalid_credentials\""
                )
            )
        ),
        @ApiResponse(
            responseCode = "503",
            description = "⚠️ Serviço temporariamente indisponível",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "service_unavailable",
                          "message": "Authentication service temporarily unavailable"
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Credenciais de login (email e senha)",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = {
                        @ExampleObject(
                            name = "Patient",
                            description = "Login de paciente",
                            value = """
                                {
                                  "email": "maria@example.com",
                                  "password": "senha123"
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "Clinician",
                            description = "Login de profissional",
                            value = """
                                {
                                  "email": "joao.silva@clinic.com",
                                  "password": "senha456"
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "Admin",
                            description = "Login de administrador",
                            value = """
                                {
                                  "email": "admin@rehabai.com",
                                  "password": "admin123"
                                }
                                """
                        )
                    }
                )
            )
            @Valid @RequestBody LoginRequest req) {
        try {
            UserDetails ud = userService.loadUserByUsername(req.email());
            if (ud == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid_credentials");
            }
            if (!passwordEncoder.matches(req.password(), ud.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid_credentials");
            }


            UserServiceClient.CredentialsResponse creds = userService.getCredentialsByEmail(req.email());
            String token = jwtUtil.generateToken(ud, Map.of("user_id", creds.id().toString()));
            long expiresIn = jwtUtil.getExpirationMs();

            var rt = refreshTokenService.issueForUser(creds.id());
            long refreshExpiresIn = refreshTokenService.getRefreshExpirationMs();

            return ResponseEntity.ok(new AuthResponse(token, "Bearer", expiresIn, rt.getTokenId().toString(), refreshExpiresIn));
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid_credentials");
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            org.slf4j.LoggerFactory.getLogger(AuthController.class)
                .error("User service error during login for {}: {}", req.email(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "service_unavailable", "message", "Authentication service temporarily unavailable"));
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(AuthController.class)
                .error("Unexpected error during login for {}", req.email(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "internal_error", "message", "An unexpected error occurred"));
        }
    }

    @Operation(
        summary = "Renovar access token",
        description = """
            # 🔄 Refresh Token
            
            Renova um `accessToken` expirado usando o `refreshToken`.
            
            ## Quando Usar:
            
            - Quando o `accessToken` expirar (após 1 hora)
            - Para manter o usuário autenticado sem pedir senha novamente
            
            ## Como Funciona:
            
            1. Valida o `refreshToken`
            2. Gera novo `accessToken` (válido por 1 hora)
            3. **Rotaciona** o `refreshToken` (invalidando o antigo)
            4. Retorna novos tokens
            
            ## ⚠️ Importante: Token Rotation
            
            O `refreshToken` é **rotacionado** (one-time use):
            - O token antigo é invalidado
            - Um novo `refreshToken` é gerado
            - Use sempre o token mais recente
            
            ## Expiração:
            
            - Se o `refreshToken` também expirou (após 7 dias), o usuário 
              precisa fazer login novamente
            
            ## Exemplo de Uso:
            
            ```bash
            curl -X POST http://localhost:8081/auth/refresh \\
              -H "Content-Type: application/json" \\
              -d '{
                "refreshToken": "660e8400-e29b-41d4-a716-446655440000"
              }'
            ```
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "✅ Token renovado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Sucesso",
                    value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "tokenType": "Bearer",
                          "expiresIn": 3600000,
                          "refreshToken": "770e8400-e29b-41d4-a716-446655440000",
                          "refreshExpiresIn": 604800000
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "❌ Refresh token inválido ou expirado",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Token Expirado",
                        value = "\"expired_refresh_token\""
                    ),
                    @ExampleObject(
                        name = "Token Inválido",
                        value = "\"invalid_refresh_token\""
                    )
                }
            )
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Refresh token para renovação",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = RefreshRequest.class),
                    examples = @ExampleObject(
                        name = "Refresh Request",
                        value = """
                            {
                              "refreshToken": "660e8400-e29b-41d4-a716-446655440000"
                            }
                            """
                    )
                )
            )
            @Valid @RequestBody RefreshRequest req) {
        try {
            UUID tokenId = UUID.fromString(req.refreshToken());
            var newRt = refreshTokenService.rotate(tokenId);
            UserServiceClient.UserResponse u = userService.getUserById(newRt.getUserId());
            UserDetails ud = userService.buildUserDetailsFrom(u);
            String token = jwtUtil.generateToken(ud, Map.of("user_id", u.id().toString()));
            long expiresIn = jwtUtil.getExpirationMs();
            long refreshExpiresIn = refreshTokenService.getRefreshExpirationMs();

            return ResponseEntity.ok(new AuthResponse(token, "Bearer", expiresIn, newRt.getTokenId().toString(), refreshExpiresIn));
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if ("expired_refresh_token".equals(msg)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("expired_refresh_token");
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid_refresh_token");
        }
    }

    @Operation(
        summary = "Logout (invalidar refresh token)",
        description = """
            # 🚪 Logout do Sistema
            
            Invalida um `refreshToken` específico, fazendo logout do usuário.
            
            ## Como Funciona:
            
            1. Recebe o `refreshToken`
            2. Revoga/invalida o token
            3. Token não pode mais ser usado para refresh
            
            ## ⚠️ Nota Importante:
            
            - Invalida apenas o `refreshToken` fornecido
            - O `accessToken` continua válido até expirar (1 hora)
            - Para invalidar todos os tokens, use `/auth/logout_all`
            
            ## Segurança:
            
            - Cliente deve descartar o `accessToken` localmente
            - Token não pode mais ser renovado
            
            ## Exemplo de Uso:
            
            ```bash
            curl -X POST http://localhost:8081/auth/logout \\
              -H "Content-Type: application/json" \\
              -d '{
                "refreshToken": "660e8400-e29b-41d4-a716-446655440000"
              }'
            ```
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "✅ Logout realizado com sucesso (sem conteúdo)"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "❌ Refresh token inválido ou malformado"
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Refresh token a ser invalidado",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = LogoutRequest.class),
                    examples = @ExampleObject(
                        value = """
                            {
                              "refreshToken": "660e8400-e29b-41d4-a716-446655440000"
                            }
                            """
                    )
                )
            )
            @Valid @RequestBody LogoutRequest req) {
        try {
            UUID tokenId = UUID.fromString(req.refreshToken());
            refreshTokenService.revokeToken(tokenId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(
        summary = "Logout de todos os dispositivos",
        description = """
            # 🚪🚪 Logout Global (Todos os Dispositivos)
            
            Invalida **TODOS** os refresh tokens do usuário autenticado.
            
            ## Quando Usar:
            
            - Suspeita de conta comprometida
            - Trocar senha
            - Fazer logout forçado de todos os dispositivos
            
            ## Como Funciona:
            
            1. Extrai `user_id` do JWT no header Authorization
            2. Revoga **todos** os refresh tokens do usuário
            3. Usuário precisa fazer login novamente em todos os dispositivos
            
            ## 🔐 Autenticação Obrigatória:
            
            Requer `accessToken` válido no header:
            
            ```
            Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
            ```
            
            ## Exemplo de Uso:
            
            ```bash
            curl -X POST http://localhost:8081/auth/logout_all \\
              -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            ```
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "✅ Logout global realizado com sucesso (todos os tokens invalidados)"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "❌ Token JWT inválido, expirado ou ausente"
        )
    })
    @PostMapping("/logout_all")
    public ResponseEntity<Void> logoutAll(
            @Parameter(
                description = "Token JWT de autenticação (formato: Bearer <token>)",
                required = true,
                example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader(name = "Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authHeader.substring(7);
        try {
            String userIdStr = jwtUtil.getClaimAsString(token, "user_id");
            if (userIdStr == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            UUID userId = UUID.fromString(userIdStr);
            refreshTokenService.revokeAllForUser(userId);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}

package com.rehabai.auth_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do SpringDoc OpenAPI 3.0 para documentação automática da API.
 *
 * Acesso à documentação:
 * - Swagger UI: http://localhost:8081/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8081/v3/api-docs
 * - OpenAPI YAML: http://localhost:8081/v3/api-docs.yaml
 */
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI authServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RehabAI - Auth Service API")
                        .description("""
                                # 🔐 Serviço de Autenticação e Autorização
                                
                                API REST para gerenciamento de autenticação, autorização e usuários do sistema RehabAI.
                                
                                ## 🎯 Funcionalidades Principais
                                
                                - **Autenticação JWT**: Login com email/senha e geração de tokens JWT
                                - **Autorização RBAC**: Controle de acesso baseado em roles (ADMIN, CLINICIAN, PATIENT)
                                - **Gerenciamento de Usuários**: CRUD completo de usuários
                                - **Refresh Tokens**: Renovação de tokens sem re-autenticação
                                - **Health Check**: Endpoint de saúde para monitoramento
                                
                                ## 🔑 Roles Disponíveis
                                
                                - **ADMIN**: Acesso completo ao sistema
                                - **CLINICIAN**: Profissional de saúde (fisioterapeuta, médico)
                                - **PATIENT**: Paciente do sistema
                                
                                ## 🚀 Como Usar
                                
                                1. **Registrar/Login**: Use `/auth/register` ou `/auth/login`
                                2. **Obter Token**: Copie o `accessToken` do response
                                3. **Autenticar**: Clique no botão 🔓 Authorize e cole o token
                                4. **Testar Endpoints**: Todos os endpoints protegidos agora funcionarão
                                
                                ## 📝 Formato do Token
                                
                                ```
                                Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                                ```
                                
                                ## ⚠️ Ambiente
                                
                                - **Porta**: 8081
                                - **Base URL**: http://localhost:8081
                                - **Banco de Dados**: PostgreSQL
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("RehabAI Team")
                                .email("dev@rehabai.com")
                                .url("https://rehabai.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Desenvolvimento Local"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("API Gateway (Produção)")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                                        ## 🔑 Autenticação JWT
                                        
                                        Para acessar endpoints protegidos, você precisa incluir um token JWT válido no header:
                                        
                                        ```
                                        Authorization: Bearer <seu-token-aqui>
                                        ```
                                        
                                        ### Como Obter um Token:
                                        
                                        1. **Registrar**: POST `/auth/register` com seus dados
                                        2. **Login**: POST `/auth/login` com email e senha
                                        3. **Copiar Token**: Use o `accessToken` retornado
                                        
                                        ### Validade do Token:
                                        
                                        - Access Token: 1 hora
                                        - Refresh Token: 7 dias
                                        
                                        ### Renovar Token Expirado:
                                        
                                        Use POST `/auth/refresh` com o `refreshToken`
                                        """)));
    }
}


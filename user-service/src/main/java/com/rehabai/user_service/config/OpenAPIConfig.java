package com.rehabai.user_service.config;

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
 * - Swagger UI: http://localhost:8082/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8082/v3/api-docs
 * - OpenAPI YAML: http://localhost:8082/v3/api-docs.yaml
 */
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RehabAI - User Service API")
                        .description("""
                                # 👥 Serviço de Gerenciamento de Usuários
                                
                                API REST para gerenciamento completo de usuários do sistema RehabAI (CRUD + Credenciais).
                                
                                ## 🎯 Funcionalidades Principais
                                
                                ### 👤 Usuários
                                - **CRUD Completo**: Criar, ler, atualizar e deletar usuários
                                - **Busca por Email**: Encontrar usuário pelo email
                                - **Listagem**: Listar todos os usuários do sistema
                                - **Health Check**: Endpoint de saúde para monitoramento
                                
                                ### 🔐 Credenciais (Auth Integration)
                                - **Criar Credenciais**: Registro de email/senha para autenticação
                                - **Validar Credenciais**: Verificar email e senha
                                - **Buscar por Email**: Obter credenciais para autenticação
                                - **Atualizar Senha**: Trocar senha do usuário
                                - **Deletar Credenciais**: Remover credenciais ao deletar usuário
                                
                                ## 🔑 Roles/Perfis de Usuário
                                
                                - **ADMIN**: Administrador do sistema
                                  - Acesso total a todos os recursos
                                  - Pode gerenciar qualquer usuário
                                  
                                - **CLINICIAN**: Profissional de saúde
                                  - Fisioterapeuta, médico, profissional de reabilitação
                                  - Pode acessar dados de pacientes atribuídos
                                  
                                - **PATIENT**: Paciente
                                  - Usuário do sistema de reabilitação
                                  - Acesso apenas aos próprios dados
                                
                                ## 🔒 Segurança e Autorização
                                
                                ### Regras de Acesso:
                                - **Health Check**: Público (sem autenticação)
                                - **CRUD de Usuários**: Requer autenticação
                                  - ADMIN: Acesso total
                                  - CLINICIAN: Acesso limitado
                                  - PATIENT: Apenas próprios dados
                                - **Credenciais**: Apenas auth-service (uso interno)
                                
                                ## 🚀 Como Usar
                                
                                1. **Obter Token JWT**: Use o auth-service para login
                                2. **Autenticar**: Clique no botão 🔓 Authorize e cole o token
                                3. **Testar Endpoints**: Todos os endpoints protegidos funcionarão
                                
                                ## 📝 Formato do Token
                                
                                ```
                                Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                                ```
                                
                                ## 🏗️ Arquitetura
                                
                                Este serviço trabalha em conjunto com:
                                - **auth-service**: Autenticação e autorização
                                - **patient-service**: Perfis detalhados de pacientes
                                - **API Gateway**: Roteamento centralizado
                                
                                ## ⚠️ Ambiente
                                
                                - **Porta**: 8082
                                - **Base URL**: http://localhost:8082
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
                                .url("http://localhost:8082")
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
                                        
                                        1. **Login**: Use o auth-service (porta 8081)
                                           - POST http://localhost:8081/auth/login
                                           - Body: { "email": "...", "password": "..." }
                                        2. **Copiar Token**: Use o `accessToken` retornado
                                        3. **Autorizar**: Cole no botão 🔓 Authorize desta página
                                        
                                        ### Validade do Token:
                                        
                                        - Access Token: 1 hora
                                        - Refresh Token: 7 dias (use auth-service para renovar)
                                        
                                        ### Permissões por Role:
                                        
                                        - **ADMIN**: Acesso total
                                        - **CLINICIAN**: Acesso limitado a recursos específicos
                                        - **PATIENT**: Apenas próprios dados
                                        """)));
    }
}


package com.rehabai.file_service.config;

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

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI fileServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RehabAI - File Service API")
                        .description("""
                                # 📁 Serviço de Gerenciamento de Arquivos
                                
                                API REST para upload, armazenamento e pseudonimização de arquivos médicos.
                                
                                ## 🎯 Funcionalidades Principais
                                
                                ### 📤 Upload de Arquivos
                                - **Upload Multipart**: Envio de arquivos com metadata
                                - **Tipos Suportados**: PDF, Imagens (JPEG, PNG), Documentos
                                - **Armazenamento**: AWS S3 (ou LocalStack em desenvolvimento)
                                - **Validação**: Hash SHA-256 para integridade
                                
                                ### 🔒 Pseudonimização (LGPD/GDPR)
                                - **Anonimização**: Remoção de dados identificáveis
                                - **Logs de Auditoria**: Rastreamento de pseudonimização
                                - **Compliance**: LGPD e GDPR
                                
                                ### 📋 Gerenciamento
                                - **Listagem**: Filtrar por usuário e status
                                - **Download**: Recuperar arquivos armazenados
                                - **Deleção**: Remover arquivos (soft/hard delete)
                                - **Metadata**: ID, nome original, tamanho, hash
                                
                                ## 📨 Integração com RabbitMQ
                                
                                Após upload bem-sucedido, publica evento:
                                - **Queue**: `notification.file.uploaded`
                                - **Consumer**: notification-service
                                - **Ação**: Envia notificação por email
                                
                                ## 🔑 Tipos de Arquivo
                                
                                - **MEDICAL_REPORT**: Laudos médicos
                                - **PRESCRIPTION**: Prescrições médicas
                                - **IMAGE**: Imagens (raio-x, ressonância, etc.)
                                - **OTHER**: Outros documentos
                                
                                ## 📊 Status de Arquivo
                                
                                - **UPLOADED**: Recém enviado
                                - **PSEUDONYMIZED**: Anonimizado
                                - **PROCESSING**: Em processamento
                                - **READY**: Pronto para uso
                                - **ERROR**: Erro no processamento
                                
                                ## 🔒 Segurança e Autorização
                                
                                ### Regras de Acesso:
                                - **Health Check**: Público
                                - **Upload**: CLINICIAN ou ADMIN
                                - **Pseudonymize**: CLINICIAN ou ADMIN
                                - **List**: PATIENT (próprios), CLINICIAN/ADMIN (todos)
                                - **Download/Delete**: Dono do arquivo ou ADMIN
                                
                                ## 🚀 Como Usar
                                
                                1. **Obter Token JWT**: Use o auth-service para login
                                2. **Autenticar**: Clique no botão 🔓 Authorize e cole o token
                                3. **Upload**: Use multipart/form-data com arquivo
                                4. **Gerenciar**: Liste, baixe ou delete arquivos
                                
                                ## ⚠️ Ambiente
                                
                                - **Porta**: 8085
                                - **Base URL**: http://localhost:8085
                                - **Storage**: AWS S3 (ou LocalStack)
                                - **Banco de Dados**: PostgreSQL
                                - **Message Broker**: RabbitMQ
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
                                .url("http://localhost:8085")
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
                                        
                                        Para acessar endpoints protegidos, inclua um token JWT válido:
                                        
                                        ```
                                        Authorization: Bearer <seu-token-aqui>
                                        ```
                                        
                                        ### Como Obter um Token:
                                        
                                        1. **Login**: Use o auth-service (porta 8081)
                                           - POST http://localhost:8081/auth/login
                                        2. **Copiar Token**: Use o `accessToken` retornado
                                        3. **Autorizar**: Cole no botão 🔓 Authorize
                                        
                                        ### Permissões por Role:
                                        
                                        - **ADMIN**: Acesso total
                                        - **CLINICIAN**: Upload, pseudonymize, listar todos
                                        - **PATIENT**: Apenas próprios arquivos
                                        """)));
    }
}


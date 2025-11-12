package com.rehabai.patient_service.config;

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
    public OpenAPI patientServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RehabAI - Patient Service API")
                        .description("""
                                # 🏥 Serviço de Gerenciamento de Pacientes
                                
                                API REST para gerenciamento completo de perfis de pacientes e histórico clínico.
                                
                                ## 🎯 Funcionalidades Principais
                                
                                ### 👤 Patient Profiles
                                - **CRUD Completo**: Criar, ler, atualizar e deletar perfis de pacientes
                                - **Informações Pessoais**: Nome, email, data de nascimento, gênero
                                - **Perfil Detalhado**: Idioma preferido, sexo biológico, notas
                                
                                ### 📋 Histórico Clínico
                                - **Clinical Notes**: Anotações clínicas com autor e timestamp
                                - **Conditions**: Condições médicas com códigos e datas
                                - **Allergies**: Alergias com substância, reação e severidade
                                - **Medications**: Medicações com dose, via, frequência e período
                                - **Vital Signs**: Sinais vitais em formato JSON flexível
                                
                                ## 🔑 Perfis de Acesso
                                
                                - **ADMIN**: Acesso total a todos os recursos
                                - **CLINICIAN**: Acesso a pacientes atribuídos
                                - **PATIENT**: Acesso apenas aos próprios dados
                                
                                ## 🚀 Como Usar
                                
                                1. **Obter Token JWT**: Use o auth-service para login
                                2. **Autenticar**: Clique no botão 🔓 Authorize e cole o token
                                3. **Testar Endpoints**: Explore os endpoints disponíveis
                                
                                ## 🏗️ Arquitetura
                                
                                Este serviço trabalha em conjunto com:
                                - **auth-service**: Autenticação e autorização
                                - **user-service**: Dados básicos de usuários
                                - **API Gateway**: Roteamento centralizado
                                
                                ## ⚠️ Ambiente
                                
                                - **Porta**: 8083
                                - **Base URL**: http://localhost:8083
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
                                .url("http://localhost:8083")
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
                                        - **CLINICIAN**: Pacientes atribuídos
                                        - **PATIENT**: Apenas próprios dados
                                        """)));
    }
}


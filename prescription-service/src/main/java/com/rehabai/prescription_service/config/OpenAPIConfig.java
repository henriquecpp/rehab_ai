package com.rehabai.prescription_service.config;

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
    public OpenAPI prescriptionServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RehabAI - Prescription Service API")
                        .description("""
                                # 🤖 Serviço de Prescrição com IA
                                
                                API REST para geração automatizada de prescrições médicas usando IA (AWS Bedrock).
                                
                                ## 🎯 Funcionalidades Principais
                                
                                ### 🚀 Workflow de Prescrição
                                - **Iniciar**: Cria workflow com upload de documentos
                                - **OCR**: Extração de texto (Textract/Tesseract)
                                - **IA Generation**: Claude via AWS Bedrock
                                - **Draft**: Revisão e edição
                                - **Aprovação**: Finalização
                                
                                ### 📋 Gerenciamento
                                - **Listar**: Filtrar por usuário e status
                                - **Buscar**: Por ID
                                - **Histórico**: Todas as prescrições geradas
                                - **Plan Draft**: Gerar plano de reabilitação
                                
                                ### 🔄 Lifecycle Management
                                - **Start**: Iniciar processamento
                                - **Advance**: Avançar etapa
                                - **Complete**: Finalizar workflow
                                
                                ## 🤖 Integração com IA
                                
                                - **Modelo**: Claude 3.5 Haiku (AWS Bedrock)
                                - **Prompt Engineering**: Templates otimizados
                                - **JSON Response**: Estrutura validada
                                - **Contexto**: Histórico do paciente
                                
                                ## 📊 Estágios do Workflow
                                
                                1. **CREATED** - Workflow criado
                                2. **UPLOADED** - Arquivos enviados
                                3. **OCR_COMPLETED** - Texto extraído
                                4. **AI_GENERATED** - IA gerou prescrição
                                5. **DRAFT** - Em revisão
                                6. **APPROVED** - Aprovado
                                7. **ERROR** - Erro no processo
                                
                                ## 🔒 Segurança
                                
                                - **CLINICIAN**: Criar, gerenciar workflows
                                - **PATIENT**: Visualizar próprias prescrições
                                - **ADMIN**: Acesso total
                                
                                ## ⚠️ Ambiente
                                
                                - **Porta**: 8086
                                - **Base URL**: http://localhost:8086
                                - **IA**: AWS Bedrock (Claude 3.5 Haiku)
                                - **OCR**: AWS Textract / Tesseract
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
                                .url("http://localhost:8086")
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
                                        
                                        ```
                                        Authorization: Bearer <seu-token-aqui>
                                        ```
                                        
                                        ### Como Obter:
                                        1. Login no auth-service (porta 8081)
                                        2. Copiar accessToken
                                        3. Autorizar no botão 🔓
                                        
                                        ### Permissões:
                                        - **CLINICIAN**: Criar e gerenciar workflows
                                        - **PATIENT**: Visualizar próprias prescrições
                                        - **ADMIN**: Acesso total
                                        """)));
    }
}


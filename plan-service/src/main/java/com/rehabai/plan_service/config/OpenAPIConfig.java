package com.rehabai.plan_service.config;

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
    public OpenAPI planServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RehabAI - Plan Service API")
                        .description("""
                                # 📋 Serviço de Planos de Reabilitação
                                
                                API REST para gerenciamento, versionamento e auditoria de planos de reabilitação.
                                
                                ## 🎯 Funcionalidades Principais
                                
                                ### 📝 Gerenciamento de Planos
                                - **Criar**: Plano a partir de prescrição (IA)
                                - **Listar**: Filtrar por usuário, status
                                - **Buscar**: Por ID
                                - **Atualizar**: Editar planData (JSON)
                                
                                ### 🔄 Versionamento
                                - **Versões**: Histórico completo de mudanças
                                - **Rollback**: Voltar para versão anterior
                                - **Nova Versão**: Criar versão a partir de atual
                                - **Comparação**: Diff entre versões
                                
                                ### ✅ Workflow de Aprovação
                                - **DRAFT**: Rascunho (editável)
                                - **PENDING_APPROVAL**: Aguardando aprovação
                                - **APPROVED**: Aprovado (read-only)
                                - **ARCHIVED**: Arquivado
                                
                                ### 📊 Auditoria
                                - **Logs**: Quem, quando, o que mudou
                                - **Rastreamento**: Histórico completo
                                - **Compliance**: LGPD/GDPR
                                
                                ## 🗄️ Arquitetura JSONB
                                
                                ### Por Que JSON?
                                
                                Planos são armazenados como **JSON string** (JSONB no PostgreSQL) para:
                                - ✅ **Flexibilidade**: Estrutura adaptável
                                - ✅ **Versionamento**: Fácil comparação
                                - ✅ **Extensibilidade**: Adicionar campos sem migração
                                - ✅ **Integração**: Compatível com prescription-service
                                
                                ### Estrutura do planData
                                
                                ```json
                                {
                                  "title": "Plano de Reabilitação - Joelho Direito",
                                  "diagnosis": "Gonartrose",
                                  "goals": ["Reduzir dor", "Melhorar mobilidade"],
                                  "exercises": [
                                    {
                                      "name": "Alongamento quadríceps",
                                      "sets": 3,
                                      "reps": 15,
                                      "frequency": "3x/semana"
                                    }
                                  ],
                                  "duration": "8 semanas",
                                  "notes": "Paciente apresenta..."
                                }
                                ```
                                
                                ## 🔒 Segurança
                                
                                - **CLINICIAN**: Criar, editar, aprovar
                                - **PATIENT**: Visualizar próprios planos
                                - **ADMIN**: Acesso total + arquivar
                                
                                ## 🔗 Integração
                                
                                - **prescription-service**: Gera planDraft (JSON)
                                - **plan-service**: Cria plano versionado
                                - **patient-service**: Associa ao paciente
                                
                                ## ⚠️ Ambiente
                                
                                - **Porta**: 8088
                                - **Base URL**: http://localhost:8088
                                - **Banco**: PostgreSQL (JSONB)
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
                                .url("http://localhost:8088")
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
                                        - **CLINICIAN**: Criar, editar, aprovar planos
                                        - **PATIENT**: Visualizar próprios planos
                                        - **ADMIN**: Acesso total + arquivar
                                        """)));
    }
}


# Rehab AI Platform (Monorepo)

Plataforma de prescrição personalizada de exercícios de reabilitação com **IA generativa**. Arquitetura de microsserviços com Spring Boot 3.5.x, Spring Cloud Gateway, PostgreSQL, RabbitMQ, AWS (Textract, Bedrock) e observabilidade (Prometheus/Grafana + OpenTelemetry/Jaeger).

## ✨ Funcionalidades Principais

### 🤖 **Geração de Planos com IA** (NOVO!)
- Upload de laudos médicos (PDF/imagem)
- OCR automático com AWS Textract
- Processamento assíncrono via RabbitMQ
- Geração de planos estruturados com AWS Bedrock (Claude 3)
- **Drafts prontos para revisão** em ~40 segundos
- [📖 Documentação Completa](INDICE-MASTER.md)

### 🔐 Autenticação & Autorização
- JWT HS256 com refresh tokens
- Roles: PATIENT, CLINICIAN, ADMIN
- API Gateway valida e injeta headers

### 📁 Gerenciamento de Arquivos
- Upload para S3
- Anonimização LGPD-compliant
- Versionamento e auditoria

### 📋 Planos de Reabilitação
- Criação, edição e versionamento
- Status: DRAFT, APPROVED, ARCHIVED
- Histórico completo de mudanças

### 📊 Observabilidade
- Métricas com Prometheus
- Dashboards com Grafana
- Tracing distribuído com Jaeger
- Logs estruturados

---

## 🏗️ Componentes

| Serviço | Porta | Descrição |
|---------|-------|-----------|
| **API Gateway** | 8080 | Roteamento, CORS, validação JWT |
| **Auth Service** | 8081 | Registro/login, emissão de JWT |
| **User Service** | 8082 | CRUD de usuários, consentimentos LGPD |
| **Patient Service** | 8083 | Perfil e histórico médico |
| **Plan Service** | 8084 | Planos de reabilitação versionados |
| **File Service** | 8085 | Upload S3, anonimização |
| **Prescription Service** | 8086 | **OCR, IA, geração de drafts** ⭐ |
| **Notification Service** | 8087 | Email/push, eventos |

---

## 🚀 Quick Start

### Pré-requisitos
- Docker e Docker Compose
- Conta AWS (para Textract e Bedrock) ou usar stubs para testes

### 1. Configuração

```bash
# Clone o repositório
git clone <repo-url>
cd rehab_ai

# Configure variáveis de ambiente
cp .env.example .env
# Edite .env com suas credenciais AWS e secrets
```

### 2. Executar com Docker Compose

```bash
# Subir toda a stack
docker-compose up -d --build

# Verificar logs
docker-compose logs -f

# Parar serviços
docker-compose down
```

---

## 📖 Documentação

### 🎯 **Início Rápido**
- **[INDICE-MASTER.md](INDICE-MASTER.md)** - Índice completo de toda documentação
- **[QUICK-START-PLAN-DRAFT.md](QUICK-START-PLAN-DRAFT.md)** - Comandos essenciais

### 📊 **Entendimento do Fluxo**
- **[FLUXO-VISUAL-RESUMIDO.md](FLUXO-VISUAL-RESUMIDO.md)** - Visão geral em 4 etapas
- **[FLUXO-COMPLETO-UPLOAD-TO-DRAFT.md](FLUXO-COMPLETO-UPLOAD-TO-DRAFT.md)** - Detalhamento completo
- **[SEQUENCE-DIAGRAM-UPLOAD-TO-DRAFT.md](SEQUENCE-DIAGRAM-UPLOAD-TO-DRAFT.md)** - Diagrama técnico

### 🔧 **Implementação**
- **[PRESCRIPTION-SERVICE-IMPROVEMENTS.md](PRESCRIPTION-SERVICE-IMPROVEMENTS.md)** - Guia técnico
- **[PRESCRIPTION-SERVICE-COMPLETE.md](PRESCRIPTION-SERVICE-COMPLETE.md)** - Resumo executivo
- **[API-ENDPOINTS-COMPLETE.md](API-ENDPOINTS-COMPLETE.md)** - 70+ endpoints documentados

---

## 🌐 Endpoints Principais

### Autenticação
```bash
POST /auth/register  # Criar usuário
POST /auth/login     # Fazer login
POST /auth/refresh   # Renovar token
```

### Geração de Planos com IA ⭐
```bash
# 1. Upload de laudo
POST /files/upload

# 2. Verificar status (opcional)
GET /prescriptions/workflows/latest?fileId={id}

# 3. Obter draft gerado pela IA
GET /prescriptions/files/{fileId}/plan-draft?userId={patientId}

# 4. Criar plano formal
POST /plans

# 5. Aprovar plano
POST /plans/{id}/approve
```

### Gerenciamento
```bash
GET  /users           # Listar usuários
GET  /patients/{id}   # Perfil do paciente
GET  /plans/user/{id} # Planos do paciente
```

**📚 Documentação completa:** [API-ENDPOINTS-COMPLETE.md](API-ENDPOINTS-COMPLETE.md)

---

## 🏃 Execução Local (sem Docker)
Todos os serviços usam Postgres (veja `.env.example`), RabbitMQ e S3 (MinIO para dev). Inicie os serviços de infraestrutura ou use Docker para eles.

```zsh
# Em terminais independentes (necessário ter Postgres, RabbitMQ e MinIO rodando)
(cd api-gateway && mvn spring-boot:run)
(cd auth-service && mvn spring-boot:run)
(cd user-service && mvn spring-boot:run)
(cd file-service && mvn spring-boot:run)
(cd prescription-service && mvn spring-boot:run)
(cd plan-service && mvn spring-boot:run)
(cd notification-service && mvn spring-boot:run)
```

## Autenticação
- Auth Service emite tokens HS256 (`auth.jwt.secret`).
- O Gateway valida o JWT com a mesma chave simétrica e bloqueia acesso não autenticado.

## Observabilidade
- Actuator habilitado; Prometheus coleta `/actuator/prometheus` conforme `monitoring/prometheus.yml`.
- OpenTelemetry exporta traces para o Collector (OTLP gRPC), visualizados no Jaeger.

---

## Variáveis de ambiente (tabelas de referência)

Dica: os valores do `.env` são injetados no `docker-compose.yml`. Em runtime, a prioridade é: variáveis de ambiente > application.yml (padrões). Abaixo, variáveis globais, S3 (MinIO), Bedrock/Textract e um mapeamento por serviço.

### Globais (Infra, Segurança e Observabilidade)

| Variável | Descrição | Exemplo |
|---|---|---|
| POSTGRES_USER | Usuário do Postgres | postgres |
| POSTGRES_PASSWORD | Senha do Postgres | 1234 |
| POSTGRES_DB | Nome do DB | rehabdb |
| RABBITMQ_USER | Usuário do RabbitMQ | guest |
| RABBITMQ_PASS | Senha do RabbitMQ | guest |
| JWT_SECRET | Segredo JWT (HS256) | change-me-in-prod |
| OTEL_EXPORTER_OTLP_ENDPOINT | Endpoint OTLP gRPC do Collector | http://otel-collector:4317 |
| OTEL_EXPORTER_OTLP_PROTOCOL | Protocolo do OTLP | grpc |
| TRACING_SAMPLING_PROBABILITY | Amostragem de traces | 1.0 |
| DDL_AUTO | Estrategia de schema do Hibernate (none/validate/update/create/create-drop) | update (use create-drop para reset) |

### Armazenamento de arquivos (S3 / MinIO – somente desenvolvimento)

Estas variáveis são usadas para o acesso S3 compatível do MinIO (não são credenciais AWS reais):

| Variável | Descrição | Exemplo |
|---|---|---|
| S3_ACCESS_KEY_ID | Access key do MinIO | minioadmin |
| S3_SECRET_ACCESS_KEY | Secret key do MinIO | minioadmin |
| S3_ENDPOINT | Endpoint do MinIO | http://minio:9000 |
| S3_BUCKET | Bucket padrão | rehab-files |
| AWS_REGION | Região lógica (compat.) | us-east-1 |

Serviços que usam S3_*: File Service e Prescription Service (para ler o arquivo a ser processado). O cliente S3 está configurado com path-style e `endpointOverride` para funcionar com MinIO.

### IA (AWS Bedrock/Textract) – credenciais dedicadas e fallback

Para separar credenciais do S3 (MinIO) e da AWS real, usamos dois conjuntos distintos:

- Dedicado a Bedrock/Textract (preferido):

| Variável | Descrição | Exemplo |
|---|---|---|
| BEDROCK_AWS_ACCESS_KEY_ID | Access key AWS para Bedrock/Textract | AKIA... |
| BEDROCK_AWS_SECRET_ACCESS_KEY | Secret key AWS para Bedrock/Textract | **************** |
| USE_BEDROCK | Habilita chamadas ao Bedrock | true/false |
| USE_TEXTRACT | Habilita OCR Textract | false/true |
| BEDROCK_MODEL_ID | Modelo do Bedrock | anthropic.claude-3-haiku |

- Fallback padrão da AWS (opcional, usado se BEDROCK_* não estiverem definidos):

| Variável | Descrição | Exemplo |
|---|---|---|
| AWS_ACCESS_KEY_ID | Access key AWS padrão | AKIA... |
| AWS_SECRET_ACCESS_KEY | Secret key AWS padrão | **************** |

Observações importantes:
- Prescription Service: se `USE_BEDROCK=true` e não houver `BEDROCK_AWS_*`, o serviço tenta `AWS_*` (cadeia padrão do SDK). Se nada for encontrado, a aplicação inicia e loga um WARN; a chamada ao Bedrock pode falhar até credenciais válidas serem fornecidas.
- O acesso S3 local permanece usando apenas `S3_*` (MinIO), não conflitando com credenciais AWS.

### Específicas do Prescription (OCR e normalização)

| Variável | Descrição | Exemplo |
|---|---|---|
| TESSDATA_PATH | Caminho do tessdata (montado no container) | /opt/tessdata |
| OCR_LANG | Idiomas do OCR (tesseract) | por+eng |
| OCR_MAX_PAGES | Máximo de páginas para OCR | 5 |
| OCR_TEXTRACT_USE_ANALYZE | Usa AnalyzeDocument no Textract | true |
| OCR_HEURISTIC_MIN_CHARS_NO_WS | Heurística de qualidade | 30 |
| OCR_HEURISTIC_MIN_LETTER_RATIO | Heurística de qualidade | 0.15 |
| OCR_HEURISTIC_MIN_UNIQUE_CHARS | Heurística de qualidade | 10 |
| OCR_HEURISTIC_MIN_NONWS_DENSITY | Heurística de qualidade | 0.30 |
| OCR_FALLBACK_MIN_CONFIDENCE | Confiança mínima fallback | 0.2 |
| NORMALIZATION_USE_LLM | Normalização com LLM | false |

### Mapeamento por serviço (resumo)

| Serviço | DB (Postgres) | RabbitMQ | JWT | S3_* (MinIO) | BEDROCK_AWS_* | AWS_* Fallback | OTLP |
|---|---|---|---|---|---|---|---|
| api-gateway | – | – | JWT_SECRET | – | – | – | OTEL_* |
| auth-service | SPRING_DATASOURCE_* | – | JWT_SECRET | – | – | – | OTEL_* |
| user-service | SPRING_DATASOURCE_* | RabbitMQ_* | JWT_SECRET | – | – | – | OTEL_* |
| patient-service | SPRING_DATASOURCE_* | RabbitMQ_* | JWT_SECRET | – | – | – | OTEL_* |
| file-service | SPRING_DATASOURCE_* | RabbitMQ_* | JWT_SECRET | S3_* | – | – | OTEL_* |
| prescription-service | SPRING_DATASOURCE_* | RabbitMQ_* | – | S3_* | Opcional | Opcional | OTEL_* |
| plan-service | SPRING_DATASOURCE_* | – | – | – | – | – | OTEL_* |
| notification-service | SPRING_DATASOURCE_* | RabbitMQ_* | – | – | – | – | OTEL_* |

Notas:
- SPRING_DATASOURCE_* são montados pelo docker-compose a partir de `POSTGRES_*`.
- OTEL_* são usados por todos os serviços para exportar traces ao Collector (gRPC 4317).
- `S3_*` são exclusivos para MinIO (ambiente local). Produção em AWS pode usar S3 real removendo `S3_ENDPOINT` e fornecendo credenciais AWS reais (fora do escopo deste README).

---

## Dicas de solução de problemas
- Se ver erros de H2 no startup: garanta que `spring.datasource.driver-class-name=org.postgresql.Driver` e o dialect do Hibernate para Postgres estejam ativos (já padronizado nos services).
- Se o file-service falhar com “Unable to load an HTTP implementation”: o pom inclui `software.amazon.awssdk:url-connection-client` e o S3 usa `UrlConnectionHttpClient`; confirme que o jar foi rebuildado.
- Se houver timeout de OTLP no startup: verifique que o `otel-collector` está pronto e escutando em `0.0.0.0:4317`. Jaeger deve estar com `COLLECTOR_OTLP_ENABLED=true`.

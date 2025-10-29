# Auth Service

Serviço de autenticação e autorização, gerenciamento de usuários e consentimentos.

## Variáveis de Ambiente

### Banco de Dados
- `SPRING_DATASOURCE_URL` - URL de conexão PostgreSQL (padrão: `jdbc:postgresql://db:5432/rehabdb`)
- `SPRING_DATASOURCE_USERNAME` - Usuário do banco (padrão: `postgres`)
- `SPRING_DATASOURCE_PASSWORD` - Senha do banco (padrão: `1234`)
- `DB_HOST` - Host do banco (padrão: `db`)
- `DB_PORT` - Porta do banco (padrão: `5432`)
- `DB_NAME` - Nome do banco (padrão: `rehabdb`)

### Segurança
- `JWT_SECRET` - Chave secreta para assinatura JWT (padrão: `my-secret-key`)
- `JWT_EXPIRATION_MS` - Tempo de expiração do token em ms (padrão: `86400000` - 24h)

### Servidor
- `SERVER_PORT` - Porta do serviço (padrão: `8081`)

### Observabilidade
- `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` - Endpoints expostos (padrão: `prometheus,health,info`)
- `TRACING_SAMPLING_PROBABILITY` - Probabilidade de sampling de traces (padrão: `1.0`)
- `OTEL_EXPORTER_OTLP_ENDPOINT` - Endpoint OpenTelemetry (padrão: `http://otel-collector:4317`)
- `OTEL_EXPORTER_OTLP_PROTOCOL` - Protocolo OTLP (padrão: `grpc`)

## Funcionalidades

- ✅ Registro de usuários com roles (ADMIN, CLINICIAN, PATIENT)
- ✅ Autenticação via JWT
- ✅ Gerenciamento de consentimentos (LGPD/GDPR)
- ✅ OAuth2 clients (tabelas criadas)
- ✅ Refresh tokens (tabelas criadas)
- ✅ Usuários com email único (sem username)

## Endpoints Principais

- `POST /auth/register` - Registrar novo usuário
- `POST /auth/login` - Login e obtenção de JWT
- `POST /consents/users/{userId}` - Criar/atualizar consentimento
- `GET /consents/users/{userId}` - Listar consentimentos do usuário
- `GET /consents/users/{userId}/check/{type}` - Verificar consentimento específico

## Roles Disponíveis

- `ADMIN` - Administrador do sistema
- `CLINICIAN` - Profissional de saúde
- `PATIENT` - Paciente (padrão)


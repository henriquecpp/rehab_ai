# Plan Service

Serviço responsável pelo gerenciamento de planos de reabilitação, incluindo versionamento e auditoria.

## Variáveis de Ambiente

### Banco de Dados
- `SPRING_DATASOURCE_URL` - URL de conexão PostgreSQL (padrão: `jdbc:postgresql://db:5432/rehabdb`)
- `SPRING_DATASOURCE_USERNAME` - Usuário do banco (padrão: `postgres`)
- `SPRING_DATASOURCE_PASSWORD` - Senha do banco (padrão: `1234`)
- `DB_HOST` - Host do banco (padrão: `db`)
- `DB_PORT` - Porta do banco (padrão: `5432`)
- `DB_NAME` - Nome do banco (padrão: `rehabdb`)
- `DB_USER` - Usuário alternativo (padrão: `postgres`)
- `DB_PASSWORD` - Senha alternativa (padrão: `1234`)

### Servidor
- `SERVER_PORT` - Porta do serviço (padrão: `8086`)

### Observabilidade
- `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` - Endpoints expostos (padrão: `prometheus,health,info`)
- `TRACING_SAMPLING_PROBABILITY` - Probabilidade de sampling de traces (padrão: `1.0`)
- `OTEL_EXPORTER_OTLP_ENDPOINT` - Endpoint OpenTelemetry (padrão: `http://otel-collector:4317`)
- `OTEL_EXPORTER_OTLP_PROTOCOL` - Protocolo OTLP (padrão: `grpc`)

## Funcionalidades

- ✅ Criação de planos a partir de prescrições
- ✅ Versionamento automático de planos
- ✅ Auditoria completa de mudanças
- ✅ Aprovação/rejeição de planos
- ✅ Arquivamento de planos
- ✅ Histórico de versões
- ✅ Métricas Prometheus

## Endpoints Principais

- `POST /plans` - Criar novo plano
- `GET /plans/{id}` - Obter plano por ID
- `PUT /plans/{id}` - Atualizar plano
- `GET /plans/user/{userId}` - Listar planos do usuário
- `GET /plans/prescription/{prescriptionId}/latest` - Obter última versão do plano
- `POST /plans/{id}/approve` - Aprovar plano
- `POST /plans/{id}/archive` - Arquivar plano
- `GET /plans/{id}/audit` - Histórico de auditoria


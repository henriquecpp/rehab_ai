# API Gateway

Gateway de API com roteamento, autenticação JWT e observabilidade.

## Variáveis de Ambiente

### Segurança
- `JWT_SECRET` - Chave secreta para validação JWT (padrão: `my-secret-key`)

### Roteamento de Serviços
- `AUTH_SERVICE_URL` - URL do auth-service (padrão: `http://auth-service:8081`)
- `USER_SERVICE_URL` - URL do user-service (padrão: `http://user-service:8082`)
- `FILE_SERVICE_URL` - URL do file-service (padrão: `http://file-service:8083`)
- `PRESCRIPTION_SERVICE_URL` - URL do prescription-service (padrão: `http://prescription-service:8084`)
- `NOTIFICATION_SERVICE_URL` - URL do notification-service (padrão: `http://notification-service:8085`)
- `PLAN_SERVICE_URL` - URL do plan-service (padrão: `http://plan-service:8086`)

### Servidor
- `SERVER_PORT` - Porta do gateway (padrão: `8080`)

### Observabilidade
- `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` - Endpoints expostos (padrão: `prometheus,health,info`)
- `TRACING_SAMPLING_PROBABILITY` - Probabilidade de sampling (padrão: `1.0`)
- `OTEL_EXPORTER_OTLP_ENDPOINT` - Endpoint OpenTelemetry (padrão: `http://otel-collector:4317`)
- `OTEL_EXPORTER_OTLP_PROTOCOL` - Protocolo OTLP (padrão: `grpc`)

## Funcionalidades

- ✅ Roteamento para todos os microserviços
- ✅ Validação de JWT em endpoints protegidos
- ✅ CORS configurado
- ✅ Headers de tracing propagados
- ✅ Métricas Prometheus
- ⚠️ Rate limiting (pendente)

## Rotas

Todas as requisições passam pelo gateway na porta `8080`:

- `/auth/**` → auth-service
- `/users/**` → user-service
- `/files/**` → file-service
- `/prescriptions/**` → prescription-service
- `/notifications/**` → notification-service
- `/plans/**` → plan-service

## Endpoints Públicos

- `/auth/register`
- `/auth/login`
- `/actuator/health`
- `/actuator/prometheus`

Todos os outros endpoints requerem JWT válido no header `Authorization: Bearer <token>`.


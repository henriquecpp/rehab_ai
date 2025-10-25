# API Gateway

Roteia chamadas para os microsserviços e aplica autenticação.

## Variáveis de ambiente
- SERVER_PORT (default: 8080)
- JWT_SECRET (obrigatório em prod)
- AUTH_SERVICE_URL (default: http://auth-service:8081)
- PATIENT_SERVICE_URL (default: http://patient-service:8082)
- FILE_SERVICE_URL (default: http://file-service:8083)
- PRESCRIPTION_SERVICE_URL (default: http://prescription-service:8084)
- NOTIFICATION_SERVICE_URL (default: http://notification-service:8085)
- PLAN_SERVICE_URL (default: http://plan-service:8086)
- MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE (default: prometheus,health,info)
- TRACING_SAMPLING_PROBABILITY (default: 1.0)
- OTEL_EXPORTER_OTLP_ENDPOINT (default: http://otel-collector:4317)
- OTEL_EXPORTER_OTLP_PROTOCOL (default: grpc)

## Observabilidade
- Métricas: /actuator/prometheus
- Tracing: exportado via OTLP para o collector

## Execução (Docker Compose)
O compose já injeta os valores padrão. Ajuste um `.env` se precisar sobrescrever.


# Auth Service

Autenticação e autorização (JWT/OAuth2).

## Variáveis de ambiente
- SERVER_PORT (default: 8081)
- SPRING_DATASOURCE_URL (default: jdbc:postgresql://db:5432/rehabdb)
- SPRING_DATASOURCE_USERNAME (default: postgres)
- SPRING_DATASOURCE_PASSWORD (default: 1234)
- JWT_SECRET (obrigatório em prod)
- JWT_EXPIRATION_MS (default: 3600000)
- MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE (default: prometheus,health,info)
- TRACING_SAMPLING_PROBABILITY (default: 1.0)
- OTEL_EXPORTER_OTLP_ENDPOINT (default: http://otel-collector:4317)
- OTEL_EXPORTER_OTLP_PROTOCOL (default: grpc)

## Observabilidade
- Métricas: /actuator/prometheus
- Health: /actuator/health
- Tracing: exportado via OTLP


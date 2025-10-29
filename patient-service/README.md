# Patient Service

Gestão de pacientes e histórico clínico.

## Variáveis de ambiente
- SERVER_PORT (default: 8087)
- SPRING_DATASOURCE_URL (default: jdbc:postgresql://db:5432/rehabdb)
- SPRING_DATASOURCE_USERNAME (default: postgres)
- SPRING_DATASOURCE_PASSWORD (default: 1234)
- SPRING_RABBITMQ_HOST (default: rabbitmq)
- SPRING_RABBITMQ_PORT (default: 5672)
- RABBITMQ_USER (default: guest)
- RABBITMQ_PASS (default: guest)
- JWT_SECRET (obrigatório em rotas seguras)
- MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE (default: prometheus,health,info)
- TRACING_SAMPLING_PROBABILITY (default: 1.0)
- OTEL_EXPORTER_OTLP_ENDPOINT (default: http://otel-collector:4317)
- OTEL_EXPORTER_OTLP_PROTOCOL (default: grpc)

## Observabilidade
- Métricas: /actuator/prometheus
- Health: /actuator/health
- Tracing: via OTLP

# File Service

Upload e mensageria de arquivos (RabbitMQ + S3/MinIO).

## Variáveis de ambiente
- SERVER_PORT (default: 8083)
- SPRING_DATASOURCE_URL/USERNAME/PASSWORD (Postgres)
- SPRING_RABBITMQ_HOST/PORT/USERNAME/PASSWORD
- AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY / AWS_REGION
- S3_ENDPOINT (ex.: http://minio:9000)
- S3_BUCKET (default: rehab-files)
- JWT_SECRET (obrigatório em rotas de upload seguras)
- AMQP_FILE_EXCHANGE (default: file.events)
- AMQP_ROUTING_KEY_UPLOADED (default: file.uploaded)
- MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE (default: prometheus,health,info)
- TRACING_SAMPLING_PROBABILITY (default: 1.0)
- OTEL_EXPORTER_OTLP_ENDPOINT (default: http://otel-collector:4317)
- OTEL_EXPORTER_OTLP_PROTOCOL (default: grpc)

## Observabilidade
- Métricas: /actuator/prometheus
- Health: /actuator/health
- Tracing: via OTLP


# Prescription Service

Extração/OCR, normalização e geração de prescrição assistida por IA.

## Variáveis de ambiente
- SERVER_PORT (default: 8084)
- SPRING_DATASOURCE_URL/USERNAME/PASSWORD (Postgres)
- SPRING_RABBITMQ_HOST (default: rabbitmq)
- SPRING_RABBITMQ_PORT (default: 5672)
- RABBITMQ_USER (default: guest)
- RABBITMQ_PASS (default: guest)
- AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY / AWS_REGION
- S3_ENDPOINT (ex.: http://minio:9000)
- S3_BUCKET (default: rehab-files)
- USE_TEXTRACT (default: false)
- OCR_LANG (default: por+eng)
- TESSDATA_PATH (ex.: /opt/tessdata)
- OCR_MAX_PAGES (default: 5)
- OCR_TEXTRACT_USE_ANALYZE (default: true)
- OCR_HEURISTIC_MIN_CHARS_NO_WS (default: 30)
- OCR_HEURISTIC_MIN_LETTER_RATIO (default: 0.15)
- OCR_HEURISTIC_MIN_UNIQUE_CHARS (default: 10)
- OCR_HEURISTIC_MIN_NONWS_DENSITY (default: 0.30)
- OCR_FALLBACK_MIN_CONFIDENCE (default: 0.2)
- NORMALIZATION_USE_LLM (default: false)
- USE_BEDROCK (default: false)
- BEDROCK_MODEL_ID (default: anthropic.claude-3-haiku)
- GUARDRAILS_ENABLED (default: true)
- AMQP_FILE_EXCHANGE/AMQP_ROUTING_KEY_UPLOADED/AMQP_PRESCRIPTION_QUEUE
- MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE (default: prometheus,health,info)
- TRACING_SAMPLING_PROBABILITY (default: 1.0)
- OTEL_EXPORTER_OTLP_ENDPOINT (default: http://otel-collector:4317)
- OTEL_EXPORTER_OTLP_PROTOCOL (default: grpc)

## Observabilidade
- Métricas: /actuator/prometheus
- Health: /actuator/health
- Tracing: via OTLP (Jaeger UI em http://localhost:16686)

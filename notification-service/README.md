# Notification Service

Envio de e-mails/push a partir de eventos (RabbitMQ).

## Variáveis de ambiente
- SERVER_PORT (default: 8085)
- SPRING_DATASOURCE_URL/USERNAME/PASSWORD (Postgres)
- SPRING_RABBITMQ_HOST (default: rabbitmq)
- SPRING_RABBITMQ_PORT (default: 5672)
- RABBITMQ_USER (default: guest)
- RABBITMQ_PASS (default: guest)
- MAIL_HOST (default: smtp.gmail.com)
- MAIL_PORT (default: 587)
- MAIL_USER, MAIL_PASS
- NOTIFICATION_TO (default: MAIL_USER)
- AMQP_FILE_EXCHANGE/AMQP_ROUTING_KEY_UPLOADED/AMQP_NOTIFICATION_QUEUE
- MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE (default: prometheus,health,info)
- TRACING_SAMPLING_PROBABILITY (default: 1.0)
- OTEL_EXPORTER_OTLP_ENDPOINT (default: http://otel-collector:4317)
- OTEL_EXPORTER_OTLP_PROTOCOL (default: grpc)

## Observabilidade
- Métricas: /actuator/prometheus
- Health: /actuator/health
- Tracing: via OTLP

# Notification Service

Serviço de envio de notificações por e-mail e eventos assíncronos.

## Variáveis de Ambiente

### Banco de Dados
- `SPRING_DATASOURCE_URL` - URL de conexão PostgreSQL (padrão: `jdbc:postgresql://db:5432/rehabdb`)
- `SPRING_DATASOURCE_USERNAME` - Usuário do banco (padrão: `postgres`)
- `SPRING_DATASOURCE_PASSWORD` - Senha do banco (padrão: `1234`)

### RabbitMQ
- `SPRING_RABBITMQ_HOST` - Host do RabbitMQ (padrão: `rabbitmq`)
- `SPRING_RABBITMQ_PORT` - Porta do RabbitMQ (padrão: `5672`)
- `RABBITMQ_USER` - Usuário RabbitMQ (padrão: `guest`)
- `RABBITMQ_PASS` - Senha RabbitMQ (padrão: `guest`)

### E-mail (SMTP)
- `SPRING_MAIL_HOST` - Host SMTP (padrão: `smtp.gmail.com`)
- `SPRING_MAIL_PORT` - Porta SMTP (padrão: `587`)
- `SPRING_MAIL_USERNAME` - Usuário e-mail (obrigatório)
- `SPRING_MAIL_PASSWORD` - Senha e-mail (obrigatório)
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH` - Habilitar auth (padrão: `true`)
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE` - Habilitar TLS (padrão: `true`)

### Servidor
- `SERVER_PORT` - Porta do serviço (padrão: `8085`)

### Observabilidade
- `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` - Endpoints expostos (padrão: `prometheus,health,info`)
- `TRACING_SAMPLING_PROBABILITY` - Probabilidade de sampling (padrão: `1.0`)
- `OTEL_EXPORTER_OTLP_ENDPOINT` - Endpoint OpenTelemetry (padrão: `http://otel-collector:4317`)
- `OTEL_EXPORTER_OTLP_PROTOCOL` - Protocolo OTLP (padrão: `grpc`)

## Funcionalidades

- ✅ Envio de e-mails via SMTP
- ✅ Consumidor de eventos RabbitMQ
- ✅ Templates de e-mail básicos
- ⚠️ Push notifications (pendente)
- ⚠️ Histórico de notificações (pendente)

## Eventos Consumidos

O serviço pode consumir diversos eventos para enviar notificações, incluindo:
- Novo usuário registrado
- Arquivo processado
- Plano aprovado
- Prescrição gerada


# File Service

Serviço de upload, armazenamento e gerenciamento de arquivos (laudos, imagens médicas, etc.).

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

### AWS S3 / MinIO
- `AWS_ACCESS_KEY_ID` - Access Key AWS/MinIO (padrão: `minioadmin`)
- `AWS_SECRET_ACCESS_KEY` - Secret Key AWS/MinIO (padrão: `minioadmin`)
- `AWS_REGION` - Região AWS (padrão: `us-east-1`)
- `S3_ENDPOINT` - Endpoint S3 customizado para MinIO (padrão: `http://minio:9000`)
- `S3_BUCKET` - Nome do bucket (padrão: `rehab-files`)

### AMQP
- `AMQP_FILE_EXCHANGE` - Exchange para eventos de arquivo (padrão: `file.events`)
- `AMQP_ROUTING_KEY_UPLOADED` - Routing key para upload (padrão: `file.uploaded`)

### Servidor
- `SERVER_PORT` - Porta do serviço (padrão: `8083`)

### Observabilidade
- `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` - Endpoints expostos (padrão: `prometheus,health,info`)
- `TRACING_SAMPLING_PROBABILITY` - Probabilidade de sampling de traces (padrão: `1.0`)
- `OTEL_EXPORTER_OTLP_ENDPOINT` - Endpoint OpenTelemetry (padrão: `http://otel-collector:4317`)
- `OTEL_EXPORTER_OTLP_PROTOCOL` - Protocolo OTLP (padrão: `grpc`)

## Funcionalidades

- ✅ Upload de arquivos para S3/MinIO
- ✅ Cálculo de hash SHA-256
- ✅ Publicação de eventos RabbitMQ ao fazer upload
- ✅ Metadados de arquivos no PostgreSQL
- ✅ Associação de arquivos com usuários
- ⚠️ Anonimização (tabela criada, lógica pendente)

## Endpoints Principais

- `POST /files/upload` - Upload de arquivo
- `GET /files/{id}` - Obter metadados do arquivo
- `GET /files/user/{userId}` - Listar arquivos do usuário

## Eventos Publicados

- `file.uploaded` - Publicado quando arquivo é enviado com sucesso
  - Campos: `fileId`, `bucket`, `s3Path`, `originalName`, `sizeBytes`, `hashSha256`


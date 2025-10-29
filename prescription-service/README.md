# Prescription Service

Serviço de pipeline de IA para extração, normalização e geração de prescrições de exercícios.

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
- `AMQP_FILE_EXCHANGE` - Exchange para eventos (padrão: `file.events`)
- `AMQP_ROUTING_KEY_UPLOADED` - Routing key upload (padrão: `file.uploaded`)
- `AMQP_PRESCRIPTION_QUEUE` - Fila de prescrições (padrão: `prescription.file.uploaded`)

### AWS Bedrock
- `AWS_ACCESS_KEY_ID` - Access Key AWS (obrigatório se USE_BEDROCK=true)
- `AWS_SECRET_ACCESS_KEY` - Secret Key AWS (obrigatório se USE_BEDROCK=true)
- `AWS_REGION` - Região AWS (padrão: `us-east-1`)
- `USE_BEDROCK` - Habilitar Bedrock (padrão: `false`)
- `BEDROCK_MODEL_ID` - ID do modelo Bedrock (padrão: `anthropic.claude-3-haiku`)

### OCR
- `USE_TEXTRACT` - Usar AWS Textract (padrão: `false`)
- `TESSDATA_PATH` - Caminho para tessdata (Tesseract) (padrão: vazio)
- `OCR_LANG` - Idiomas OCR (padrão: `por+eng`)
- `OCR_MAX_PAGES` - Máximo de páginas PDF (padrão: `5`)
- `OCR_TEXTRACT_USE_ANALYZE` - Usar AnalyzeDocument (padrão: `true`)

### OCR Heuristics (ajuste fino)
- `OCR_HEURISTIC_MIN_CHARS_NO_WS` - Mínimo de caracteres sem espaços (padrão: `30`)
- `OCR_HEURISTIC_MIN_LETTER_RATIO` - Ratio mínimo de letras (padrão: `0.15`)
- `OCR_HEURISTIC_MIN_UNIQUE_CHARS` - Mínimo de caracteres únicos (padrão: `10`)
- `OCR_HEURISTIC_MIN_NONWS_DENSITY` - Densidade mínima sem espaços (padrão: `0.30`)
- `OCR_FALLBACK_MIN_CONFIDENCE` - Confiança mínima fallback (padrão: `0.2`)

### Normalização e LLM
- `NORMALIZATION_USE_LLM` - Usar LLM para normalização (padrão: `false`)
- `GUARDRAILS_ENABLED` - Habilitar guardrails (padrão: `true`)

### S3 (para download de arquivos)
- `S3_ENDPOINT` - Endpoint S3 customizado (padrão: vazio, usa AWS)
- `S3_BUCKET` - Bucket S3 (padrão: `rehab-files`)

### Servidor
- `SERVER_PORT` - Porta do serviço (padrão: `8084`)

### Observabilidade
- `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` - Endpoints expostos (padrão: `prometheus,health,info`)
- `TRACING_SAMPLING_PROBABILITY` - Probabilidade de sampling (padrão: `1.0`)
- `OTEL_EXPORTER_OTLP_ENDPOINT` - Endpoint OpenTelemetry (padrão: `http://otel-collector:4317`)
- `OTEL_EXPORTER_OTLP_PROTOCOL` - Protocolo OTLP (padrão: `grpc`)

## Funcionalidades

- ✅ OCR com Tesseract (Tess4J)
- ✅ OCR com AWS Textract
- ✅ Fallback inteligente entre OCR engines
- ✅ Extração de texto de PDFs nativos
- ✅ Normalização de termos clínicos
- ✅ Geração de prescrições via Bedrock (Claude)
- ✅ Guardrails básicos
- ✅ Pipeline assíncrono via RabbitMQ
- ✅ Métricas por estágio (extração, normalização, prescrição)
- ✅ Traces de IA registrados no banco
- ✅ OpenTelemetry integrado

## Pipeline de Processamento

1. **EXTRACTION** - OCR do arquivo (PDF/imagem)
2. **NORMALIZATION** - Normalização de termos médicos
3. **PRESCRIPTION** - Geração de plano via LLM
4. **DONE** - Pipeline concluído

## Endpoints Principais

- `GET /prescriptions/workflows/latest?fileId={uuid}` - Último workflow do arquivo
- `GET /prescriptions/stages/latest?fileId={uuid}` - Últimos estágios processados
- `GET /prescriptions/extractions/{id}` - Obter extração específica
- `GET /prescriptions/normalizations/{id}` - Obter normalização específica
- `GET /prescriptions/generated/{id}` - Obter prescrição gerada

## Eventos Consumidos

- `file.uploaded` - Consome da fila `prescription.file.uploaded` e inicia pipeline


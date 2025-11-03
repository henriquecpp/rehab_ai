# Prescription Service

Serviço de pipeline de IA para extração, normalização e geração de prescrições de exercícios.

Este README foi atualizado para refletir a separação entre credenciais usadas para S3/MinIO (locais) e credenciais AWS usadas apenas para Bedrock (LLM).

## Visão Geral

- O serviço consome eventos de upload de arquivos e executa uma pipeline: OCR → Normalização → Geração de prescrição (via LLM opcional).
- Localmente usamos MinIO para emular S3. Para usar Bedrock (AWS) é preciso fornecer credenciais AWS separadas.

## Variáveis de Ambiente (principais)

Observação: há agora duas famílias de variáveis relacionadas a armazenamento/IA:

- MinIO / S3 local (dev): `S3_ACCESS_KEY_ID`, `S3_SECRET_ACCESS_KEY`, `S3_ENDPOINT`, `S3_BUCKET` — usadas pelos clients S3 dentro dos serviços.
- AWS (Bedrock/Textract): `BEDROCK_AWS_ACCESS_KEY_ID`, `BEDROCK_AWS_SECRET_ACCESS_KEY` — usadas apenas para chamadas à AWS Bedrock/Textract quando necessário.

### Banco de Dados
- `SPRING_DATASOURCE_URL` - URL de conexão PostgreSQL (padrão: `jdbc:postgresql://db:5432/rehabdb`)
- `SPRING_DATASOURCE_USERNAME` - Usuário do banco (padrão: `postgres`)
- `SPRING_DATASOURCE_PASSWORD` - Senha do banco (padrão: `1234`)

### RabbitMQ
- `SPRING_RABBITMQ_HOST` - Host do RabbitMQ (padrão: `rabbitmq`)
- `SPRING_RABBITMQ_PORT` - Porta do RabbitMQ (padrão: `5672`)
- `RABBITMQ_USER`, `RABBITMQ_PASS`

### S3 / MinIO (desenvolvimento)
- `S3_ACCESS_KEY_ID` - Access key para MinIO (default: `minioadmin` no `.env.example`)
- `S3_SECRET_ACCESS_KEY` - Secret key para MinIO (default: `minioadmin`)
- `S3_ENDPOINT` - Endpoint do S3/MinIO (ex: `http://minio:9000`)
- `S3_BUCKET` - Nome do bucket (default: `rehab-files`)

> Importante: NÃO reutilize as credenciais do MinIO para Bedrock. Eles são separados por design.

### AWS Bedrock / Textract (opcional)
- `USE_BEDROCK` (ou `llm.useBedrock`) - Habilitar chamadas ao Bedrock (padrão: `false`).
  - Se `USE_BEDROCK=true`, o serviço exige (fail-fast) que as variáveis `BEDROCK_AWS_ACCESS_KEY_ID` e `BEDROCK_AWS_SECRET_ACCESS_KEY` estejam definidas.
- `BEDROCK_AWS_ACCESS_KEY_ID` - Access Key AWS (somente Bedrock)
- `BEDROCK_AWS_SECRET_ACCESS_KEY` - Secret Key AWS (somente Bedrock)
- `BEDROCK_MODEL_ID` - ID do modelo Bedrock (padrão: `anthropic.claude-3-haiku`)

### OCR
- `USE_TEXTRACT` - Usar AWS Textract (padrão: `false`)
- `TESSDATA_PATH` - Caminho para tessdata (Tesseract)
- `OCR_LANG`, `OCR_MAX_PAGES`, etc. (vide `application.yml`)

### Observabilidade e servidor
- `SERVER_PORT`, `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE`, `OTEL_EXPORTER_OTLP_ENDPOINT`, `TRACING_SAMPLING_PROBABILITY` (vide `application.yml`)

## Comportamento importante

- Validação de startup: se `USE_BEDROCK=true` e as variáveis `BEDROCK_AWS_ACCESS_KEY_ID` / `BEDROCK_AWS_SECRET_ACCESS_KEY` não estiverem presentes, o serviço não inicia e lança uma mensagem indicando que as credenciais são necessárias. Isso evita tentativas silenciosas de chamada ao Bedrock com credenciais inválidas.

## Como executar localmente (dev)

1) Copie o exemplo de variáveis para `.env` (NÃO comite esse arquivo):

```bash
cp .env.example .env
# edite .env para ajustar valores locais se necessário
```

2) Subir a stack (exemplo mínimo que inclui MinIO):

```bash
# na raiz do repositório
docker compose up -d minio db rabbitmq file-service prescription-service
```

- Por padrão o `.env.example` configura `S3_ACCESS_KEY_ID/minioadmin` e `S3_SECRET_ACCESS_KEY/minioadmin` para MinIO.

3) Habilitar Bedrock (somente se você tiver credenciais AWS válidas)

- No seu `.env` local (não versionado) defina:

```bash
USE_BEDROCK=true
BEDROCK_AWS_ACCESS_KEY_ID=AKIA...
BEDROCK_AWS_SECRET_ACCESS_KEY=...
BEDROCK_MODEL_ID=anthropic.claude-3-haiku
```

- Reinicie o serviço `prescription-service`:

```bash
docker compose up -d prescription-service
```

Se `USE_BEDROCK=true` e as variáveis `BEDROCK_AWS_*` não existirem, o serviço falhará no startup com uma mensagem clara.

## Endpoints principais

- `GET /prescriptions/workflows/latest?fileId={uuid}` - Último workflow do arquivo
- `GET /prescriptions/stages/latest?fileId={uuid}` - Últimos estágios processados
- `GET /prescriptions/extractions/{id}` - Obter extração específica
- `GET /prescriptions/normalizations/{id}` - Obter normalização específica
- `GET /prescriptions/generated/{id}` - Obter prescrição gerada

## Eventos consumidos

- `file.uploaded` - Consome da fila `prescription.file.uploaded` e inicia pipeline

## Dicas e troubleshooting

- Se o serviço não iniciar e a mensagem mencionar Bedrock/credenciais → verifique `USE_BEDROCK` e se as variáveis `BEDROCK_AWS_ACCESS_KEY_ID`/`BEDROCK_AWS_SECRET_ACCESS_KEY` estão definidas no `.env` ou no ambiente do container.
- Para testar apenas o fluxo de OCR/normalização sem Bedrock, mantenha `USE_BEDROCK=false`.
- Não comite credenciais. Use Docker secrets / variáveis de ambiente do CI para ambientes de produção.

---

Se quiser, eu posso também acrescentar um trecho rápido no README com comandos de verificação de logs e um exemplo de evento `file.uploaded` para teste local. Deseja que eu adicione isso?

# Rehab AI Platform (Monorepo)

Plataforma de prescrição personalizada de exercícios de reabilitação. Arquitetura de microsserviços com Spring Boot 3.5.x, Spring Cloud Gateway, PostgreSQL, RabbitMQ e observabilidade (Prometheus/Grafana + OpenTelemetry/Jaeger).

## Componentes
- API Gateway (porta 8080): roteamento, CORS, validação de JWT HS256, ponto de entrada único.
- Auth Service (porta 8081): registro/login de usuários e emissão de JWT HS256.
- User Service (porta 8082): CRUD de usuários (PATIENT, CLINICIAN, ADMIN) — substitui o antigo patient-service.
- File Service (porta 8083): upload/armazenamento S3, publica eventos RabbitMQ.
- Prescription Service (porta 8084): pipeline OCR/LLM, normalização, guarda estágios.
- Notification Service (porta 8085): e-mail/push e consumo de eventos.
- Plan Service (porta 8086): versionamento/auditoria de planos e integração com User Service.

## Execução (Docker Compose)
Pré-requisitos: Docker e Docker Compose instalados. Copie `.env.example` para `.env` e ajuste segredos/credenciais.

```zsh
# Na raiz do repositório
cp -n .env.example .env
# Suba toda a stack usando as variáveis do .env
docker compose --env-file .env up -d --build
```

- Postgres em 5432
- RabbitMQ em 5672 (UI: 15672)
- Gateway em http://localhost:8080
- Prometheus em http://localhost:9090
- Grafana em http://localhost:3000 (admin/admin)
- Jaeger UI em http://localhost:16686

Observações:
- O Gateway é protegido: exige JWT para qualquer rota que não seja `/auth/**` nem `/actuator/**`.
- O roteamento usa variáveis de ambiente (padrões definidos em `docker-compose.yml`).
- Prometheus coleta métricas em `/actuator/prometheus` para todos os serviços.

## Execução local (sem Docker)
Todos os serviços usam Postgres (veja `.env.example`), RabbitMQ e S3 (MinIO para dev). Inicie os serviços de infraestrutura ou use Docker para eles.

```zsh
# Em terminais independentes (necessário ter Postgres, RabbitMQ e MinIO rodando)
(cd api-gateway && mvn spring-boot:run)
(cd auth-service && mvn spring-boot:run)
(cd user-service && mvn spring-boot:run)
(cd file-service && mvn spring-boot:run)
(cd prescription-service && mvn spring-boot:run)
(cd plan-service && mvn spring-boot:run)
(cd notification-service && mvn spring-boot:run)
```

## Autenticação
- Auth Service emite tokens HS256 (`auth.jwt.secret`).
- O Gateway valida o JWT com a mesma chave simétrica e bloqueia acesso não autenticado.

## Observabilidade
- Actuator habilitado; Prometheus coleta `/actuator/prometheus` conforme `monitoring/prometheus.yml`.
- OpenTelemetry exporta traces para o Collector (OTLP gRPC), visualizados no Jaeger.

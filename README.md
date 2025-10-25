# Rehab AI Platform (Monorepo)

Plataforma de prescrição personalizada de exercícios de reabilitação. Arquitetura de microsserviços com Spring Boot 3.5.x, Spring Cloud Gateway, PostgreSQL, RabbitMQ e observabilidade (Prometheus/Grafana).

## Componentes
- API Gateway (porta 8080): roteamento, CORS, ponto central de autenticação.
- Auth Service (porta 8081): usuários, login/registro, emissão de JWT HS256.
- Patient Service (porta 8082): CRUD de pacientes (skeleton).
- File Service (porta 8083): upload/armazenamento S3, eventos RabbitMQ (skeleton).
- Prescription Service (porta 8084): orquestração IA e pipeline (skeleton).
- Plan Service (porta 8085): versionamento/auditoria de planos (skeleton).
- Notification Service (porta 8086): e-mail/push e consumo de eventos (skeleton).

## Execução (Docker Compose)
Pré-requisitos: Docker e Docker Compose instalados.

```zsh
# Na raiz do repositório
docker compose up --build
```

- Postgres em 5432
- RabbitMQ em 5672 (UI: 15672)
- Gateway em http://localhost:8080
- Prometheus em http://localhost:9090
- Grafana em http://localhost:3000 (admin/admin)

O Gateway usa `application-docker.yml` (profile `docker`) para rotear por hostname dos containers.

## Execução local (sem Docker)
Cada serviço expõe uma porta distinta. Inicie primeiro o Auth Service (H2 em memória por padrão).

```zsh
# Em terminais independentes
(cd api-gateway && ./mvnw spring-boot:run)
(cd auth-service && ./mvnw spring-boot:run)
(cd patient-service && ./mvnw spring-boot:run)
(cd file-service && ./mvnw spring-boot:run)
(cd prescription-service && ./mvnw spring-boot:run)
(cd plan-service && ./mvnw spring-boot:run)
(cd notification-service && ./mvnw spring-boot:run)
```

## Autenticação
- Auth Service emite tokens HS256 (segredo: `auth.jwt.secret` em `auth-service/src/main/resources/application.properties`).
- O Gateway, por enquanto, apenas encaminha o header `Authorization` e não valida JWT. Recomenda-se ativar validação no Gateway via `spring-boot-starter-oauth2-resource-server` e `jwk-set-uri` ou chave simétrica compartilhada.

## Observabilidade
- Todos os serviços possuem Actuator. O Prometheus coleta em `/actuator/prometheus`.
- As portas alvo estão em `monitoring/prometheus.yml`.

## Próximos passos sugeridos
1. Implementar endpoints reais (controllers, services) nos serviços Patient/File/Prescription/Plan/Notification.
2. Validar JWT no Gateway (ou manter validação apenas nos serviços downstream com Resource Server).
3. Adicionar MinIO para S3 local e configurar File/Prescription services.
4. Adicionar tracing (OpenTelemetry/Jaeger) e logs estruturados.
5. Consolidar perfis: separar `application.yml` para dev e `application-docker.yml` para docker em todos os serviços.


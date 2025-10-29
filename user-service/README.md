# User Service

Serviço de gerenciamento de usuários da plataforma (pacientes, clínicos e administradores).

## Variáveis de Ambiente

### Banco de Dados
- `SPRING_DATASOURCE_URL` - URL de conexão PostgreSQL (padrão: `jdbc:postgresql://db:5432/rehabdb`)
- `SPRING_DATASOURCE_USERNAME` - Usuário do banco (padrão: `postgres`)
- `SPRING_DATASOURCE_PASSWORD` - Senha do banco (padrão: `1234`)
- `DB_HOST` - Host do banco (padrão: `db`)
- `DB_PORT` - Porta do banco (padrão: `5432`)
- `DB_NAME` - Nome do banco (padrão: `rehabdb`)

### Servidor
- `SERVER_PORT` - Porta do serviço (padrão: `8082`)

### Observabilidade
- `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` - Endpoints expostos (padrão: `prometheus,health,info`)
- `TRACING_SAMPLING_PROBABILITY` - Probabilidade de sampling de traces (padrão: `1.0`)
- `OTEL_EXPORTER_OTLP_ENDPOINT` - Endpoint OpenTelemetry (padrão: `http://otel-collector:4317`)
- `OTEL_EXPORTER_OTLP_PROTOCOL` - Protocolo OTLP (padrão: `grpc`)

## Funcionalidades

- ✅ CRUD de usuários
- ✅ Suporte a roles (ADMIN, CLINICIAN, PATIENT)
- ✅ Filtros por role e status ativo
- ✅ Busca por email
- ✅ Alinhado com tabela `users` do banco

## Endpoints Principais

- `POST /users` - Criar usuário
- `GET /users` - Listar todos usuários
- `GET /users?role=PATIENT` - Filtrar por role
- `GET /users?activeOnly=true` - Apenas usuários ativos
- `GET /users/{id}` - Obter usuário por ID
- `GET /users/email/{email}` - Obter usuário por email
- `PUT /users/{id}` - Atualizar usuário
- `DELETE /users/{id}` - Deletar usuário

## Roles Disponíveis

- `PATIENT` - Paciente (padrão)
- `CLINICIAN` - Profissional de saúde
- `ADMIN` - Administrador do sistema

## Diferenças do Patient-Service

Este serviço substitui o `patient-service` com as seguintes melhorias:

1. **Tabela alinhada**: Usa tabela `users` ao invés de `patients` (conforme schema)
2. **Roles**: Suporte a ADMIN, CLINICIAN, PATIENT
3. **Campos corretos**: email, fullName, role, active (conforme schema)
4. **Sem campos extras**: Removidos dateOfBirth e gender (não estão no schema)



# 📚 API Endpoints - Documentação Completa

**Rehab AI Platform** **Data:** 03/11/2025  
**Versão:** 1.0.0  
**Base URL:** `http://localhost:8080`

---

## 📊 Visão Geral

| Métrica | Valor |
|---------|------:|
| 🌐 Total de Endpoints | 70+ |
| 🔧 Microserviços | 7 |
| 👥 Níveis de Acesso | 3 (PATIENT, CLINICIAN, ADMIN) |
| 🔐 Autenticação | JWT (Bearer Token) |
| 📦 Controllers Analisados | 10 |

---

## 🎨 Convenções Visuais

| Símbolo | Significado |
|:-------:|-------------|
| 🔑 | Path Parameter |
| 🔍 | Query Parameter |
| 📋 | Request Body |
| 📤 | Response Body |
| 🔒 | Autenticação Requerida |
| 🔓 | Endpoint Público |
| ⚠️ | Possíveis Erros |
| 💡 | Exemplo de Uso |
| 👤 | PATIENT |
| 👨‍⚕️ | CLINICIAN |
| 👑 | ADMIN |

---

## 📑 Índice

1. [Auth Service](#1-auth-service) - 5 endpoints
2. [User Service](#2-user-service) - 14 endpoints
3. [Consent Management](#3-consent-management) - 7 endpoints
4. [Patient Profile](#4-patient-profile) - 2 endpoints
5. [Patient History](#5-patient-history) - 10 endpoints
6. [Plan Service](#6-plan-service) - 13 endpoints
7. [File Service](#7-file-service) - 9 endpoints
8. [Prescription Workflow](#8-prescription-workflow) - 5 endpoints
9. [Prescription Lifecycle](#9-prescription-lifecycle) - 5 endpoints

---

## 1. Auth Service

**Porta:** 8081  
**Base Path:** `/auth`  
**Descrição:** Gerenciamento de autenticação e autorização

---

### 1.1 📝 Register (Registrar Usuário)

**Endpoint:** `POST /auth/register`  
**Acesso:** 🔓 Público (exceto para criar ADMIN)  
**Descrição:** Registra um novo usuário no sistema e retorna JWT + Refresh Token

#### 📋 Request Body
```json
{
  "email": "user@example.com",
  "password": "StrongPass123!",
  "fullName": "João Silva",
  "role": "PATIENT"
}
```
| Campo | Tipo | Obrigatório | Validação | Descrição |
|---|---|:---:|---|---|
| email | string | ✅ | Email válido | Email único do usuário |
| password | string | ✅ | Min 8 chars | Senha do usuário |
| fullName | string | ✅ | Min 3 chars | Nome completo |
| role | enum | ✅ | PATIENT, CLINICIAN, ADMIN | Função do usuário |

**Regras de Role:**
* 👤 `PATIENT`: Qualquer pessoa pode criar
* 👨‍⚕️ `CLINICIAN`: Qualquer pessoa pode criar
* 👑 `ADMIN`: Apenas ADMIN pode criar (exceto primeiro admin - bootstrap)

#### 📤 Response
**Status:** `201 Created`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 3600000,
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "refreshExpiresIn": 2592000000
}
```
| Campo | Tipo | Descrição |
|---|---|---|
| accessToken | string | JWT para autenticação |
| tokenType | string | Sempre "Bearer" |
| expiresIn | number | Tempo de expiração em ms (1 hora) |
| refreshToken | string | UUID do refresh token |
| refreshExpiresIn | number | Tempo de expiração do refresh em ms (30 dias) |

#### ⚠️ Possíveis Erros

| Status | Código | Descrição |
|---|---|---|
| 400 | `email_already_exists` | Email já cadastrado |
| 403 | `admin_only` | Tentou criar ADMIN sem permissão |
| 500 | `internal_error` | Erro interno do servidor |

#### 💡 Exemplo de Uso
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient@example.com",
    "password": "SecurePass123!",
    "fullName": "Maria Silva",
    "role": "PATIENT"
  }'
```

### 1.2 🔐 Login

**Endpoint:** `POST /auth/login`  
**Acesso:** 🔓 Público  
**Descrição:** Autentica um usuário e retorna JWT + Refresh Token

#### 📋 Request Body
```json
{
  "email": "user@example.com",
  "password": "StrongPass123!"
}
```
| Campo | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| email | string | ✅ | Email do usuário |
| password | string | ✅ | Senha do usuário |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 3600000,
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "refreshExpiresIn": 2592000000
}
```
#### ⚠️ Possíveis Erros

| Status | Código | Descrição |
|---|---|---|
| 401 | `invalid_credentials` | Email ou senha inválidos |
| 503 | `service_unavailable` | Serviço temporariamente indisponível |
| 500 | `internal_error` | Erro interno do servidor |

#### 💡 Exemplo de Uso
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient@example.com",
    "password": "SecurePass123!"
  }'
```

### 1.3 🔄 Refresh Token

**Endpoint:** `POST /auth/refresh`  
**Acesso:** 🔓 Público (requer refresh token)  
**Descrição:** Gera um novo access token usando o refresh token

#### 📋 Request Body
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```
| Campo | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| refreshToken | string | ✅ | UUID do refresh token |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 3600000,
  "refreshToken": "660e8400-e29b-41d4-a716-446655440001",
  "refreshExpiresIn": 2592000000
}
```
**Nota:** O refresh token é rotacionado (novo token gerado, antigo revogado)

#### ⚠️ Possíveis Erros

| Status | Código | Descrição |
|---|---|---|
| 401 | `expired_refresh_token` | Refresh token expirado |
| 401 | `invalid_refresh_token` | Refresh token inválido |

### 1.4 🚪 Logout

**Endpoint:** `POST /auth/logout`  
**Acesso:** 🔓 Público (requer refresh token)  
**Descrição:** Revoga um refresh token específico

#### 📋 Request Body
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```
#### 📤 Response
**Status:** `204 No Content`

#### ⚠️ Possíveis Erros

| Status | Código | Descrição |
|---|---|---|
| 400 | - | Refresh token inválido |

### 1.5 🚪🚪 Logout All

**Endpoint:** `POST /auth/logout_all`  
**Acesso:** 🔒 Requer autenticação  
**Descrição:** Revoga todos os refresh tokens do usuário (logout de todos os dispositivos)

#### 📋 Headers
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```
#### 📤 Response
**Status:** `204 No Content`

#### ⚠️ Possíveis Erros

| Status | Código | Descrição |
|---|---|---|
| 401 | - | Token inválido ou ausente |

#### 💡 Exemplo de Uso
```bash
curl -X POST http://localhost:8080/auth/logout_all \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

## 2. User Service

**Porta:** 8082  
**Base Path:** `/users`  
**Descrição:** Gerenciamento de usuários (CRUD completo)

### 2.1 🏥 Health Check

**Endpoint:** `GET /users/health`  
**Acesso:** 🔓 Público  
**Descrição:** Verifica se o serviço está funcionando

#### 📤 Response
**Status:** `200 OK`
```json
"ok"
```

### 2.2 ➕ Create User

**Endpoint:** `POST /users`  
**Acesso:** 🔓 Público (usado pelo auth-service)  
**Descrição:** Cria um novo usuário no banco de dados

#### 📋 Request Body
```json
{
  "email": "user@example.com",
  "passwordHash": "$2a$10$abcdefghijklmnopqrstuvwxyz",
  "fullName": "João Silva",
  "role": "PATIENT"
}
```
| Campo | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| email | string | ✅ | Email único |
| passwordHash | string | ✅ | Hash BCrypt da senha |
| fullName | string | ✅ | Nome completo |
| role | enum | ✅ | PATIENT, CLINICIAN, ADMIN |

#### 📤 Response
**Status:** `201 Created`  
**Location:** `/users/{id}`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "fullName": "João Silva",
  "role": "PATIENT",
  "active": true
}
```
#### ⚠️ Possíveis Erros

| Status | Descrição |
|---|---|
| 400 | Dados inválidos |
| 409 | Email já existe |

### 2.3 📋 List Users

**Endpoint:** `GET /users`  
**Acesso:** 🔒 CLINICIAN ou ADMIN  
**Descrição:** Lista todos os usuários com filtros opcionais

#### 🔍 Query Parameters

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| role | enum | ❌ | Filtrar por role (PATIENT, CLINICIAN, ADMIN) |
| activeOnly | boolean | ❌ | Listar apenas usuários ativos |

#### 📤 Response
**Status:** `200 OK`
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "fullName": "João Silva",
    "role": "PATIENT",
    "active": true
  }
]
```
#### 💡 Exemplos de Uso
```bash
# Listar todos os usuários
GET /users

# Listar apenas pacientes
GET /users?role=PATIENT

# Listar apenas usuários ativos
GET /users?activeOnly=true

# Listar pacientes ativos
GET /users?role=PATIENT&activeOnly=true
```
#### ⚠️ Possíveis Erros

| Status | Descrição |
|---|---|
| 403 | Usuário não é CLINICIAN ou ADMIN |

### 2.4 🔍 Get User by ID

**Endpoint:** `GET /users/{id}`  
**Acesso:** 🔒 Autenticado  
**Descrição:** Busca um usuário específico por ID

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do usuário |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "fullName": "João Silva",
  "role": "PATIENT",
  "active": true
}
```
#### ⚠️ Possíveis Erros

| Status | Descrição |
|---|---|
| 404 | Usuário não encontrado |

### 2.5 📧 Get User by Email

**Endpoint:** `GET /users/email/{email}`  
**Acesso:** 🔒 Autenticado  
**Descrição:** Busca um usuário específico por email

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| email | string | Email do usuário |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "fullName": "João Silva",
  "role": "PATIENT",
  "active": true
}
```

### 2.6 ✏️ Update User

**Endpoint:** `PUT /users/{id}`  
**Acesso:** 🔒 ADMIN  
**Descrição:** Atualiza dados de um usuário

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do usuário |

#### 📋 Request Body
```json
{
  "email": "newemail@example.com",
  "fullName": "João da Silva",
  "role": "CLINICIAN",
  "active": true
}
```
| Campo | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| email | string | ❌ | Novo email |
| fullName | string | ❌ | Novo nome |
| role | enum | ❌ | Nova role |
| active | boolean | ❌ | Status ativo |

#### 📤 Response
**Status:** `200 OK`

### 2.7 🗑️ Delete User

**Endpoint:** `DELETE /users/{id}`  
**Acesso:** 🔒 ADMIN  
**Descrição:** Deleta um usuário (não pode deletar a si mesmo)

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do usuário |

#### 📤 Response
**Status:** `204 No Content`

#### ⚠️ Possíveis Erros

| Status | Descrição |
|---|---|
| 403 | Tentou deletar a si mesmo |
| 404 | Usuário não encontrado |

### 2.8 ✅ Activate User

**Endpoint:** `POST /users/{id}/activate`  
**Acesso:** 🔒 ADMIN  
**Descrição:** Ativa um usuário inativo

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do usuário |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "active": true
}
```

### 2.9 ⛔ Deactivate User

**Endpoint:** `POST /users/{id}/deactivate`  
**Acesso:** 🔒 ADMIN  
**Descrição:** Desativa um usuário

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do usuário |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "active": false
}
```

### 2.10 👔 Change Role

**Endpoint:** `POST /users/{id}/role`  
**Acesso:** 🔒 ADMIN  
**Descrição:** Altera a função (role) de um usuário

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do usuário |

#### 📋 Request Body
```json
{
  "role": "CLINICIAN"
}
```
| Campo | Tipo | Obrigatório | Valores Aceitos |
|---|---|:---:|---|
| role | enum | ✅ | PATIENT, CLINICIAN, ADMIN |

#### 📤 Response
**Status:** `200 OK`

### 2.11 🔑 Change Password

**Endpoint:** `POST /users/{id}/password`  
**Acesso:** 🔒 ADMIN  
**Descrição:** Altera a senha de um usuário (hash BCrypt)

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do usuário |

#### 📋 Request Body
```json
{
  "passwordHash": "$2a$10$newHashHere..."
}
```
| Campo | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| passwordHash | string | ✅ | Novo hash BCrypt da senha |

#### 📤 Response
**Status:** `200 OK`

### 2.12 🔧 Internal Endpoints
Estes endpoints são para comunicação interna entre microserviços e **não são expostos pelo API Gateway.**

* **Get Credentials** `GET /internal/users/credentials?email={email}`  
  Retorna credenciais do usuário (usado pelo auth-service)

* **Count Users** `GET /internal/users/count`  
  Retorna total de usuários cadastrados

* **Check Admin Exists** `GET /internal/users/any-admin`  
  Verifica se existe pelo menos um ADMIN (para bootstrap)

## 3. Consent Management (LGPD/GDPR)

**Porta:** 8082  
**Base Path:** `/users/{userId}/consents`  
**Descrição:** Gerenciamento de consentimentos LGPD/GDPR para processamento de dados

### 3.1 📝 Create Consent

**Endpoint:** `POST /users/{userId}/consents`  
**Acesso:** 🔒 ADMIN ou próprio usuário  
**Descrição:** Cria um novo consentimento para o usuário

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário |

#### 📋 Request Body
```json
{
  "consentType": "DATA_PROCESSING",
  "granted": true,
  "purpose": "Processamento de dados para tratamento médico",
  "validityPeriod": 365,
  "dataCategories": ["health_data", "personal_info"]
}
```
| Campo | Tipo | Obrigatório | Valores Aceitos | Descrição |
|---|---|:---:|---|---|
| consentType | enum | ✅ | DATA_PROCESSING, DATA_SHARING, MARKETING, ANALYTICS | Tipo de consentimento |
| granted | boolean | ✅ | true/false | Se o consentimento foi concedido |
| purpose | string | ✅ | - | Finalidade do consentimento |
| validityPeriod | number | ❌ | Dias | Período de validade em dias |
| dataCategories | string[] | ❌ | Array de strings | Categorias de dados cobertas |

#### 📤 Response
**Status:** `201 Created`
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "consentType": "DATA_PROCESSING",
  "granted": true,
  "purpose": "Processamento de dados para tratamento médico",
  "validityPeriod": 365,
  "dataCategories": ["health_data", "personal_info"],
  "grantedAt": "2025-03-11T10:00:00Z",
  "expiresAt": "2026-03-11T10:00:00Z"
}
```
#### ⚠️ Possíveis Erros

| Status | Código | Descrição |
|---|---|---|
| 403 | `access_denied` | Sem permissão para criar consentimento |
| 404 | `user_not_found` | Usuário não existe |
| 400 | `invalid_consent_type` | Tipo de consentimento inválido |

### 3.2 🚫 Revoke Consent

**Endpoint:** `POST /users/{userId}/consents/revoke`  
**Acesso:** 🔒 ADMIN ou próprio usuário  
**Descrição:** Revoga um consentimento ativo

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário |

#### 📋 Request Body
```json
{
  "consentType": "DATA_PROCESSING",
  "revocationReason": "Usuário solicitou exclusão de dados"
}
```
| Campo | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| consentType | enum | ✅ | Tipo de consentimento a revogar |
| revocationReason | string | ✅ | Motivo da revogação |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440000",
  "consentType": "DATA_PROCESSING",
  "granted": false,
  "revokedAt": "2025-03-11T11:00:00Z",
  "revocationReason": "Usuário solicitou exclusão de dados"
}
```

### 3.3 📋 List Consents

**Endpoint:** `GET /users/{userId}/consents`  
**Acesso:** 🔒 ADMIN ou próprio usuário  
**Descrição:** Lista todos os consentimentos do usuário

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário |

#### 🔍 Query Parameters

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| consentType | enum | ❌ | Filtrar por tipo de consentimento |
| activeOnly | boolean | ❌ | Apenas consentimentos ativos |

#### 📤 Response
**Status:** `200 OK`
```json
[
  {
    "id": "660e8400-e29b-41d4-a716-446655440000",
    "consentType": "DATA_PROCESSING",
    "granted": true,
    "purpose": "Processamento de dados para tratamento médico",
    "grantedAt": "2025-03-11T10:00:00Z",
    "expiresAt": "2026-03-11T10:00:00Z",
    "revokedAt": null
  }
]
```

### 3.4 🔍 Get Latest Consent

**Endpoint:** `GET /users/{userId}/consents/latest`  
**Acesso:** 🔒 ADMIN ou próprio usuário  
**Descrição:** Obtém o consentimento mais recente de cada tipo

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "DATA_PROCESSING": {
    "id": "660e8400-e29b-41d4-a716-446655440000",
    "granted": true,
    "grantedAt": "2025-03-11T10:00:00Z",
    "expiresAt": "2026-03-11T10:00:00Z"
  },
  "DATA_SHARING": {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "granted": false,
    "grantedAt": "2025-02-01T09:00:00Z",
    "revokedAt": "2025-03-01T10:00:00Z"
  }
}
```

### 3.5 👤 Create My Consent

**Endpoint:** `POST /users/me/consents`  
**Acesso:** 🔒 Autenticado  
**Descrição:** Cria consentimento para o próprio usuário autenticado

#### 📋 Request Body
```json
{
  "consentType": "MARKETING",
  "granted": false,
  "purpose": "Receber comunicações de marketing"
}
```
#### 📤 Response
**Status:** `201 Created`

### 3.6 👤 Revoke My Consent

**Endpoint:** `POST /users/me/consents/revoke`  
**Acesso:** 🔒 Autenticado  
**Descrição:** Revoga consentimento do próprio usuário autenticado

#### 📋 Request Body
```json
{
  "consentType": "MARKETING",
  "revocationReason": "Não desejo receber emails promocionais"
}
```
#### 📤 Response
**Status:** `200 OK`

### 3.7 👤 List My Consents

**Endpoint:** `GET /users/me/consents`  
**Acesso:** 🔒 Autenticado  
**Descrição:** Lista consentimentos do próprio usuário autenticado

#### 📤 Response
**Status:** `200 OK`
```json
[
  {
    "id": "660e8400-e29b-41d4-a716-446655440000",
    "consentType": "DATA_PROCESSING",
    "granted": true,
    "purpose": "Processamento de dados para tratamento médico",
    "grantedAt": "2025-03-11T10:00:00Z",
    "expiresAt": "2026-03-11T10:00:00Z"
  }
]
```

## 4. Patient Profile

**Porta:** 8083  
**Base Path:** `/patients/{userId}/profile`  
**Descrição:** Gerenciamento de perfis de pacientes

### 4.1 🔍 Get Patient Profile

**Endpoint:** `GET /patients/{userId}/profile`  
**Acesso:** 🔒 PATIENT (próprio), CLINICIAN  
**Descrição:** Obtém o perfil completo do paciente

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário paciente |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "dateOfBirth": "1985-05-15",
  "gender": "MALE",
  "phone": "+5511999999999",
  "address": {
    "street": "Rua das Flores, 123",
    "city": "São Paulo",
    "state": "SP",
    "zipCode": "01234-567",
    "country": "Brasil"
  },
  "emergencyContact": {
    "name": "Maria Silva",
    "phone": "+5511888888888",
    "relationship": "Esposa"
  },
  "bloodType": "A_POSITIVE",
  "height": 175,
  "weight": 70,
  "createdAt": "2025-03-11T10:00:00Z",
  "updatedAt": "2025-03-11T10:00:00Z"
}
```
| Campo | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário |
| dateOfBirth | string | Data de nascimento (YYYY-MM-DD) |
| gender | enum | MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY |
| phone | string | Telefone com código do país |
| address | object | Endereço completo |
| emergencyContact | object | Contato de emergência |
| bloodType | enum | A_POSITIVE, A_NEGATIVE, B_POSITIVE, etc. |
| height | number | Altura em cm |
| weight | number | Peso em kg |

#### ⚠️ Possíveis Erros

| Status | Código | Descrição |
|---|---|---|
| 404 | `profile_not_found` | Perfil não encontrado |
| 403 | `access_denied` | Sem permissão para acessar perfil |

### 4.2 ✏️ Create/Update Patient Profile

**Endpoint:** `PUT /patients/{userId}/profile`  
**Acesso:** 🔒 PATIENT (próprio), CLINICIAN  
**Descrição:** Cria ou atualiza o perfil do paciente

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário paciente |

#### 📋 Request Body
```json
{
  "dateOfBirth": "1985-05-15",
  "gender": "MALE",
  "phone": "+5511999999999",
  "address": {
    "street": "Rua das Flores, 123",
    "city": "São Paulo",
    "state": "SP",
    "zipCode": "01234-567",
    "country": "Brasil"
  },
  "emergencyContact": {
    "name": "Maria Silva",
    "phone": "+5511888888888",
    "relationship": "Esposa"
  },
  "bloodType": "A_POSITIVE",
  "height": 175,
  "weight": 70
}
```
| Campo | Tipo | Obrigatório | Validação | Descrição |
|---|---|:---:|---|---|
| dateOfBirth | string | ✅ | Data válida | Data de nascimento |
| gender | enum | ✅ | Valores permitidos | Gênero |
| phone | string | ✅ | Formato internacional | Telefone |
| address | object | ✅ | - | Endereço completo |
| emergencyContact | object | ✅ | - | Contato de emergência |
| bloodType | enum | ❌ | Valores permitidos | Tipo sanguíneo |
| height | number | ❌ | > 0 | Altura em cm |
| weight | number | ❌ | > 0 | Peso em kg |

#### 📤 Response
**Status:** `200 OK` (atualização) ou `201 Created` (criação)
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "dateOfBirth": "1985-05-15",
  "gender": "MALE",
  "phone": "+5511999999999",
  "bloodType": "A_POSITIVE",
  "height": 175,
  "weight": 70,
  "updatedAt": "2025-03-11T11:00:00Z"
}
```
#### ⚠️ Possíveis Erros

| Status | Código | Descrição |
|---|---|---|
| 400 | `invalid_data` | Dados inválidos |
| 403 | `access_denied` | Sem permissão para editar perfil |

## 5. Patient History

**Porta:** 8083  
**Base Path:** `/patients/{userId}`  
**Descrição:** Gerenciamento completo do histórico médico do paciente

### 5.1 📝 Add Clinical Note

**Endpoint:** `POST /patients/{userId}/history/notes`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Adiciona uma nota clínica ao histórico do paciente

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário paciente |

#### 📋 Request Body
```json
{
  "title": "Consulta de acompanhamento",
  "content": "Paciente apresenta melhora significativa na mobilidade...",
  "noteType": "PROGRESS_NOTE",
  "tags": ["mobilidade", "melhora", "fisioterapia"]
}
```
| Campo | Tipo | Obrigatório | Valores Aceitos | Descrição |
|---|---|:---:|---|---|
| title | string | ✅ | - | Título da nota |
| content | string | ✅ | - | Conteúdo detalhado |
| noteType | enum | ✅ | PROGRESS_NOTE, ASSESSMENT, TREATMENT_PLANT | Tipo de nota |
| tags | string[] | ❌ | - | Tags para organização |

#### 📤 Response
**Status:** `201 Created`
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Consulta de acompanhamento",
  "content": "Paciente apresenta melhora significativa na mobilidade...",
  "noteType": "PROGRESS_NOTE",
  "tags": ["mobilidade", "melhora", "fisioterapia"],
  "authorId": "550e8400-e29b-41d4-a716-446655440001",
  "createdAt": "2025-03-11T10:00:00Z"
}
```

### 5.2 📋 List Clinical Notes

**Endpoint:** `GET /patients/{userId}/history/notes`  
**Acesso:** 🔒 PATIENT (próprio), CLINICIAN  
**Descrição:** Lista todas as notas clínicas do paciente

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário paciente |

#### 🔍 Query Parameters

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| noteType | enum | ❌ | Filtrar por tipo de nota |
| startDate | string | ❌ | Data inicial (YYYY-MM-DD) |
| endDate | string | ❌ | Data final (YYYY-MM-DD) |

#### 📤 Response
**Status:** `200 OK`
```json
[
  {
    "id": "770e8400-e29b-41d4-a716-446655440000",
    "title": "Consulta de acompanhamento",
    "content": "Paciente apresenta melhora significativa...",
    "noteType": "PROGRESS_NOTE",
    "tags": ["mobilidade", "melhora"],
    "authorId": "550e8400-e29b-41d4-a716-446655440001",
    "createdAt": "2025-03-11T10:00:00Z"
  }
]
```

### 5.3 🩺 Add Medical Condition

**Endpoint:** `POST /patients/{userId}/conditions`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Adiciona uma condição médica ao histórico

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário paciente |

#### 📋 Request Body
```json
{
  "name": "Artrose no joelho direito",
  "diagnosisDate": "2024-01-15",
  "status": "ACTIVE",
  "severity": "MODERATE",
  "description": "Degeneração articular no joelho direito grau II",
  "icd10Code": "M17.1"
}
```
| Campo | Tipo | Obrigatório | Valores Aceitos | Descrição |
|---|---|:---:|---|---|
| name | string | ✅ | - | Nome da condição |
| diagnosisDate | string | ✅ | Data válida | Data do diagnóstico |
| status | enum | ✅ | ACTIVE, RESOLVED, CHRONIC | Status da condição |
| severity | enum | ✅ | MILD, MODERATE, SEVERE | Gravidade |
| description | string | ❌ | - | Descrição detalhada |
| icd10Code | string | ❌ | Código ICD-10 | Código de classificação |

#### 📤 Response
**Status:** `201 Created`
```json
{
  "id": "780e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Artrose no joelho direito",
  "diagnosisDate": "2024-01-15",
  "status": "ACTIVE",
  "severity": "MODERATE",
  "description": "Degeneração articular no joelho direito grau II",
  "icd10Code": "M17.1",
  "createdAt": "2025-03-11T10:00:00Z"
}
```

### 5.4 📋 List Medical Conditions

**Endpoint:** `GET /patients/{userId}/conditions`  
**Acesso:** 🔒 PATIENT (próprio), CLINICIAN  
**Descrição:** Lista todas as condições médicas do paciente

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário paciente |

#### 🔍 Query Parameters

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| status | enum | ❌ | Filtrar por status |
| severity | enum | ❌ | Filtrar por gravidade |

#### 📤 Response
**Status:** `200 OK`
```json
[
  {
    "id": "780e8400-e29b-41d4-a716-446655440000",
    "name": "Artrose no joelho direito",
    "diagnosisDate": "2024-01-15",
    "status": "ACTIVE",
    "severity": "MODERATE",
    "icd10Code": "M17.1",
    "createdAt": "2025-03-11T10:00:00Z"
  }
]
```

### 5.5 🔍 Get Medical Condition

**Endpoint:** `GET /patients/{userId}/conditions/{id}`  
**Acesso:** 🔒 PATIENT (próprio), CLINICIAN  
**Descrição:** Obtém detalhes de uma condição médica específica

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário paciente |
| id | UUID | ID da condição médica |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "id": "780e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Artrose no joelho direito",
  "diagnosisDate": "2024-01-15",
  "status": "ACTIVE",
  "severity": "MODERATE",
  "description": "Degeneração articular no joelho direito grau II",
  "icd10Code": "M17.1",
  "createdAt": "2025-03-11T10:00:00Z",
  "updatedAt": "2025-03-11T10:00:00Z"
}
```

### 5.6 🗑️ Delete Medical Condition

**Endpoint:** `DELETE /patients/{userId}/conditions/{id}`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Remove uma condição médica do histórico

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário paciente |
| id | UUID | ID da condição médica |

#### 📤 Response
**Status:** `204 No Content`

### 5.7 🤧 Add Allergy

**Endpoint:** `POST /patients/{userId}/allergies`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Adiciona uma alergia ao histórico do paciente

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário paciente |

#### 📋 Request Body
```json
{
  "allergen": "Penicilina",
  "reaction": "Urticária e edema",
  "severity": "HIGH",
  "onsetDate": "2020-05-10",
  "notes": "Reação ocorreu 30min após administração"
}
```
| Campo | Tipo | Obrigatório | Valores Aceitos | Descrição |
|---|---|:---:|---|---|
| allergen | string | ✅ | - | Substância que causa alergia |
| reaction | string | ✅ | - | Descrição da reação |
| severity | enum | ✅ | LOW, MEDIUM, HIGH, CRITICAL | Gravidade da alergia |
| onsetDate | string | ❌ | Data válida | Data do primeiro episódio |
| notes | string | ❌ | - | Observações adicionais |

#### 📤 Response
**Status:** `201 Created`
```json
{
  "id": "790e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "allergen": "Penicilina",
  "reaction": "Urticária e edema",
  "severity": "HIGH",
  "onsetDate": "2020-05-10",
  "notes": "Reação ocorreu 30min após administração",
  "createdAt": "2025-03-11T10:00:00Z"
}
```

### 5.8 📋 List Allergies

**Endpoint:** `GET /patients/{userId}/allergies`  
**Acesso:** 🔒 PATIENT (próprio), CLINICIAN  
**Descrição:** Lista todas as alergias do paciente

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário paciente |

#### 📤 Response
**Status:** `200 OK`
```json
[
  {
    "id": "790e8400-e29b-41d4-a716-446655440000",
    "allergen": "Penicilina",
    "reaction": "Urticária e edema",
    "severity": "HIGH",
    "onsetDate": "2020-05-10",
    "createdAt": "2025-03-11T10:00:00Z"
  }
]
```

### 5.9 💊 Add Medication

**Endpoint:** `POST /patients/{userId}/medications`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Adiciona uma medicação ao histórico

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário paciente |

#### 📋 Request Body
```json
{
  "name": "Ibuprofeno",
  "dosage": "400mg",
  "frequency": "8/8h",
  "route": "ORAL",
  "startDate": "2025-03-10",
  "endDate": "2025-03-17",
  "purpose": "Controle de dor e inflamação",
  "prescribingClinician": "Dr. Silva"
}
```
| Campo | Tipo | Obrigatório | Valores Aceitos | Descrição |
|---|---|:---:|---|---|
| name | string | ✅ | - | Nome da medicação |
| dosage | string | ✅ | - | Dosagem e forma |
| frequency | string | ✅ | - | Frequência de administração |
| route | enum | ✅ | ORAL, TOPICAL, INJECTION, etc. | Via de administração |
| startDate | string | ✅ | Data válida | Data de início |
| endDate | string | ❌ | Data válida | Data de término |
| purpose | string | ❌ | - | Finalidade do tratamento |
| prescribingClinician | string | ❌ | - | Nome do prescritor |

#### 📤 Response
**Status:** `201 Created`
```json
{
  "id": "7a0e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Ibuprofeno",
  "dosage": "400mg",
  "frequency": "8/8h",
  "route": "ORAL",
  "startDate": "2025-03-10",
  "endDate": "2025-03-17",
  "purpose": "Controle de dor e inflamação",
  "prescribingClinician": "Dr. Silva",
  "createdAt": "2025-03-11T10:00:00Z"
}
```

### 5.10 📋 List Medications

**Endpoint:** `GET /patients/{userId}/medications`  
**Acesso:** 🔒 PATIENT (próprio), CLINICIAN  
**Descrição:** Lista todas as medicações do paciente

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário paciente |

#### 🔍 Query Parameters

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| activeOnly | boolean | ❌ | Apenas medicações ativas |

#### 📤 Response
**Status:** `200 OK`
```json
[
  {
    "id": "7a0e8400-e29b-41d4-a716-446655440000",
    "name": "Ibuprofeno",
    "dosage": "400mg",
    "frequency": "8/8h",
    "route": "ORAL",
    "startDate": "2025-03-10",
    "endDate": "2025-03-17",
    "purpose": "Controle de dor e inflamação",
    "createdAt": "2025-03-11T10:00:00Z"
  }
]
```

### 5.11 ❤️ Record Vital Signs

**Endpoint:** `POST /patients/{userId}/vitals`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Registra sinais vitais do paciente

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário paciente |

#### 📋 Request Body
```json
{
  "vitalType": "BLOOD_PRESSURE",
  "value": "120/80",
  "unit": "mmHg",
  "measuredAt": "2025-03-11T10:00:00Z",
  "notes": "Pressão arterial dentro da normalidade"
}
```
| Campo | Tipo | Obrigatório | Valores Aceitos | Descrição |
|---|---|:---:|---|---|
| vitalType | enum | ✅ | BLOOD_PRESSURE, HEART_RATE, TEMPERATURE, etc. | Tipo de sinal vital |
| value | string | ✅ | - | Valor medido |
| unit | string | ✅ | - | Unidade de medida |
| measuredAt | string | ✅ | Data/hora válida | Data/hora da medição |
| notes | string | ❌ | - | Observações |

#### 📤 Response
**Status:** `201 Created`
```json
{
  "id": "7b0e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "vitalType": "BLOOD_PRESSURE",
  "value": "120/80",
  "unit": "mmHg",
  "measuredAt": "2025-03-11T10:00:00Z",
  "notes": "Pressão arterial dentro da normalidade",
  "recordedAt": "2025-03-11T10:05:00Z"
}
```

### 5.12 📊 List Vital Signs

**Endpoint:** `GET /patients/{userId}/vitals`  
**Acesso:** 🔒 PATIENT (próprio), CLINICIAN  
**Descrição:** Lista histórico de sinais vitais

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário paciente |

#### 🔍 Query Parameters

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| vitalType | enum | ❌ | Filtrar por tipo |
| startDate | string | ❌ | Data inicial |
| endDate | string | ❌ | Data final |

#### 📤 Response
**Status:** `200 OK`
```json
[
  {
    "id": "7b0e8400-e29b-41d4-a716-446655440000",
    "vitalType": "BLOOD_PRESSURE",
    "value": "120/80",
    "unit": "mmHg",
    "measuredAt": "2025-03-11T10:00:00Z",
    "recordedAt": "2025-03-11T10:05:00Z"
  }
]
```

## 6. Plan Service

**Porta:** 8084  
**Base Path:** `/plans`  
**Descrição:** Gerenciamento de planos de reabilitação

### 6.1 📝 Create Rehabilitation Plan

**Endpoint:** `POST /plans`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Cria um novo plano de reabilitação

#### 📋 Request Body
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Plano de Reabilitação Pós-Artroscopia",
  "description": "Plano para recuperação após artroscopia do joelho direito",
  "diagnosis": "Lesão do menisco medial",
  "exercises": [
    {
      "name": "Flexão de joelho sentado",
      "description": "Flexionar joelho até 90 graus",
      "sets": 3,
      "repetitions": 10,
      "duration": null,
      "frequency": "DIARIO"
    }
  ],
  "goals": [
    "Recuperar amplitude de movimento completa",
    "Fortalecer musculatura do quadríceps"
  ],
  "duration": 30,
  "frequency": "DIARIO",
  "startDate": "2025-03-12",
  "endDate": "2025-04-11"
}
```
| Campo | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| userId | UUID | ✅ | ID do paciente |
| title | string | ✅ | Título do plano |
| description | string | ✅ | Descrição detalhada |
| diagnosis | string | ✅ | Diagnóstico relacionado |
| exercises | array | ✅ | Lista de exercícios |
| goals | array | ✅ | Objetivos do plano |
| duration | number | ✅ | Duração em dias |
| frequency | enum | ✅ | DIARIO, SEMANAL, etc. |
| startDate | string | ✅ | Data de início |
| endDate | string | ❌ | Data de término |

#### 📤 Response
**Status:** `201 Created`
```json
{
  "id": "8a0e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Plano de Reabilitação Pós-Artroscopia",
  "description": "Plano para recuperação após artroscopia do joelho direito",
  "diagnosis": "Lesão do menisco medial",
  "exercises": [
    {
      "name": "Flexão de joelho sentado",
      "description": "Flexionar joelho até 90 graus",
      "sets": 3,
      "repetitions": 10,
      "duration": null,
      "frequency": "DIARIO"
    }
  ],
  "goals": [
    "Recuperar amplitude de movimento completa",
    "Fortalecer musculatura do quadríceps"
  ],
  "duration": 30,
  "frequency": "DIARIO",
  "startDate": "2025-03-12",
  "endDate": "2025-04-11",
  "status": "DRAFT",
  "version": 1,
  "createdBy": "550e8400-e29b-41d4-a716-446655440001",
  "createdAt": "2025-03-11T10:00:00Z"
}
```

### 6.2 🔍 Get Plan by ID

**Endpoint:** `GET /plans/{id}`  
**Acesso:** 🔒 PATIENT (próprio), CLINICIAN  
**Descrição:** Obtém um plano específico por ID

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do plano |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "id": "8a0e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Plano de Reabilitação Pós-Artroscopia",
  "description": "Plano para recuperação após artroscopia do joelho direito",
  "diagnosis": "Lesão do menisco medial",
  "exercises": [...],
  "goals": [...],
  "duration": 30,
  "frequency": "DIARIO",
  "startDate": "2025-03-12",
  "endDate": "2025-04-11",
  "status": "DRAFT",
  "version": 1,
  "createdBy": "550e8400-e29b-41d4-a716-446655440001",
  "createdAt": "2025-03-11T10:00:00Z",
  "updatedAt": "2025-03-11T10:00:00Z"
}
```

### 6.3 ✏️ Update Plan

**Endpoint:** `PUT /plans/{id}`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Atualiza um plano existente

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do plano |

#### 📋 Request Body
```json
{
  "title": "Plano de Reabilitação Pós-Artroscopia - Atualizado",
  "description": "Plano revisado com novos exercícios",
  "exercises": [...],
  "goals": [...],
  "duration": 45
}
```
#### 📤 Response
**Status:** `200 OK`

### 6.4 📋 List User Plans

**Endpoint:** `GET /plans/user/{userId}`  
**Acesso:** 🔒 PATIENT (próprio), CLINICIAN  
**Descrição:** Lista todos os planos de um usuário

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário |

#### 📤 Response
**Status:** `200 OK`
```json
[
  {
    "id": "8a0e8400-e29b-41d4-a716-446655440000",
    "title": "Plano de Reabilitação Pós-Artroscopia",
    "status": "DRAFT",
    "version": 1,
    "startDate": "2025-03-12",
    "endDate": "2025-04-11",
    "createdAt": "2025-03-11T10:00:00Z"
  }
]
```

### 6.5 🔍 List Plans by Status

**Endpoint:** `GET /plans/user/{userId}/status/{status}`  
**Acesso:** 🔒 PATIENT (próprio), CLINICIAN  
**Descrição:** Lista planos de um usuário filtrados por status

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| userId | UUID | ID do usuário |
| status | enum | Status do plano |

#### 📤 Response
**Status:** `200 OK`

### 6.6 📚 List Plan Versions

**Endpoint:** `GET /plans/prescription/{id}/versions`  
**Acesso:** 🔒 PATIENT (próprio), CLINICIAN  
**Descrição:** Lista todas as versões de um plano

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID da prescrição original |

#### 📤 Response
**Status:** `200 OK`
```json
[
  {
    "id": "8a0e8400-e29b-41d4-a716-446655440000",
    "version": 1,
    "status": "DRAFT",
    "createdAt": "2025-03-11T10:00:00Z",
    "createdBy": "550e8400-e29b-41d4-a716-446655440001"
  },
  {
    "id": "8a0e8400-e29b-41d4-a716-446655440001",
    "version": 2,
    "status": "APPROVED",
    "createdAt": "2025-03-11T11:00:00Z",
    "createdBy": "550e8400-e29b-41d4-a716-446655440001"
  }
]
```

### 6.7 🔍 Get Latest Plan Version

**Endpoint:** `GET /plans/prescription/{id}/latest`  
**Acesso:** 🔒 PATIENT (próprio), CLINICIAN  
**Descrição:** Obtém a versão mais recente de um plano

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID da prescrição original |

#### 📤 Response
**Status:** `200 OK`

### 6.8 📋 Get Plan Audit History

**Endpoint:** `GET /plans/{id}/audit`  
**Acesso:** 🔒 PATIENT (próprio), CLINICIAN  
**Descrição:** Obtém o histórico completo de auditoria de um plano

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do plano |

#### 📤 Response
**Status:** `200 OK`
```json
[
  {
    "action": "CREATE",
    "version": 1,
    "changes": "Plano criado inicialmente",
    "userId": "550e8400-e29b-41d4-a716-446655440001",
    "timestamp": "2025-03-11T10:00:00Z"
  },
  {
    "action": "UPDATE",
    "version": 2,
    "changes": "Exercícios atualizados",
    "userId": "550e8400-e29b-41d4-a716-446655440001",
    "timestamp": "2025-03-11T11:00:00Z"
  }
]
```

### 6.9 ✅ Approve Plan

**Endpoint:** `POST /plans/{id}/approve`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Aprova um plano (muda status para APPROVED)

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do plano |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "id": "8a0e8400-e29b-41d4-a716-446655440000",
  "status": "APPROVED",
  "approvedAt": "2025-03-11T12:00:00Z",
  "approvedBy": "550e8400-e29b-41d4-a716-446655440001"
}
```

### 6.10 📁 Archive Plan

**Endpoint:** `POST /plans/{id}/archive`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Arquiva um plano (muda status para ARCHIVED)

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do plano |

#### 📤 Response
**Status:** `200 OK`

### 6.11 🔄 Create New Version

**Endpoint:** `POST /plans/{id}/new-version`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Cria uma nova versão de um plano existente

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do plano base |

#### 📋 Request Body
```json
{
  "changes": "Adicionados novos exercícios de fortalecimento",
  "exercises": [...],
  "goals": [...]
}
```
#### 📤 Response
**Status:** `201 Created`
```json
{
  "id": "8a0e8400-e29b-41d4-a716-446655440002",
  "previousVersionId": "8a0e8400-e29b-41d4-a716-446655440001",
  "version": 3,
  "status": "DRAFT",
  "createdAt": "2025-03-11T13:00:00Z"
}
```

### 6.12 ↩️ Rollback Plan

**Endpoint:** `POST /plans/{id}/rollback`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Reverte para uma versão anterior do plano

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do plano atual |

#### 📋 Request Body
```json
{
  "targetVersion": 2,
  "reason": "Versão atual possui exercícios muito avançados"
}
```
#### 📤 Response
**Status:** `200 OK`

## 7. File Service

**Porta:** 8085  
**Base Path:** `/files`  
**Descrição:** Gerenciamento de upload, download e anonimização de arquivos

### 7.1 🏥 Health Check

**Endpoint:** `GET /files/health`  
**Acesso:** 🔓 Público  
**Descrição:** Verifica se o serviço está funcionando

#### 📤 Response
**Status:** `200 OK`
```json
"ok"
```

### 7.2 📤 Upload File

**Endpoint:** `POST /files/upload`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Faz upload de um arquivo (prescrição médica, exames, etc.)

#### 📋 Form Data

| Campo | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| file | MultipartFile | ✅ | Arquivo a ser enviado |
| userId | UUID | ✅ | ID do paciente proprietário |
| fileType | enum | ❌ | PRESCRIPTION, EXAM, REPORT, OTHER |
| description | string | ❌ | Descrição do arquivo |

**Formatos Suportados:** `PDF, JPG, JPEG, PNG, DOC, DOCX`  
**Tamanho Máximo:** `10MB`

#### 📤 Response
**Status:** `201 Created`
```json
{
  "id": "9a0e8400-e29b-41d4-a716-446655440000",
  "originalName": "prescricao_medica.pdf",
  "storedName": "9a0e8400-e29b-41d4-a716-446655440000.pdf",
  "fileType": "PRESCRIPTION",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "size": 2048576,
  "mimeType": "application/pdf",
  "status": "UPLOADED",
  "description": "Prescrição médica para fisioterapia",
  "uploadedBy": "550e8400-e29b-41d4-a716-446655440001",
  "uploadedAt": "2025-03-11T10:00:00Z"
}
```

### 7.3 🎭 Pseudonymize File

**Endpoint:** `POST /files/{id}/pseudonymize`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Aplica anonimização/pseudonimização em um arquivo

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do arquivo |

#### 📋 Request Body
```json
{
  "pseudonymizationType": "FULL",
  "fieldsToAnonymize": ["patientName", "cpf", "address"],
  "retentionPeriod": 365
}
```
| Campo | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| pseudonymizationType | enum | ✅ | FULL, PARTIAL, SELECTIVE |
| fieldsToAnonymize | string[] | ❌ | Campos específicos para anonimizar |
| retentionPeriod | number | ❌ | Período de retenção em dias |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "id": "9a0e8400-e29b-41d4-a716-446655440000",
  "status": "ANONYMIZED",
  "anonymizedAt": "2025-03-11T10:30:00Z",
  "anonymizedBy": "550e8400-e29b-41d4-a716-446655440001",
  "pseudonymizationType": "FULL"
}
```

### 7.4 📋 Get Anonymization Logs

**Endpoint:** `GET /files/{id}/anonymization-logs`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Obtém os logs de anonimização de um arquivo

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do arquivo |

#### 📤 Response
**Status:** `200 OK`
```json
[
  {
    "action": "ANONYMIZE",
    "pseudonymizationType": "FULL",
    "performedBy": "550e8400-e29b-41d4-a716-446655440001",
    "performedAt": "2025-03-11T10:30:00Z",
    "fieldsAnonymized": ["patientName", "cpf", "address"]
  }
]
```

### 7.5 🔍 Get File Metadata

**Endpoint:** `GET /files/{id}`  
**Acesso:** 🔒 Proprietário, CLINICIAN  
**Descrição:** Obtém os metadados de um arquivo

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do arquivo |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "id": "9a0e8400-e29b-41d4-a716-446655440000",
  "originalName": "prescricao_medica.pdf",
  "fileType": "PRESCRIPTION",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "size": 2048576,
  "mimeType": "application/pdf",
  "status": "ANONYMIZED",
  "description": "Prescrição médica para fisioterapia",
  "uploadedBy": "550e8400-e29b-41d4-a716-446655440001",
  "uploadedAt": "2025-03-11T10:00:00Z",
  "anonymizedAt": "2025-03-11T10:30:00Z"
}
```

### 7.6 📚 List Files

**Endpoint:** `GET /files`  
**Acesso:** 🔒 Autenticado  
**Descrição:** Lista arquivos de acordo com as permissões do usuário

#### 🔍 Query Parameters

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| userId | UUID | ❌ | Filtrar por usuário (apenas CLINICIAN/ADMIN) |
| fileType | enum | ❌ | Filtrar por tipo de arquivo |
| status | enum | ❌ | Filtrar por status |

#### 📤 Response
**Status:** `200 OK`
```json
[
  {
    "id": "9a0e8400-e29b-41d4-a716-446655440000",
    "originalName": "prescricao_medica.pdf",
    "fileType": "PRESCRIPTION",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "size": 2048576,
    "status": "ANONYMIZED",
    "uploadedAt": "2025-03-11T10:00:00Z"
  }
]
```

### 7.7 📥 Download File

**Endpoint:** `GET /files/{id}/download`  
**Acesso:** 🔒 Proprietário, CLINICIAN  
**Descrição:** Faz download do arquivo

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do arquivo |

#### 📤 Response
**Status:** `200 OK`  
**Headers:**
```text
Content-Type: [mime-type do arquivo]
Content-Disposition: attachment; filename="[nome-original]"
Content-Length: [tamanho do arquivo]
```
**Body:** Stream do arquivo

#### ⚠️ Possíveis Erros

| Status | Código | Descrição |
|---|---|---|
| 404 | `file_not_found` | Arquivo não encontrado |
| 403 | `access_denied` | Sem permissão para acessar arquivo |
| 410 | `file_deleted` | Arquivo foi deletado |

### 7.8 🗑️ Delete File

**Endpoint:** `DELETE /files/{id}`  
**Acesso:** 🔒 Proprietário, CLINICIAN  
**Descrição:** Deleta um arquivo (soft delete)

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do arquivo |

#### 📤 Response
**Status:** `204 No Content`

## 8. Prescription Workflow

**Porta:** 8086  
**Base Path:** `/prescriptions`  
**Descrição:** Gerenciamento do workflow de processamento de prescrições médicas

### 8.1 🔍 Get Latest Workflow

**Endpoint:** `GET /prescriptions/workflows/latest`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Obtém o workflow mais recente para um fileId

#### 🔍 Query Parameters

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| fileId | UUID | ✅ | ID do arquivo da prescrição |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440000",
  "fileId": "9a0e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "currentStage": "NORMALIZATION",
  "status": "RUNNING",
  "stages": [
    {
      "stage": "EXTRACTION",
      "status": "COMPLETED",
      "startedAt": "2025-03-11T10:05:00Z",
      "completedAt": "2025-03-11T10:10:00Z"
    },
    {
      "stage": "NORMALIZATION",
      "status": "RUNNING",
      "startedAt": "2025-03-11T10:10:00Z",
      "completedAt": null
    }
  ],
  "createdAt": "2025-03-11T10:00:00Z",
  "updatedAt": "2025-03-11T10:10:00Z"
}
```

### 8.2 🔍 Get Latest Stages

**Endpoint:** `GET /prescriptions/stages/latest`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Obtém os últimos estágios processados para um fileId

#### 🔍 Query Parameters

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| fileId | UUID | ✅ | ID do arquivo da prescrição |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "extraction": {
    "id": "ab0e8400-e29b-41d4-a716-446655440000",
    "status": "COMPLETED",
    "result": {
      "extractedText": "Prescrição: Fisioterapia...",
      "confidence": 0.95,
      "entities": ["exercicios", "frequencia", "duracao"]
    },
    "startedAt": "2025-03-11T10:05:00Z",
    "completedAt": "2025-03-11T10:10:00Z"
  },
  "normalization": {
    "id": "ac0e8400-e29b-41d4-a716-446655440000",
    "status": "RUNNING",
    "startedAt": "2025-03-11T10:10:00Z",
    "completedAt": null
  }
}
```

### 8.3 🔍 Get Extraction Details

**Endpoint:** `GET /prescriptions/extractions/{id}`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Obtém detalhes completos de uma extração (OCR)

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID da extração |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "id": "ab0e8400-e29b-41d4-a716-446655440000",
  "workflowId": "aa0e8400-e29b-41d4-a716-446655440000",
  "fileId": "9a0e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "input": {
    "fileId": "9a0e8400-e29b-41d4-a716-446655440000",
    "serviceUsed": "TEXTRACT"
  },
  "result": {
    "extractedText": "Prescrição Médica\nPaciente: João Silva\nExercícios: Flexão de joelho 3x10\nFrequência: Diária\nDuração: 30 dias",
    "rawOutput": {...},
    "confidence": 0.95,
    "pagesProcessed": 1,
    "entitiesFound": [
      {
        "type": "EXERCISE",
        "value": "Flexão de joelho",
        "confidence": 0.92
      },
      {
        "type": "FREQUENCY",
        "value": "Diária",
        "confidence": 0.88
      }
    ]
  },
  "metadata": {
    "serviceVersion": "1.0.0",
    "processingTime": 5000,
    "startedAt": "2025-03-11T10:05:00Z",
    "completedAt": "2025-03-11T10:10:00Z"
  }
}
```

### 8.4 🔍 Get Normalization Details

**Endpoint:** `GET /prescriptions/normalizations/{id}`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Obtém detalhes completos de uma normalização

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID da normalização |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "id": "ac0e8400-e29b-41d4-a716-446655440000",
  "workflowId": "aa0e8400-e29b-41d4-a716-446655440000",
  "extractionId": "ab0e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "input": {
    "extractedText": "Prescrição Médica\nPaciente: João Silva\nExercícios: Flexão de joelho 3x10\nFrequência: Diária\nDuração: 30 dias",
    "entities": [...]
  },
  "result": {
    "normalizedData": {
      "exercises": [
        {
          "name": "Flexão de joelho",
          "sets": 3,
          "repetitions": 10,
          "frequency": "DIARIO",
          "duration": 30
        }
      ],
      "goals": ["Fortalecimento muscular", "Recuperação da amplitude"],
      "duration": 30,
      "frequency": "DIARIO"
    },
    "confidence": 0.88,
    "normalizationRulesApplied": ["exercise_pattern", "frequency_mapping"]
  },
  "metadata": {
    "modelUsed": "rehab-ai-normalizer-v1",
    "processingTime": 3000,
    "startedAt": "2025-03-11T10:10:00Z",
    "completedAt": "2025-03-11T10:15:00Z"
  }
}
```

### 8.5 🔍 Get Generated Prescription

**Endpoint:** `GET /prescriptions/generated/{id}`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Obtém a prescrição gerada após processamento completo

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID da geração |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "id": "ad0e8400-e29b-41d4-a716-446655440000",
  "workflowId": "aa0e8400-e29b-41d4-a716-446655440000",
  "normalizationId": "ac0e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "result": {
    "planData": {
      "title": "Plano de Reabilitação Baseado na Prescrição",
      "exercises": [
        {
          "name": "Flexão de joelho",
          "description": "Flexionar joelho mantendo o alinhamento",
          "sets": 3,
          "repetitions": 10,
          "frequency": "DIARIO"
        }
      ],
      "goals": [
        "Fortalecimento do quadríceps",
        "Melhora da amplitude articular"
      ],
      "duration": 30,
      "frequency": "DIARIO",
      "restrictions": ["Evitar impacto", "Não ultrapassar ângulo de 90°"]
    },
    "confidence": 0.85,
    "suggestions": ["Incluir exercícios de alongamento", "Monitorar dor"]
  },
  "metadata": {
    "aiModel": "rehab-ai-generator-v1",
    "processingTime": 4000,
    "startedAt": "2025-03-11T10:15:00Z",
    "completedAt": "2025-03-11T10:20:00Z"
  }
}
```

### 8.6 📊 Get AI Traces

**Endpoint:** `GET /prescriptions/traces`  
**Acesso:** 🔒 ADMIN  
**Descrição:** Obtém traces de IA para debugging (Bedrock/Textract)

#### 🔍 Query Parameters

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| workflowId | UUID | ❌ | Filtrar por workflow |
| service | enum | ❌ | TEXTRACT, BEDROCK |
| startDate | string | ❌ | Data inicial |
| endDate | string | ❌ | Data final |

#### 📤 Response
**Status:** `200 OK`
```json
[
  {
    "id": "ae0e8400-e29b-41d4-a716-446655440000",
    "workflowId": "aa0e8400-e29b-41d4-a716-446655440000",
    "service": "TEXTRACT",
    "operation": "AnalyzeDocument",
    "input": {...},
    "output": {...},
    "duration": 2500,
    "timestamp": "2025-03-11T10:05:00Z",
    "success": true
  }
]
```

## 9. Prescription Lifecycle

**Porta:** 8086  
**Base Path:** `/prescriptions/workflows`  
**Descrição:** Controle completo do ciclo de vida do processamento de prescrições

### 9.1 🚀 Start Workflow

**Endpoint:** `POST /prescriptions/workflows`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Inicia um novo workflow de processamento de prescrição

#### 📋 Request Body
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "fileId": "9a0e8400-e29b-41d4-a716-446655440000",
  "traceId": "trace-123456789",
  "priority": "NORMAL",
  "callbackUrl": "[https://clinica.com/webhooks/prescription-completed](https://clinica.com/webhooks/prescription-completed)"
}
```
| Campo | Tipo | Obrigatório | Valores Aceitos | Descrição |
|---|---|:---:|---|---|
| userId | UUID | ✅ | - | ID do paciente |
| fileId | UUID | ✅ | - | ID do arquivo da prescrição |
| traceId | string | ✅ | - | ID de rastreamento |
| priority | enum | ❌ | LOW, NORMAL, HIGH | Prioridade do processamento |
| callbackUrl | string | ❌ | URL válida | Webhook para notificação |

#### 📤 Response
**Status:** `201 Created`
```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "fileId": "9a0e8400-e29b-41d4-a716-446655440000",
  "traceId": "trace-123456789",
  "currentStage": "EXTRACTION",
  "status": "RUNNING",
  "priority": "NORMAL",
  "stages": [
    {
      "stage": "EXTRACTION",
      "status": "PENDING",
      "startedAt": null,
      "completedAt": null
    },
    {
      "stage": "NORMALIZATION",
      "status": "PENDING",
      "startedAt": null,
      "completedAt": null
    },
    {
      "stage": "GENERATION",
      "status": "PENDING",
      "startedAt": null,
      "completedAt": null
    }
  ],
  "createdAt": "2025-03-11T10:00:00Z"
}
```

### 9.2 ⏩ Advance Stage

**Endpoint:** `POST /prescriptions/workflows/{id}/advance`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Avança manually para o próximo estágio do workflow

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do workflow |

#### 📋 Request Body
```json
{
  "targetStage": "NORMALIZATION",
  "reason": "Extração concluída com sucesso"
}
```
| Campo | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| targetStage | enum | ✅ | Estágio destino |
| reason | string | ❌ | Motivo do avanço |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440000",
  "currentStage": "NORMALIZATION",
  "previousStage": "EXTRACTION",
  "status": "RUNNING",
  "updatedAt": "2025-03-11T10:10:00Z"
}
```

### 9.3 ✅ Complete Workflow

**Endpoint:** `POST /prescriptions/workflows/{id}/complete`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Marca o workflow como concluído com sucesso

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do workflow |

#### 📋 Request Body
```json
{
  "generatedPlanId": "8a0e8400-e29b-41d4-a716-446655440000",
  "notes": "Workflow concluído com sucesso, plano gerado automaticamente"
}
```
| Campo | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| generatedPlanId | UUID | ❌ | ID do plano gerado |
| notes | string | ❌ | Observações finais |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "completedAt": "2025-03-11T10:30:00Z",
  "totalProcessingTime": 1800000,
  "generatedPlanId": "8a0e8400-e29b-41d4-a716-446655440000"
}
```

### 9.4 ❌ Fail Workflow

**Endpoint:** `POST /prescriptions/workflows/{id}/fail`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Marca o workflow como falhou

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do workflow |

#### 📋 Request Body
```json
{
  "errorCode": "OCR_FAILED",
  "errorMessage": "Falha na extração de texto do documento",
  "retryable": true
}
```
| Campo | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| errorCode | string | ✅ | Código do erro |
| errorMessage | string | ✅ | Descrição do erro |
| retryable | boolean | ❌ | Se pode ser tentado novamente |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440000",
  "status": "FAILED",
  "failedAt": "2025-03-11T10:25:00Z",
  "errorCode": "OCR_FAILED",
  "errorMessage": "Falha na extração de texto do documento",
  "retryable": true
}
```

### 9.5 🔄 Retry Workflow

**Endpoint:** `POST /prescriptions/workflows/{id}/retry`  
**Acesso:** 🔒 CLINICIAN  
**Descrição:** Tenta reprocessar um workflow que falhou

#### 🔑 Path Parameters

| Parâmetro | Tipo | Descrição |
|---|---|---|
| id | UUID | ID do workflow |

#### 📋 Request Body
```json
{
  "fromStage": "EXTRACTION",
  "useAlternativeService": true
}
```
| Campo | Tipo | Obrigatório | Descrição |
|---|---|:---:|---|
| fromStage | enum | ❌ | Estágio para recomeçar |
| useAlternativeService | boolean | ❌ | Usar serviço alternativo |

#### 📤 Response
**Status:** `200 OK`
```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440000",
  "status": "RUNNING",
  "currentStage": "EXTRACTION",
  "retryCount": 1,
  "lastRetryAt": "2025-03-11T10:35:00Z"
}
```

---

### 📊 Estatísticas Gerais

| Métrica | Valor |
|---|---|
| Total de Endpoints | 70+ |
| Endpoints Públicos | 8 |
| Endpoints Autenticados | 62+ |
| Endpoints CLINICIAN | 35+ |
| Endpoints ADMIN | 15+ |
| Endpoints PATIENT | 25+ |

### 🔐 Matriz de Permissões

| Serviço | Público | Patient | Clinician | Admin |
|---|:---:|:---:|:---:|:---:|
| Auth | 5 | - | - | - |
| User | 2 | 3 | 8 | 11 |
| Consent | - | 7 | 7 | 7 |
| Patient Profile | - | 2 | 2 | - |
| Patient History | - | 6 | 10 | - |
| Plans | - | 7 | 13 | - |
| Files | 1 | 4 | 9 | - |
| Prescription | - | - | 10 | 1 |

---

### 💡 Fluxos Principais

#### Fluxo de Registro e Login
```text
1. POST /auth/register
   → Cria usuário e retorna JWT
2. POST /auth/login
   → Autentica e retorna JWT
3. Usar JWT em todas as requisições:
   Authorization: Bearer <token>
```
#### Fluxo de Prescrição Completa
```text
1. POST /files/upload
   → Upload da prescrição médica (PDF/imagem)
2. POST /prescriptions/workflows
   → Inicia processamento (OCR + IA)
3. GET /prescriptions/stages/latest
   → Verifica progresso
4. POST /plans
   → Cria plano de reabilitação baseado na prescrição
5. POST /plans/{id}/approve
   → Aprova o plano
```
#### Fluxo de Gestão de Paciente
```text
1. PUT /patients/{userId}/profile
   → Cria/atualiza perfil do paciente
2. POST /patients/{userId}/conditions
   → Adiciona condições médicas
3. POST /patients/{userId}/allergies
   → Registra alergias
4. POST /patients/{userId}/medications
   → Adiciona medicações
5. POST /patients/{userId}/vitals
   → Registra sinais vitais
```
---

### 🎯 Códigos HTTP Utilizados

| Código | Status | Uso |
|---|---|---|
| 200 | OK | Sucesso em GET/PUT/POST |
| 201 | Created | Recurso criado com sucesso |
| 204 | No Content | Sucesso sem retorno (DELETE) |
| 400 | Bad Request | Dados inválidos |
| 401 | Unauthorized | Não autenticado |
| 403 | Forbidden | Sem permissão |
| 404 | Not Found | Recurso não encontrado |
| 409 | Conflict | Conflito (ex: email duplicado) |
| 500 | Internal Server Error | Erro do servidor |
| 503 | Service Unavailable | Serviço indisponível |

---

### 📝 Notas Importantes

#### Autenticação
* Todos os endpoints (exceto `/auth/**` e alguns health checks) requerem JWT
* JWT expira em 1 hora
* Refresh token expira em 30 dias
* Use `POST /auth/refresh` para renovar o token

#### Autorização
* **API Gateway** valida o JWT e injeta headers:
  * `X-User-Id`: UUID do usuário
  * `X-User-Roles`: Roles do usuário (ex: "ROLE_PATIENT")
  * `X-User-Email`: Email do usuário
* **Microserviços** confiam nesses headers (não validam JWT)

#### Validação de Acesso
* **PATIENT:** Pode acessar apenas seus próprios dados
* **CLINICIAN:** Pode acessar dados de todos os pacientes
* **ADMIN:** Pode gerenciar usuários e acessar tudo

#### IDs
* Todos os IDs são UUID v4
* Formato: `550e8400-e29b-41d4-a716-446655440000`

---


## 📚 Recursos Adicionais

-  **Swagger UI**: (Não implementado ainda)

-  **Postman Collection**: (Criar baseado nesta documentação)

-  **Health Checks**: Cada serviço tem `/health` ou `/{service}/health`

---

**🎉 Documentação completa de 70+ endpoints!**

**Gerado em:** 03/11/2025

**Versão:** 1.0.0

**Última atualização:** Auto-gerado a partir dos controllers

---
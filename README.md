# Campus Placement Management System

A full-stack placement management platform with three roles — **Student**, **TPO (Placement Officer)**, and **Recruiter** — built with Spring Boot, React, and PostgreSQL.

The core of the system is an **eligibility engine**: a drive stores criteria (minimum percentage, max backlogs, eligible branches), and students only see and can apply to drives they actually qualify for.

## Tech Stack

| Layer      | Technology |
|------------|------------|
| Backend    | Java 25, Spring Boot 3, Spring Data JPA, Spring Security (JWT), Bean Validation |
| Database   | PostgreSQL (H2 for tests) |
| Frontend   | React 18 (Vite), React Router, Axios |
| Docs       | springdoc-openapi (Swagger UI) |
| Testing    | JUnit 5 + Mockito (27 service-layer tests) |
| DevOps     | Docker, docker-compose, GitHub Actions CI |

## Architecture

```
Controller  →  Service  →  Repository  →  PostgreSQL
   ↑              ↑
 DTO/Validation  Business logic (eligibility engine)
```

**Entities & relationships**
- `User` (base auth record: email, password hash, role)
- `Student` (1:1 with `User`) — branch, percentage, backlogs, resume
- `Recruiter` (1:1 with `User`) — company name
- `Drive` — eligibility criteria + `eligibleBranches` (collection table) + FK to `Recruiter` and posting `User` (TPO)
- `Application` — the many-to-many join entity between `Student` and `Drive`, enriched with `status` (APPLIED / SHORTLISTED / REJECTED) and timestamps, with a unique constraint on `(student_id, drive_id)`

## Project Structure

```
campus-placement-system/
├── backend/               # Spring Boot REST API
│   ├── src/main/java/com/pms/
│   │   ├── model/          # JPA entities
│   │   ├── repository/     # Spring Data repositories
│   │   ├── service/        # Business logic (incl. EligibilityService)
│   │   ├── controller/     # REST controllers
│   │   ├── dto/             # Request/response DTOs
│   │   ├── security/        # JWT filter, UserDetails, JwtUtil
│   │   ├── config/          # Security, CORS, OpenAPI config
│   │   └── exception/       # Global exception handler
│   ├── src/test/java/...    # JUnit + Mockito tests
│   ├── Dockerfile
│   └── pom.xml
├── frontend/               # React (Vite) SPA
│   ├── src/
│   │   ├── api/             # Axios instance with JWT interceptor
│   │   ├── context/         # AuthContext
│   │   ├── components/      # ProtectedRoute, Navbar
│   │   └── pages/           # Login, Register, per-role dashboards
│   └── Dockerfile
├── docker-compose.yml
└── .github/workflows/ci.yml
```

## Running Locally (without Docker)

### 1. Database
Create a local PostgreSQL database:
```sql
CREATE DATABASE placement_db;
```

### 2. Backend
```bash
cd backend
# Optionally override defaults via environment variables:
# DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD, JWT_SECRET
mvn spring-boot:run
```
The API starts on `http://localhost:8080`.
Swagger UI: `http://localhost:8080/swagger-ui.html`

### 3. Frontend
```bash
cd frontend
npm install
cp .env.example .env    # adjust VITE_API_BASE_URL if needed
npm run dev
```
The app starts on `http://localhost:5173`.

## Running with Docker Compose

From the project root:
```bash
docker compose up --build
```
This starts Postgres, the backend (`:8080`) and the frontend (`:5173`) together.

## Running Tests

```bash
cd backend
mvn test
```
27 JUnit + Mockito tests cover: authentication, the eligibility engine (branch/percentage/backlog rules and edge cases), student applications (including duplicate-application and ineligibility rejection), TPO drive/application management, and recruiter authorization boundaries (a recruiter cannot see or act on another company's drive).

## API Overview

| Role      | Example endpoints |
|-----------|--------------------|
| Public    | `POST /api/auth/register`, `POST /api/auth/login` |
| Student   | `GET/PUT /api/students/me`, `POST /api/students/me/resume`, `GET /api/students/drives/eligible`, `POST /api/students/drives/{id}/apply`, `GET /api/students/applications` |
| TPO       | `POST/GET /api/tpo/drives`, `PATCH /api/tpo/drives/{id}/status`, `GET /api/tpo/drives/{id}/applications`, `PATCH /api/tpo/applications/{id}/status`, `GET /api/tpo/drives/{id}/export` (CSV) |
| Recruiter | `GET /api/recruiter/drives`, `GET /api/recruiter/drives/{id}/applicants`, `PATCH /api/recruiter/applications/{id}/status` |

Full interactive documentation is available via Swagger UI once the backend is running.

## Notes & Next Steps

- `spring.jpa.hibernate.ddl-auto=update` is used for convenience in this project; a production setup should switch to versioned migrations (Flyway/Liquibase).
- Resumes are stored on local disk under `uploads/resumes` (served back via `/uploads/**`); swap `FileStorageService` for an S3-backed implementation for a real AWS deployment.
- To link a `Drive` to a specific `Recruiter` account, pass `recruiterId` when the TPO creates the drive (`GET /api/recruiter/drives` will then return it for that recruiter to review and act on).
- Deploying to AWS EC2: build the backend Dockerfile, push to ECR (or copy the jar directly), run behind a reverse proxy (nginx) with the frontend's static build, and point `RDS PostgreSQL` at the backend via the `DB_*` environment variables.

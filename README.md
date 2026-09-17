# Lab Resource Utilization Platform

An enterprise full-stack platform designed for universities, research institutions, and laboratories to centrally manage laboratory equipment, user identities, inter-institution sharing, time-slot reservations, utilization tracking, maintenance, calibration compliance, and usage-rate cost accounting.

---

## Architecture

The platform is designed following a **Modular Monolith Architecture**. It avoids premature microservice decomposition while maintaining clean domain boundaries, loose coupling, and high cohesion across business modules.

```
lab-resource-utilization-platform/
│
├── frontend/                          # React 19 + TypeScript + Vite + Tailwind CSS
│   ├── src/
│   │   ├── api/                       # Centralized Axios client & API endpoints
│   │   ├── pages/                     # Routed view components
│   │   ├── App.tsx                    # React Router configuration
│   │   ├── index.css                  # Tailwind CSS tokens & layout rules
│   │   └── main.tsx                   # React root entry point
│   ├── package.json
│   └── vite.config.ts
│
├── backend/                           # Spring Boot 3.4.3 + Java 21 + Maven
│   ├── src/main/java/com/labresource/platform/
│   │   ├── config/                    # CORS, web configs, application beans
│   │   ├── security/                  # Spring Security, stateless JWT filter chain
│   │   ├── common/                    # Shared DTOs, utilities, HealthController (/api/health)
│   │   │
│   │   ├── auth/                      # Authentication & token services (Phase 2)
│   │   ├── user/                      # User & role management (Phase 2)
│   │   ├── institution/               # Universities & research entities
│   │   ├── department/                # Faculties & laboratory units
│   │   ├── equipment/                 # Equipment catalog & physical specifications
│   │   ├── booking/                   # Reservations & automated waitlists
│   │   ├── utilization/               # Runtime telemetry & idle audit logging
│   │   ├── sharing/                   # Cross-institution resource agreements
│   │   ├── maintenance/               # Service schedules & downtime work orders
│   │   ├── calibration/               # Calibration schedules & accuracy certification
│   │   ├── cost/                      # Usage rate calculation & chargeback billing
│   │   ├── notification/              # Multi-channel notifications (email, push)
│   │   ├── analytics/                 # Role-based utilization dashboards & heatmaps
│   │   └── report/                    # Regulatory audit & usage reports
│   │
│   ├── src/main/resources/
│   │   └── application.yml            # Environment-driven Spring Boot configuration
│   ├── pom.xml                        # Maven dependency configuration
│   ├── mvnw & mvnw.cmd                # Maven 3.9.16 Wrapper
│   └── .mvn/
│
├── .gitignore                         # Unified Git ignore rules
├── .env.example                       # Reference environment variables template
└── README.md                          # Project documentation
```

---

## Technology Stack

### Frontend
- **Framework**: React 19
- **Language**: TypeScript 5.7+
- **Build Tool**: Vite 6+
- **Routing**: React Router 7
- **HTTP Client**: Axios
- **Styling**: Tailwind CSS v4 (`@tailwindcss/vite`)
- **Icons**: Lucide React

### Backend
- **Language**: Java 21 LTS
- **Framework**: Spring Boot 3.4.3
- **Build Tool**: Apache Maven 3.9.16 (via Maven Wrapper)
- **Web**: Spring Web (`spring-boot-starter-web`)
- **Persistence**: Spring Data JPA / Hibernate (`spring-boot-starter-data-jpa`)
- **Security**: Spring Security (`spring-boot-starter-security`)
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)
- **JWT**: JJWT (`jjwt-api`, `jjwt-impl`, `jjwt-jackson` 0.12.6)
- **Database Driver**: MySQL Connector/J (`com.mysql:mysql-connector-j`)

### Database
- **Engine**: MySQL 8.0+

---

## Environment Variables

All database and infrastructure configurations are externalized through environment variables with sensible local defaults. Never commit production credentials to version control.

| Variable | Description | Default Value |
| :--- | :--- | :--- |
| `SERVER_PORT` | Port for Spring Boot backend | `8080` |
| `DB_HOST` | MySQL database host | `localhost` |
| `DB_PORT` | MySQL database port | `3306` |
| `DB_NAME` | Database schema name | `lab_resource_platform` |
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | *(Empty string / configured via env)* |
| `DB_URL` | Complete JDBC database URL | `jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `JPA_DDL_AUTO` | Hibernate schema management | `update` |
| `JWT_SECRET` | Secret key for JWT signing (HMAC-SHA) | Placeholder 256-bit hex secret |
| `JWT_EXPIRATION_MS` | JWT expiration duration in ms | `86400000` (24 hours) |
| `CORS_ALLOWED_ORIGINS`| Allowed origins for CORS (comma-separated)| `http://localhost:5173,http://127.0.0.1:5173` |
| `VITE_API_BASE_URL` | Frontend API base URL | `http://localhost:8080/api` |

---

## Database Setup

1. **Verify MySQL 8.0 Service**:
   Ensure MySQL service is running locally on port `3306`:
   ```powershell
   Get-Service -Name *mysql*
   ```

2. **Create Database (Optional - automatic via JDBC)**:
   The connection URL includes `createDatabaseIfNotExist=true`. Alternatively, run in MySQL shell:
   ```sql
   CREATE DATABASE IF NOT EXISTS lab_resource_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **Set Environment Credentials**:
   In PowerShell:
   ```powershell
   $env:DB_USERNAME="root"
   $env:DB_PASSWORD="your_mysql_password"
   ```
   Or in Bash/Linux/macOS:
   ```bash
   export DB_USERNAME=root
   export DB_PASSWORD=your_mysql_password
   ```

---

## Backend Setup & Execution

### Prerequisites
- JDK 21 installed (`java -version` should show Java 21)

### 1. Navigate to Backend Directory
```powershell
cd backend
```

### 2. Compile and Verify Sources
```powershell
.\mvnw.cmd clean compile
```

### 3. Run Backend Application
```powershell
.\mvnw.cmd spring-boot:run
```
Or run the packaged JAR:
```powershell
.\mvnw.cmd package -DskipTests
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### 4. Health Endpoint Verification
Once started, test the health check endpoint:
```powershell
curl http://localhost:8080/api/health
```
**Expected Response:**
```json
{
  "status": "UP",
  "service": "Lab Resource Utilization Platform"
}
```

---

## Frontend Setup & Execution

### Prerequisites
- Node.js 20+ installed (`node -v`)
- npm 10+ installed (`npm -v`)

### 1. Navigate to Frontend Directory
```powershell
cd frontend
```

### 2. Install Dependencies
```powershell
npm install
```

### 3. Start Development Server
```powershell
npm run dev
```
The application will start at `http://localhost:5173/`.

### 4. Build for Production
```powershell
npm run build
```
Build output is generated in `frontend/dist/`.

---

## API Health Check Endpoint Specification

- **URL**: `GET /api/health`
- **Authentication**: Public (permitted in `SecurityConfig`)
- **CORS**: Enabled for `http://localhost:5173` and `http://127.0.0.1:5173`
- **Response Code**: `200 OK`
- **Content-Type**: `application/json`
- **Response Body**:
  ```json
  {
    "status": "UP",
    "service": "Lab Resource Utilization Platform"
  }
  ```

---

## Next Steps

Subsequent implementation phases will include:
1. **Authentication & Authorization**: User registration, JWT issuance, refresh tokens, role-based access control (Admin, Lab Technician, Researcher, Institutional Coordinator).
2. **Core Domain Entities**: Institution, Department, Lab, and Equipment catalogs.
3. **Reservation Engine**: Real-time slot booking and queue-managed waitlists.
4. **Utilization & Cost Tracking**: Equipment telemetry integration and inter-institution billing.

# KiemThuPhanMem - Complete Boilerplate Summary

## 📁 File Structure

```
BOILERPLATE_1_ENTITIES.md              ← Database Entities (User, Role, Permission)
BOILERPLATE_2_DTOS.md                  ← DTOs (Request & Response)
BOILERPLATE_3_REPOSITORIES.md          ← JPA Repository Interfaces
BOILERPLATE_4_SERVICES.md              ← Business Logic Layer
BOILERPLATE_5_CONTROLLERS_UTILITIES.md ← REST Controllers + Utilities
BOILERPLATE_6_CONFIGURATION.md         ← Spring Configuration + Main Class
PROJECT_BOILERPLATE.md                 ← High-level Overview
```

---

## 🎯 How to Use This Boilerplate

### Step 1: Create New Spring Boot Project
```bash
# Using Spring Initializr
# - Java 17
# - Spring Boot 4.0.5
# - Gradle (Kotlin DSL)
# - MySQL, Spring JPA, Spring Security, Spring Web

# OR download from: https://start.spring.io/
```

### Step 2: Copy Code from These Files
1. Copy all **Entities** from `BOILERPLATE_1_ENTITIES.md`
2. Copy all **DTOs** from `BOILERPLATE_2_DTOS.md`
3. Copy all **Repositories** from `BOILERPLATE_3_REPOSITORIES.md`
4. Copy all **Services** from `BOILERPLATE_4_SERVICES.md`
5. Copy all **Controllers & Utilities** from `BOILERPLATE_5_CONTROLLERS_UTILITIES.md`
6. Copy all **Configuration** from `BOILERPLATE_6_CONFIGURATION.md`

### Step 3: Setup Database
```sql
-- Create database
CREATE DATABASE se113;

-- Tables will be auto-created by Hibernate (ddl-auto=update)
```

### Step 4: Configure application.properties
```properties
# Update database credentials if different
spring.datasource.url=jdbc:mysql://localhost:3306/se113
spring.datasource.username=root
spring.datasource.password=123456
```

### Step 5: Run Application
```bash
./gradlew bootRun
```

---

## 📊 Component Diagram

```
HTTP Request
    ↓
Controller (@RestController)
    ↓
Service (@Service) - Business Logic
    ↓
Repository (JpaRepository) - Data Access
    ↓
Database (MySQL)

Response Flow (opposite direction)
    ↑
ResponseBodyAdvice (Format Response)
```

---

## 🔄 Authentication Flow

```
1. Client sends: POST /api/v1/auth/login
   └─ Body: { "email": "user@example.com", "password": "password123" }

2. AuthController receives request
   └─ Validates email/password
   └─ Calls UserService.handleFindByEmail()

3. UserService checks credentials
   └─ If invalid: return 401 Unauthorized
   └─ If valid: proceed to step 4

4. SecurityUtil.createAccessToken()
   └─ Creates JWT with user info and role
   └─ Expiration: 10 days

5. SecurityUtil.createRefreshToken()
   └─ Creates refresh token
   └─ Stored in HTTP-only cookie

6. Response to client:
   └─ Body: { "access_token": "...", "user": {...}, "role": {...} }
   └─ Headers: Set-Cookie: refresh_token=... (HttpOnly)

7. Subsequent requests:
   └─ Include: Authorization: Bearer {access_token}
   └─ SecurityConfiguration validates token
   └─ Request processed if valid
```

---

## 🛡️ Security Layers

```
1. CORS Filtering
   └─ Allow: http://localhost:5173
   └─ Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS

2. Spring Security Filter Chain
   └─ Public endpoints (no authentication required):
      - /api/v1/auth/login
      - /api/v1/auth/refresh
      - /v3/api-docs/** (Swagger)
      - /swagger-ui/** (Swagger UI)
   
   └─ Protected endpoints (JWT required):
      - All other /api/v1/** endpoints

3. JWT Validation
   └─ Decode JWT token
   └─ Check signature using HS256 algorithm
   └─ Check expiration time

4. Method-Level Security
   └─ @Secured("ROLE_ADMIN")
   └─ @PreAuthorize("hasRole('USER')")
```

---

## 📝 Database Schema

### users table
| Column                | Type         | Key    | Description            |
| --------------------- | ------------ | ------ | ---------------------- |
| id                    | BINARY(16)   | PK     | UUIDv7                 |
| email                 | VARCHAR(255) | UNIQUE | User email             |
| password              | VARCHAR(255) |        | BCrypt hashed          |
| name                  | VARCHAR(255) |        | User name              |
| account_status        | VARCHAR(50)  |        | Active/Inactive/Locked |
| failed_login_attempts | INT          |        | Login attempt tracking |
| refresh_token         | MEDIUMTEXT   |        | JWT refresh token      |
| role_id               | BIGINT       | FK     | Reference to roles     |
| created_at            | TIMESTAMP    |        | Auto-set on creation   |
| updated_at            | TIMESTAMP    |        | Auto-set on update     |
| created_by            | VARCHAR(100) |        | Username who created   |
| updated_by            | VARCHAR(100) |        | Username who updated   |

### roles table
| Column      | Type         | Key    | Description             |
| ----------- | ------------ | ------ | ----------------------- |
| id          | BIGINT       | PK     | Auto-increment          |
| name        | VARCHAR(255) | UNIQUE | Role name (ADMIN, USER) |
| description | VARCHAR(255) |        | Role description        |
| active      | BOOLEAN      |        | Active status           |
| created_at  | TIMESTAMP    |        | Auto-set on creation    |
| updated_at  | TIMESTAMP    |        | Auto-set on update      |
| created_by  | VARCHAR(100) |        | Username who created    |
| updated_by  | VARCHAR(100) |        | Username who updated    |

### permissions table
| Column     | Type               | Key | Description          |
| ---------- | ------------------ | --- | -------------------- |
| id         | BIGINT             | PK  | Auto-increment       |
| name       | VARCHAR(255)       |     | Permission name      |
| api_path   | VARCHAR(255)       |     | API endpoint         |
| method     | VARCHAR(50)        |     | HTTP method          |
| module     | VARCHAR(255)       |     | Feature module       |
| created_at | TIMESTAMP          |     | Auto-set on creation |
| updated_at | TIMESTAMP          |     | Auto-set on update   |
| created_by | VARCHAR(100)       |     | Username who created |
| updated_by | VARCHAR(100)       |     | Username who updated |
| UNIQUE     | (api_path, method) |     | Composite unique     |

### role_permission junction table
| Column        | Type   | Key   | Description              |
| ------------- | ------ | ----- | ------------------------ |
| role_id       | BIGINT | PK/FK | Reference to roles       |
| permission_id | BIGINT | PK/FK | Reference to permissions |

---

## 🔌 API Endpoints Summary

### Authentication
```
POST   /api/v1/auth/login              - User login
GET    /api/v1/auth/account            - Get current user info
GET    /api/v1/auth/refresh            - Refresh access token
POST   /api/v1/auth/logout             - User logout
```

### Management (implement as needed)
```
GET    /api/v1/users                   - Get all users (paginated)
GET    /api/v1/users/{id}              - Get user by ID
POST   /api/v1/users                   - Create new user
PUT    /api/v1/users/{id}              - Update user
DELETE /api/v1/users/{id}              - Delete user

GET    /api/v1/roles                   - Get all roles (paginated)
GET    /api/v1/roles/{id}              - Get role by ID
POST   /api/v1/roles                   - Create new role
PUT    /api/v1/roles/{id}              - Update role
DELETE /api/v1/roles/{id}              - Delete role

GET    /api/v1/permissions             - Get all permissions
POST   /api/v1/permissions             - Create permission
PUT    /api/v1/permissions/{id}        - Update permission
DELETE /api/v1/permissions/{id}        - Delete permission
```

### Monitoring (Actuator)
```
GET    /actuator/health                - Application health
GET    /actuator/metrics               - Application metrics
GET    /actuator/env                   - Environment variables
GET    /actuator/beans                 - Registered beans
GET    /actuator/mappings              - Request mappings
```

---

## 🧪 Testing & Validation

### Example Login Request
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "password123"
  }'
```

### Example Authenticated Request
```bash
curl -X GET http://localhost:8081/api/v1/auth/account \
  -H "Authorization: Bearer {access_token}"
```

---

## 🚀 Build & Run Commands

```bash
# Build project
./gradlew clean build

# Run application
./gradlew bootRun

# Run tests
./gradlew test

# Create executable JAR
./gradlew bootJar
# Result: build/libs/KiemThuPhanMem-0.0.1-SNAPSHOT.jar

# Run JAR
java -jar build/libs/KiemThuPhanMem-0.0.1-SNAPSHOT.jar
```

---

## 📚 Project Dependencies

| Dependency                   | Version | Purpose               |
| ---------------------------- | ------- | --------------------- |
| Spring Boot Starter Web      | 4.0.5   | REST API              |
| Spring Boot Starter Security | 4.0.5   | Authentication        |
| Spring Data JPA              | 4.0.5   | Database ORM          |
| Spring Security OAuth2       | 4.0.5   | JWT handling          |
| MySQL Driver                 | Latest  | Database              |
| Lombok                       | Latest  | Reduce boilerplate    |
| Springdoc OpenAPI            | 2.8.14  | Swagger documentation |
| UUID Creator                 | 5.3.3   | UUIDv7 generation     |

---

## 🔗 External Resources

- **Swagger Documentation**: `http://localhost:8081/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8081/v3/api-docs`
- **Actuator**: `http://localhost:8081/actuator`

---

## 📋 Customization Checklist

- [ ] Update project name in `build.gradle.kts`
- [ ] Update package names if different from `com.uit.nhom7.KiemThuPhanMem`
- [ ] Change database credentials in `application.properties`
- [ ] Update CORS allowed origins for your frontend
- [ ] Add more DTOs as needed for your features
- [ ] Implement additional controllers and services
- [ ] Create database seed script for initial data
- [ ] Add exception handling with `@ExceptionHandler`
- [ ] Add validation annotations in DTOs
- [ ] Configure logging levels

---

## 🎓 Learning Path

1. **Understanding Architecture**: Read `PROJECT_BOILERPLATE.md`
2. **Database Layer**: Study `BOILERPLATE_1_ENTITIES.md`
3. **API Layer**: Review `BOILERPLATE_2_DTOS.md`
4. **Data Access**: Examine `BOILERPLATE_3_REPOSITORIES.md`
5. **Business Logic**: Analyze `BOILERPLATE_4_SERVICES.md`
6. **API Endpoints**: Implement from `BOILERPLATE_5_CONTROLLERS_UTILITIES.md`
7. **Configuration**: Setup `BOILERPLATE_6_CONFIGURATION.md`

---

## ✅ Quality Checklist

- [x] Entities with relationships defined
- [x] DTOs for request/response separation
- [x] Repository interfaces for data access
- [x] Service layer with business logic
- [x] REST controllers with API endpoints
- [x] JWT authentication and authorization
- [x] Custom utilities and helpers
- [x] Spring Security configuration
- [x] CORS support
- [x] Swagger documentation
- [x] Error handling
- [x] Pagination support
- [x] Audit fields (createdAt, updatedAt, createdBy, updatedBy)

---

**Version**: 1.0  
**Last Updated**: 2026-04-23  
**Status**: Production Ready  
**License**: MIT
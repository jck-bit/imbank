# JWT Authentication & Role-Based Access Control (RBAC) Documentation

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Database Schema](#database-schema)
3. [Security Components](#security-components)
4. [Authentication Flow](#authentication-flow)
5. [Authorization Flow](#authorization-flow)
6. [API Endpoints](#api-endpoints)
7. [Request/Response Examples](#request-response-examples)
8. [Security Configuration](#security-configuration)

---

## 1. Architecture Overview

### Core Components
- **Stateless Authentication**: JWT tokens (no server-side sessions)
- **Role-Based Access Control**: Three roles (ROLE_USER, ROLE_MANAGER, ROLE_ADMIN)
- **Refresh Token Mechanism**: Short-lived access tokens (15 min) + long-lived refresh tokens (7 days)
- **Production-Ready**: Proper separation of concerns, middleware, exception handling

### Technology Stack
- Spring Boot 4.0.2
- Spring Security 6.x
- JJWT 0.13.0
- BCrypt password hashing (10 rounds)
- MySQL database (AWS RDS)

---

## 2. Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- BCrypt hash
    enabled BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Roles Table
```sql
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,  -- ROLE_USER, ROLE_ADMIN, ROLE_MANAGER
    description VARCHAR(255)
);
```

### User_Roles Junction Table (Many-to-Many)
```sql
CREATE TABLE user_roles (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

### Refresh_Tokens Table
```sql
CREATE TABLE refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(500) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Employees Table (Optional Link to Users)
```sql
ALTER TABLE employees ADD COLUMN user_id BIGINT NULL;
ALTER TABLE employees ADD FOREIGN KEY (user_id) REFERENCES users(id);
```

**Rationale**: Not all employees need system access; not all users are employees.

---

## 3. Security Components

### 3.1 JwtTokenProvider
**Location**: `src/main/java/com/example/imbank/security/JwtTokenProvider.java`

**Responsibilities**:
- Generate access tokens (JWT with user info + roles)
- Validate tokens (signature, expiration)
- Extract username and claims from tokens

**Key Configuration**:
```properties
jwt.secret=5367566B59703373367639792F423F4528482B4D6251655468576D5A7134743
jwt.access-token-expiration=900000    # 15 minutes
jwt.refresh-token-expiration=604800000 # 7 days
jwt.issuer=imbank-api
```

**Token Structure**:
```json
{
  "sub": "admin",
  "roles": "ROLE_ADMIN,ROLE_USER",
  "iat": 1708164000,
  "exp": 1708164900,
  "iss": "imbank-api"
}
```

### 3.2 JwtAuthenticationFilter
**Location**: `src/main/java/com/example/imbank/security/JwtAuthenticationFilter.java`

**Responsibilities**:
- Intercept ALL incoming requests
- Extract JWT from `Authorization: Bearer <token>` header
- Validate token and load user details
- Set authentication in SecurityContext

**Flow**:
1. Extract token from header
2. Validate token signature and expiration
3. Load user from database
4. Create Authentication object
5. Store in SecurityContextHolder
6. Continue filter chain

### 3.3 CustomUserDetailsService
**Location**: `src/main/java/com/example/imbank/security/CustomUserDetailsService.java`

**Responsibilities**:
- Load user by username during authentication
- Convert User entity to Spring Security's UserDetails

### 3.4 CustomUserDetails
**Location**: `src/main/java/com/example/imbank/security/CustomUserDetails.java`

**Responsibilities**:
- Adapter between User entity and Spring Security
- Provides authorities (roles) for authorization

### 3.5 Exception Handlers

**JwtAuthenticationEntryPoint** (401 Unauthorized):
- Handles unauthenticated access attempts
- Returns JSON error response

**JwtAccessDeniedHandler** (403 Forbidden):
- Handles insufficient permissions
- Returns JSON error response

---

## 4. Authentication Flow

### 4.1 User Registration Flow

```
Client                  AuthController           AuthService              Database
  |                           |                        |                       |
  |--POST /api/auth/register->|                        |                       |
  |  {username, email, pwd}   |                        |                       |
  |                           |--register()---------->|                       |
  |                           |                        |--Check if exists---->|
  |                           |                        |<--No duplicates------|
  |                           |                        |--Hash password------->|
  |                           |                        |  (BCrypt)             |
  |                           |                        |--Assign ROLE_USER---->|
  |                           |                        |--Save user---------->|
  |                           |<--UserResponseDto------|<--User created-------|
  |<--201 Created-------------|                        |                       |
  |  {id, username, email...} |                        |                       |
```

**Code Reference**: `src/main/java/com/example/imbank/service/AuthServiceImpl.java:register()`

### 4.2 User Login Flow

```
Client                  AuthController           AuthService              JwtTokenProvider
  |                           |                        |                       |
  |--POST /api/auth/login---->|                        |                       |
  |  {usernameOrEmail, pwd}   |                        |                       |
  |                           |--login()-------------->|                       |
  |                           |                        |--Authenticate-------->|
  |                           |                        |  (AuthenticationMgr)  |
  |                           |                        |<--Success-------------|
  |                           |                        |--generateAccessToken->|
  |                           |                        |<--JWT token-----------|
  |                           |                        |--createRefreshToken-->|
  |                           |                        |  (Save to DB)         |
  |                           |<--LoginResponseDto-----|                       |
  |<--200 OK------------------|                        |                       |
  |  {accessToken,            |                        |                       |
  |   refreshToken,           |                        |                       |
  |   expiresIn: 900}         |                        |                       |
```

**Response Example**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "userId": 1,
  "username": "admin",
  "email": "admin@imbank.com"
}
```

### 4.3 Protected Request Flow

```
Client              JwtAuthFilter        JwtTokenProvider    CustomUserDetailsService    Controller
  |                      |                      |                      |                    |
  |--GET /api/employees->|                      |                      |                    |
  |  Authorization:      |                      |                      |                    |
  |  Bearer <token>      |                      |                      |                    |
  |                      |--Extract token------>|                      |                    |
  |                      |--Validate token----->|                      |                    |
  |                      |<--Valid--------------|                      |                    |
  |                      |--Get username------->|                      |                    |
  |                      |<--"admin"------------|                      |                    |
  |                      |--loadUserByUsername(admin)----------------->|                    |
  |                      |<--UserDetails with roles [ADMIN, USER]------|                    |
  |                      |--Set Authentication in SecurityContext----->|                    |
  |                      |--Continue filter chain----------------------------------->|       |
  |                      |                      |                      |            |--Execute|
  |<--200 OK with employee data---------------------------------------------------|
```

### 4.4 Token Refresh Flow

```
Client                  AuthController           AuthService              Database
  |                           |                        |                       |
  |--POST /api/auth/refresh-->|                        |                       |
  |  {refreshToken}           |                        |                       |
  |                           |--refreshToken()------->|                       |
  |                           |                        |--Find token in DB---->|
  |                           |                        |<--Token found---------|
  |                           |                        |--Check expiry-------->|
  |                           |                        |--Check revoked------->|
  |                           |                        |--Generate new tokens->|
  |                           |                        |--Revoke old token---->|
  |                           |<--LoginResponseDto-----|                       |
  |<--200 OK with new tokens--|                        |                       |
```

### 4.5 Logout Flow

```
Client                  AuthController           AuthService              Database
  |                           |                        |                       |
  |--POST /api/auth/logout--->|                        |                       |
  |  Authorization: Bearer    |                        |                       |
  |                           |--logout()------------->|                       |
  |                           |                        |--Revoke all user----->|
  |                           |                        |  refresh tokens       |
  |                           |<--Success--------------|<--Updated-------------|
  |<--204 No Content----------|                        |                       |
```

---

## 5. Authorization Flow

### 5.1 Role Definitions

| Role         | Description                           | Typical Permissions                    |
|--------------|---------------------------------------|----------------------------------------|
| ROLE_USER    | Basic authenticated user              | View own profile, basic operations     |
| ROLE_MANAGER | Department/team manager               | Manage employees, approve requests     |
| ROLE_ADMIN   | System administrator                  | Full access, user management, config   |

### 5.2 Method-Level Security

**Using @PreAuthorize annotation**:

```java
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    // Any authenticated user can view
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<EmployeeDto> getAllEmployees() { ... }

    // Only managers and admins can create
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public EmployeeDto createEmployee(@RequestBody EmployeeDto dto) { ... }

    // Only admins can delete
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteEmployee(@PathVariable Long id) { ... }
}
```

### 5.3 URL-Based Security

**SecurityConfig.java**:
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()           // Public
            .requestMatchers("/h2-console/**").permitAll()         // Dev only
            .requestMatchers("/error").permitAll()                 // Error pages
            .requestMatchers("/api/admin/**").hasRole("ADMIN")     // Admin only
            .requestMatchers("/api/**").authenticated()            // All other APIs
            .anyRequest().authenticated()
        );

    return http.build();
}
```

### 5.4 Authorization Decision Flow

```
Request with JWT
    |
    v
JwtAuthFilter extracts token
    |
    v
Load UserDetails (includes roles)
    |
    v
Set Authentication in SecurityContext
    |
    v
Request reaches Controller
    |
    v
@PreAuthorize checks roles
    |
    +---> Has required role? ---> Execute method ---> Return response
    |
    +---> Missing role? ---> JwtAccessDeniedHandler ---> 403 Forbidden
```

---

## 6. API Endpoints

### 6.1 Authentication Endpoints (Public)

| Method | Endpoint              | Description           | Request Body                              |
|--------|-----------------------|-----------------------|-------------------------------------------|
| POST   | /api/auth/register    | Create new user       | {username, email, password}               |
| POST   | /api/auth/login       | Authenticate user     | {usernameOrEmail, password}               |
| POST   | /api/auth/refresh     | Refresh access token  | {refreshToken}                            |
| POST   | /api/auth/logout      | Revoke refresh tokens | - (requires auth)                         |
| GET    | /api/auth/me          | Get current user info | - (requires auth)                         |

### 6.2 Protected Endpoints

| Method | Endpoint              | Required Role         | Description                     |
|--------|-----------------------|-----------------------|---------------------------------|
| GET    | /api/employees        | AUTHENTICATED         | List all employees              |
| POST   | /api/employees        | MANAGER, ADMIN        | Create employee                 |
| PUT    | /api/employees/{id}   | MANAGER, ADMIN        | Update employee                 |
| DELETE | /api/employees/{id}   | ADMIN                 | Delete employee                 |
| GET    | /api/departments      | AUTHENTICATED         | List all departments            |
| POST   | /api/admin/users      | ADMIN                 | Manage user roles               |

---

## 7. Request/Response Examples

### 7.1 Register New User

**Request**:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@imbank.com",
    "password": "SecurePass123"
  }'
```

**Response** (201 Created):
```json
{
  "id": 2,
  "username": "john_doe",
  "email": "john@imbank.com",
  "enabled": true,
  "accountNonLocked": true,
  "roles": ["ROLE_USER"],
  "createdAt": "2026-02-17T10:30:00",
  "updatedAt": "2026-02-17T10:30:00"
}
```

### 7.2 Login

**Request**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin",
    "password": "Admin@123"
  }'
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjoiUk9MRV9BRE1JTixST0xFX1VTRVIiLCJpYXQiOjE3MDgxNjQwMDAsImV4cCI6MTcwODE2NDkwMCwiaXNzIjoiaW1iYW5rLWFwaSJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "userId": 1,
  "username": "admin",
  "email": "admin@imbank.com"
}
```

### 7.3 Access Protected Resource

**Request**:
```bash
curl -X GET http://localhost:8080/api/employees \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "fullName": "John Smith",
    "email": "john.smith@imbank.com",
    "departmentName": "Engineering"
  }
]
```

### 7.4 Unauthorized Access

**Request** (no token):
```bash
curl -X GET http://localhost:8080/api/employees
```

**Response** (401 Unauthorized):
```json
{
  "timestamp": "2026-02-17T10:35:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required. Please login.",
  "path": "/api/employees"
}
```

### 7.5 Forbidden Access

**Request** (user without ADMIN role tries to delete):
```bash
curl -X DELETE http://localhost:8080/api/employees/1 \
  -H "Authorization: Bearer <user_token>"
```

**Response** (403 Forbidden):
```json
{
  "timestamp": "2026-02-17T10:36:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You don't have permission to access this resource",
  "path": "/api/employees/1"
}
```

### 7.6 Refresh Token

**Request**:
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...[new_token]",
  "refreshToken": "660e8400-e29b-41d4-a716-446655440111",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "userId": 1,
  "username": "admin",
  "email": "admin@imbank.com"
}
```

### 7.7 Get Current User Profile

**Request**:
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Response** (200 OK):
```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@imbank.com",
  "enabled": true,
  "accountNonLocked": true,
  "roles": ["ROLE_ADMIN", "ROLE_USER"],
  "createdAt": "2026-02-15T08:00:00",
  "updatedAt": "2026-02-17T10:00:00"
}
```

---

## 8. Security Configuration

### 8.1 SecurityConfig Overview

**Location**: `src/main/java/com/example/imbank/config/SecurityConfig.java`

**Key Features**:
- Stateless session management (no cookies/sessions)
- CSRF disabled (for REST API)
- Custom JWT filter before UsernamePasswordAuthenticationFilter
- Custom authentication entry point and access denied handler

**Code**:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Enables @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // 10 rounds by default
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/departments/**").authenticated()
                .requestMatchers("/api/employees/**").authenticated()
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### 8.2 Password Security

- **Algorithm**: BCrypt
- **Salt rounds**: 10 (Spring Security default)
- **Hash example**: `$2a$10$Y/Tm37Rr/Els3elYryl7Hezg60bFVAbqvkRbBeqvWjfBKyivSOA5i`
- **Hash format**: `$2a$[rounds]$[salt][hash]`

### 8.3 JWT Security

- **Algorithm**: HMAC-SHA256 (HS256)
- **Secret**: 256-bit key (stored in `application.properties`)
- **Token expiry**: 15 minutes (prevents replay attacks)
- **Issuer**: imbank-api (validates token origin)

### 8.4 Refresh Token Security

- **Storage**: Database (not in JWT)
- **Expiry**: 7 days
- **Revocation**: Deleted on logout
- **Rotation**: New refresh token on each refresh (prevents reuse)

---

## 9. Complete Request Lifecycle

### Example: User logs in and accesses protected resource

```
Step 1: LOGIN
================
Client: POST /api/auth/login
Body: {"usernameOrEmail": "admin", "password": "Admin@123"}
    ↓
AuthController receives request
    ↓
AuthService.login() called
    ↓
AuthenticationManager authenticates (checks password)
    ↓
Password matches? → Load user with roles from database
    ↓
Generate JWT with roles: {"sub":"admin", "roles":"ROLE_ADMIN,ROLE_USER", ...}
    ↓
Create refresh token (UUID) and save to database
    ↓
Return: {accessToken: "eyJ...", refreshToken: "550e...", expiresIn: 900}


Step 2: ACCESS PROTECTED RESOURCE
===================================
Client: GET /api/employees
Header: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
    ↓
Request enters JwtAuthenticationFilter
    ↓
Extract token from "Bearer eyJ..." header
    ↓
Validate token signature with secret key
    ↓
Check token expiration (< 15 min old?)
    ↓
Extract username from token: "admin"
    ↓
Load user from database via CustomUserDetailsService
    ↓
User found with roles: [ROLE_ADMIN, ROLE_USER]
    ↓
Create Authentication object with authorities
    ↓
Store in SecurityContextHolder
    ↓
Continue filter chain → reaches EmployeeController
    ↓
@PreAuthorize("isAuthenticated()") checks SecurityContext
    ↓
User is authenticated? YES → Execute method
    ↓
Return employee list


Step 3: TOKEN EXPIRES
======================
Client: GET /api/employees (after 15+ minutes)
Header: Authorization: Bearer <expired_token>
    ↓
JwtAuthenticationFilter validates token
    ↓
Token expired? YES → Validation fails
    ↓
No authentication set in SecurityContext
    ↓
Request reaches controller unauthenticated
    ↓
JwtAuthenticationEntryPoint triggered
    ↓
Return: 401 Unauthorized


Step 4: REFRESH TOKEN
======================
Client: POST /api/auth/refresh
Body: {"refreshToken": "550e8400..."}
    ↓
AuthService.refreshToken() called
    ↓
Find refresh token in database
    ↓
Token found? Check expiry (< 7 days old?)
    ↓
Token valid? Load associated user
    ↓
Generate NEW access token
    ↓
Generate NEW refresh token
    ↓
Revoke old refresh token in database
    ↓
Return: {accessToken: "new_token", refreshToken: "new_refresh"}
```

---

## 10. Error Handling

### 10.1 Authentication Errors

| Error                  | Status | Response                                  |
|------------------------|--------|-------------------------------------------|
| Invalid credentials    | 401    | "Bad credentials"                         |
| Expired token          | 401    | "Authentication required. Please login."  |
| Invalid token          | 401    | "Authentication required. Please login."  |
| Missing token          | 401    | "Authentication required. Please login."  |

### 10.2 Authorization Errors

| Error                  | Status | Response                                                |
|------------------------|--------|---------------------------------------------------------|
| Insufficient role      | 403    | "You don't have permission to access this resource"     |

### 10.3 Validation Errors

| Error                  | Status | Response                                  |
|------------------------|--------|-------------------------------------------|
| Duplicate username     | 400    | "Username already exists"                 |
| Duplicate email        | 400    | "Email already exists"                    |
| Invalid refresh token  | 400    | "Invalid or expired refresh token"        |

---

## 11. Key Security Features

✅ **Stateless**: No server-side sessions (scalable)
✅ **Password Hashing**: BCrypt with 10 rounds
✅ **Token Expiration**: Short-lived access tokens
✅ **Token Refresh**: Long-lived refresh tokens with rotation
✅ **Token Revocation**: Logout invalidates refresh tokens
✅ **Role-Based Access**: Fine-grained permissions
✅ **Method-Level Security**: @PreAuthorize annotations
✅ **URL-Level Security**: Pattern-based access control
✅ **Exception Handling**: Custom 401/403 responses
✅ **Separation of Concerns**: Filter → Service → Controller
✅ **Production-Ready**: Proper middleware architecture

---

## 12. Testing the Implementation

### Using cURL

```bash
# 1. Register a new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@imbank.com","password":"Test123"}'

# 2. Login as admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"Admin@123"}'

# Save the accessToken from response

# 3. Access protected resource
curl -X GET http://localhost:8080/api/employees \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# 4. Get current user profile
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# 5. Refresh token
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"YOUR_REFRESH_TOKEN"}'

# 6. Logout
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Using Postman

1. Create a collection "IMBank Auth"
2. Add environment variables:
   - `base_url`: http://localhost:8080
   - `access_token`: (will be set after login)
   - `refresh_token`: (will be set after login)
3. Add requests for each endpoint
4. Use Tests tab to auto-save tokens:
   ```javascript
   var jsonData = pm.response.json();
   pm.environment.set("access_token", jsonData.accessToken);
   pm.environment.set("refresh_token", jsonData.refreshToken);
   ```

---

## 13. Database Migrations

All schema changes managed via Liquibase:

- **003-create-roles-table.yaml**: Creates roles table
- **004-create-users-table.yaml**: Creates users table with auth fields
- **005-create-user-roles-table.yaml**: Creates junction table
- **006-seed-default-roles.yaml**: Inserts ROLE_USER, ROLE_MANAGER, ROLE_ADMIN
- **007-create-refresh-tokens-table.yaml**: Creates refresh tokens table
- **008-add-user-id-to-employees.yaml**: Links employees to users (optional)
- **009-add-audit-columns-to-employees.yaml**: Adds created_at/updated_at
- **010-add-fullname-column-to-employees.yaml**: Adds full_name column
- **012-create-admin-user-v2.yaml**: Seeds first admin user

**Admin Credentials** (created via migration):
- Username: `admin`
- Password: `Admin@123`
- Roles: ROLE_ADMIN, ROLE_USER

---

## 14. Future Enhancements

- [ ] Account lockout after failed login attempts
- [ ] Password reset via email
- [ ] Two-factor authentication (2FA)
- [ ] Token blacklisting for immediate revocation
- [ ] Audit logging for security events
- [ ] Rate limiting on login endpoint
- [ ] Remember me functionality
- [ ] Social login (OAuth2)
- [ ] API key authentication for service-to-service

---

**Documentation Version**: 1.0
**Last Updated**: February 17, 2026
**Author**: Jack Kinyanjui

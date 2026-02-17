# API Testing Guide - JWT Authentication & RBAC

## Table of Contents
1. [Environment Setup](#environment-setup)
2. [Authentication Tests](#authentication-tests)
3. [Authorization Tests (RBAC)](#authorization-tests-rbac)
4. [Token Management Tests](#token-management-tests)
5. [Error Scenario Tests](#error-scenario-tests)
6. [Postman Collection](#postman-collection)

---

## 1. Environment Setup

### Base URL
```
http://localhost:8080
```

### Default Admin Credentials
```
Username: admin
Password: Admin@123
```

---

## 2. Authentication Tests

### Test 2.1: Register New User (ROLE_USER)

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john.doe@imbank.com",
    "password": "JohnPass123"
  }'
```

**Expected Response:** `201 Created`
```json
{
  "id": 2,
  "username": "john_doe",
  "email": "john.doe@imbank.com",
  "enabled": true,
  "accountNonLocked": true,
  "roles": ["ROLE_USER"],
  "createdAt": "2026-02-17T10:30:00",
  "updatedAt": "2026-02-17T10:30:00"
}
```

**What to verify:**
- ✅ Status code is 201
- ✅ User gets ROLE_USER by default
- ✅ Password is NOT returned in response
- ✅ Timestamps are set automatically

---

### Test 2.2: Register Duplicate Username

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "different@imbank.com",
    "password": "Pass123"
  }'
```

**Expected Response:** `400 Bad Request`
```json
{
  "timestamp": "2026-02-17T10:31:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Username already exists"
}
```

---

### Test 2.3: Register Duplicate Email

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "different_user",
    "email": "john.doe@imbank.com",
    "password": "Pass123"
  }'
```

**Expected Response:** `400 Bad Request`
```json
{
  "timestamp": "2026-02-17T10:32:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Email already exists"
}
```

---

### Test 2.4: Login with Username

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin",
    "password": "Admin@123"
  }'
```

**Expected Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjoiUk9MRV9BRE1JTixST0xFX1VTRVIiLCJpYXQiOjE3MDgxNjQwMDAsImV4cCI6MTcwODE2NDkwMCwiaXNzIjoiaW1iYW5rLWFwaSJ9.signature",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "userId": 1,
  "username": "admin",
  "email": "admin@imbank.com"
}
```

**What to verify:**
- ✅ Status code is 200
- ✅ `accessToken` is a JWT (3 parts separated by dots)
- ✅ `refreshToken` is a UUID
- ✅ `expiresIn` is 900 seconds (15 minutes)
- ✅ `tokenType` is "Bearer"

**Save these values for next tests:**
- `ACCESS_TOKEN` = value of `accessToken`
- `REFRESH_TOKEN` = value of `refreshToken`

---

### Test 2.5: Login with Email

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin@imbank.com",
    "password": "Admin@123"
  }'
```

**Expected Response:** `200 OK` (same structure as Test 2.4)

---

### Test 2.6: Login with Wrong Password

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin",
    "password": "WrongPassword"
  }'
```

**Expected Response:** `401 Unauthorized`
```json
{
  "timestamp": "2026-02-17T10:35:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Bad credentials"
}
```

---

### Test 2.7: Login with Non-Existent User

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "nonexistent",
    "password": "SomePassword"
  }'
```

**Expected Response:** `401 Unauthorized`
```json
{
  "timestamp": "2026-02-17T10:36:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Bad credentials"
}
```

---

### Test 2.8: Get Current User Profile

**Request:**
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Expected Response:** `200 OK`
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

**What to verify:**
- ✅ Returns current authenticated user info
- ✅ Shows all assigned roles
- ✅ Password is NOT included

---

## 3. Authorization Tests (RBAC)

### Setup: Create Test Users for Each Role

**3.1 Create Regular User (ROLE_USER only)**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "regular_user",
    "email": "user@imbank.com",
    "password": "User123"
  }'
```

Login and save token:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "regular_user",
    "password": "User123"
  }'
```
**Save as:** `USER_TOKEN`

---

**3.2 Create Manager User (ROLE_MANAGER + ROLE_USER)**

First, create the user:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "manager_user",
    "email": "manager@imbank.com",
    "password": "Manager123"
  }'
```

Then, as admin, promote to manager (you'll need to create this endpoint or manually update the database):
```sql
-- Run this in your database
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u
CROSS JOIN roles r
WHERE u.username = 'manager_user' AND r.name = 'ROLE_MANAGER';
```

Login and save token:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "manager_user",
    "password": "Manager123"
  }'
```
**Save as:** `MANAGER_TOKEN`

---

**3.3 Use Admin User (ROLE_ADMIN + ROLE_USER)**

Already created. Login:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin",
    "password": "Admin@123"
  }'
```
**Save as:** `ADMIN_TOKEN`

---

### Test 3.1: Access Public Endpoint (No Auth Required)

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_public",
    "email": "public@test.com",
    "password": "Test123"
  }'
```

**Expected:** `201 Created` - Anyone can register

---

### Test 3.2: Access Protected Endpoint Without Token

**Request:**
```bash
curl -X GET http://localhost:8080/api/employees
```

**Expected Response:** `401 Unauthorized`
```json
{
  "timestamp": "2026-02-17T10:40:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required. Please login.",
  "path": "/api/employees"
}
```

---

### Test 3.3: Access Protected Endpoint WITH Token (Any Role)

**Request:**
```bash
curl -X GET http://localhost:8080/api/employees \
  -H "Authorization: Bearer YOUR_USER_TOKEN"
```

**Expected:** `200 OK` with employee list

**What to verify:**
- ✅ ROLE_USER can read employees
- ✅ ROLE_MANAGER can read employees
- ✅ ROLE_ADMIN can read employees

---

### Test 3.4: Create Employee as ROLE_USER (Should Fail)

Assuming your EmployeeController has:
```java
@PostMapping
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public EmployeeDto createEmployee(@RequestBody EmployeeDto dto) { ... }
```

**Request:**
```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Authorization: Bearer YOUR_USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test Employee",
    "email": "test@imbank.com",
    "departmentId": 1
  }'
```

**Expected Response:** `403 Forbidden`
```json
{
  "timestamp": "2026-02-17T10:42:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You don't have permission to access this resource",
  "path": "/api/employees"
}
```

---

### Test 3.5: Create Employee as ROLE_MANAGER (Should Succeed)

**Request:**
```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Authorization: Bearer YOUR_MANAGER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Manager Created Employee",
    "email": "manager.created@imbank.com",
    "departmentId": 1
  }'
```

**Expected:** `201 Created` with employee details

---

### Test 3.6: Create Employee as ROLE_ADMIN (Should Succeed)

**Request:**
```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Admin Created Employee",
    "email": "admin.created@imbank.com",
    "departmentId": 1
  }'
```

**Expected:** `201 Created` with employee details

---

### Test 3.7: Delete Employee as ROLE_USER (Should Fail)

Assuming your EmployeeController has:
```java
@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public void deleteEmployee(@PathVariable Long id) { ... }
```

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/employees/1 \
  -H "Authorization: Bearer YOUR_USER_TOKEN"
```

**Expected Response:** `403 Forbidden`

---

### Test 3.8: Delete Employee as ROLE_MANAGER (Should Fail)

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/employees/1 \
  -H "Authorization: Bearer YOUR_MANAGER_TOKEN"
```

**Expected Response:** `403 Forbidden`

---

### Test 3.9: Delete Employee as ROLE_ADMIN (Should Succeed)

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/employees/1 \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

**Expected:** `204 No Content` or `200 OK`

---

## 4. Token Management Tests

### Test 4.1: Refresh Access Token

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

**Expected Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...NEW_TOKEN",
  "refreshToken": "660e8400-e29b-41d4-a716-446655440111",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "userId": 1,
  "username": "admin",
  "email": "admin@imbank.com"
}
```

**What to verify:**
- ✅ New `accessToken` is different from old one
- ✅ New `refreshToken` is different (token rotation)
- ✅ Old refresh token is now invalid

---

### Test 4.2: Use Old Refresh Token (Should Fail)

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "OLD_REFRESH_TOKEN"
  }'
```

**Expected Response:** `400 Bad Request`
```json
{
  "timestamp": "2026-02-17T10:45:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid or expired refresh token"
}
```

---

### Test 4.3: Use Expired Access Token

**Wait 15+ minutes or manually set a short expiry for testing**

**Request:**
```bash
curl -X GET http://localhost:8080/api/employees \
  -H "Authorization: Bearer EXPIRED_TOKEN"
```

**Expected Response:** `401 Unauthorized`
```json
{
  "timestamp": "2026-02-17T10:50:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required. Please login.",
  "path": "/api/employees"
}
```

---

### Test 4.4: Logout User

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Expected Response:** `204 No Content`

**What to verify:**
- ✅ Status is 204 (no body)
- ✅ All refresh tokens for this user are revoked in database

---

### Test 4.5: Use Refresh Token After Logout (Should Fail)

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "TOKEN_FROM_BEFORE_LOGOUT"
  }'
```

**Expected Response:** `400 Bad Request`
```json
{
  "timestamp": "2026-02-17T10:52:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid or expired refresh token"
}
```

---

## 5. Error Scenario Tests

### Test 5.1: Invalid JWT Format

**Request:**
```bash
curl -X GET http://localhost:8080/api/employees \
  -H "Authorization: Bearer invalid.token.format"
```

**Expected:** `401 Unauthorized`

---

### Test 5.2: Missing Authorization Header

**Request:**
```bash
curl -X GET http://localhost:8080/api/employees
```

**Expected:** `401 Unauthorized`

---

### Test 5.3: Malformed Authorization Header

**Request:**
```bash
curl -X GET http://localhost:8080/api/employees \
  -H "Authorization: InvalidFormat"
```

**Expected:** `401 Unauthorized`

---

### Test 5.4: Invalid JSON in Request Body

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{invalid json'
```

**Expected:** `400 Bad Request`

---

### Test 5.5: Missing Required Fields

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin"
  }'
```

**Expected Response:** `400 Bad Request`
```json
{
  "timestamp": "2026-02-17T10:55:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Password is required"
}
```

---

### Test 5.6: Empty String Fields

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "",
    "password": "Admin@123"
  }'
```

**Expected Response:** `400 Bad Request`
```json
{
  "timestamp": "2026-02-17T10:56:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Username or email is required"
}
```

---

## 6. Postman Collection

### Import this JSON into Postman:

```json
{
  "info": {
    "name": "IMBank JWT Auth & RBAC",
    "description": "Complete test suite for JWT authentication and role-based access control",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    {
      "key": "base_url",
      "value": "http://localhost:8080"
    },
    {
      "key": "access_token",
      "value": ""
    },
    {
      "key": "refresh_token",
      "value": ""
    },
    {
      "key": "user_token",
      "value": ""
    },
    {
      "key": "manager_token",
      "value": ""
    },
    {
      "key": "admin_token",
      "value": ""
    }
  ],
  "item": [
    {
      "name": "Authentication",
      "item": [
        {
          "name": "Register New User",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "pm.test('Status code is 201', function () {",
                  "    pm.response.to.have.status(201);",
                  "});",
                  "",
                  "pm.test('Response has required fields', function () {",
                  "    var jsonData = pm.response.json();",
                  "    pm.expect(jsonData).to.have.property('id');",
                  "    pm.expect(jsonData).to.have.property('username');",
                  "    pm.expect(jsonData).to.have.property('email');",
                  "    pm.expect(jsonData).to.have.property('roles');",
                  "});",
                  "",
                  "pm.test('User has ROLE_USER by default', function () {",
                  "    var jsonData = pm.response.json();",
                  "    pm.expect(jsonData.roles).to.include('ROLE_USER');",
                  "});",
                  "",
                  "pm.test('Password not returned', function () {",
                  "    var jsonData = pm.response.json();",
                  "    pm.expect(jsonData).to.not.have.property('password');",
                  "});"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"username\": \"john_doe\",\n  \"email\": \"john.doe@imbank.com\",\n  \"password\": \"JohnPass123\"\n}"
            },
            "url": {
              "raw": "{{base_url}}/api/auth/register",
              "host": ["{{base_url}}"],
              "path": ["api", "auth", "register"]
            }
          }
        },
        {
          "name": "Login with Username",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "pm.test('Status code is 200', function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "",
                  "pm.test('Response has tokens', function () {",
                  "    var jsonData = pm.response.json();",
                  "    pm.expect(jsonData).to.have.property('accessToken');",
                  "    pm.expect(jsonData).to.have.property('refreshToken');",
                  "    pm.expect(jsonData.tokenType).to.eql('Bearer');",
                  "});",
                  "",
                  "// Save tokens to environment",
                  "var jsonData = pm.response.json();",
                  "pm.collectionVariables.set('access_token', jsonData.accessToken);",
                  "pm.collectionVariables.set('refresh_token', jsonData.refreshToken);",
                  "pm.collectionVariables.set('admin_token', jsonData.accessToken);",
                  "",
                  "pm.test('Token expires in 900 seconds', function () {",
                  "    pm.expect(jsonData.expiresIn).to.eql(900);",
                  "});"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"usernameOrEmail\": \"admin\",\n  \"password\": \"Admin@123\"\n}"
            },
            "url": {
              "raw": "{{base_url}}/api/auth/login",
              "host": ["{{base_url}}"],
              "path": ["api", "auth", "login"]
            }
          }
        },
        {
          "name": "Login with Email",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "pm.test('Status code is 200', function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "",
                  "pm.test('Login with email works', function () {",
                  "    var jsonData = pm.response.json();",
                  "    pm.expect(jsonData).to.have.property('accessToken');",
                  "});"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"usernameOrEmail\": \"admin@imbank.com\",\n  \"password\": \"Admin@123\"\n}"
            },
            "url": {
              "raw": "{{base_url}}/api/auth/login",
              "host": ["{{base_url}}"],
              "path": ["api", "auth", "login"]
            }
          }
        },
        {
          "name": "Get Current User Profile",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "pm.test('Status code is 200', function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "",
                  "pm.test('Returns user profile', function () {",
                  "    var jsonData = pm.response.json();",
                  "    pm.expect(jsonData).to.have.property('username');",
                  "    pm.expect(jsonData).to.have.property('roles');",
                  "});",
                  "",
                  "pm.test('Password not included', function () {",
                  "    var jsonData = pm.response.json();",
                  "    pm.expect(jsonData).to.not.have.property('password');",
                  "});"
                ]
              }
            }
          ],
          "request": {
            "auth": {
              "type": "bearer",
              "bearer": [
                {
                  "key": "token",
                  "value": "{{access_token}}"
                }
              ]
            },
            "method": "GET",
            "header": [],
            "url": {
              "raw": "{{base_url}}/api/auth/me",
              "host": ["{{base_url}}"],
              "path": ["api", "auth", "me"]
            }
          }
        },
        {
          "name": "Refresh Token",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "pm.test('Status code is 200', function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "",
                  "pm.test('Returns new tokens', function () {",
                  "    var jsonData = pm.response.json();",
                  "    pm.expect(jsonData).to.have.property('accessToken');",
                  "    pm.expect(jsonData).to.have.property('refreshToken');",
                  "});",
                  "",
                  "// Update tokens",
                  "var jsonData = pm.response.json();",
                  "pm.collectionVariables.set('access_token', jsonData.accessToken);",
                  "pm.collectionVariables.set('refresh_token', jsonData.refreshToken);"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"refreshToken\": \"{{refresh_token}}\"\n}"
            },
            "url": {
              "raw": "{{base_url}}/api/auth/refresh",
              "host": ["{{base_url}}"],
              "path": ["api", "auth", "refresh"]
            }
          }
        },
        {
          "name": "Logout",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "pm.test('Status code is 204', function () {",
                  "    pm.response.to.have.status(204);",
                  "});"
                ]
              }
            }
          ],
          "request": {
            "auth": {
              "type": "bearer",
              "bearer": [
                {
                  "key": "token",
                  "value": "{{access_token}}"
                }
              ]
            },
            "method": "POST",
            "header": [],
            "url": {
              "raw": "{{base_url}}/api/auth/logout",
              "host": ["{{base_url}}"],
              "path": ["api", "auth", "logout"]
            }
          }
        }
      ]
    },
    {
      "name": "Error Scenarios",
      "item": [
        {
          "name": "Login with Wrong Password",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "pm.test('Status code is 401', function () {",
                  "    pm.response.to.have.status(401);",
                  "});",
                  "",
                  "pm.test('Error message is Bad credentials', function () {",
                  "    var jsonData = pm.response.json();",
                  "    pm.expect(jsonData.message).to.include('Bad credentials');",
                  "});"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"usernameOrEmail\": \"admin\",\n  \"password\": \"WrongPassword\"\n}"
            },
            "url": {
              "raw": "{{base_url}}/api/auth/login",
              "host": ["{{base_url}}"],
              "path": ["api", "auth", "login"]
            }
          }
        },
        {
          "name": "Access Protected Resource Without Token",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "pm.test('Status code is 401', function () {",
                  "    pm.response.to.have.status(401);",
                  "});",
                  "",
                  "pm.test('Authentication required message', function () {",
                  "    var jsonData = pm.response.json();",
                  "    pm.expect(jsonData.message).to.include('Authentication required');",
                  "});"
                ]
              }
            }
          ],
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "{{base_url}}/api/employees",
              "host": ["{{base_url}}"],
              "path": ["api", "employees"]
            }
          }
        },
        {
          "name": "Register Duplicate Username",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "pm.test('Status code is 400', function () {",
                  "    pm.response.to.have.status(400);",
                  "});",
                  "",
                  "pm.test('Error message about duplicate username', function () {",
                  "    var jsonData = pm.response.json();",
                  "    pm.expect(jsonData.message).to.include('Username already exists');",
                  "});"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"username\": \"admin\",\n  \"email\": \"different@imbank.com\",\n  \"password\": \"Pass123\"\n}"
            },
            "url": {
              "raw": "{{base_url}}/api/auth/register",
              "host": ["{{base_url}}"],
              "path": ["api", "auth", "register"]
            }
          }
        }
      ]
    },
    {
      "name": "RBAC Tests",
      "item": [
        {
          "name": "View Employees (Any Authenticated User)",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "pm.test('Status code is 200', function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "",
                  "pm.test('Returns array of employees', function () {",
                  "    var jsonData = pm.response.json();",
                  "    pm.expect(jsonData).to.be.an('array');",
                  "});"
                ]
              }
            }
          ],
          "request": {
            "auth": {
              "type": "bearer",
              "bearer": [
                {
                  "key": "token",
                  "value": "{{access_token}}"
                }
              ]
            },
            "method": "GET",
            "header": [],
            "url": {
              "raw": "{{base_url}}/api/employees",
              "host": ["{{base_url}}"],
              "path": ["api", "employees"]
            }
          }
        },
        {
          "name": "Create Employee as Admin (Should Succeed)",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "pm.test('Status code is 201 or 200', function () {",
                  "    pm.expect(pm.response.code).to.be.oneOf([200, 201]);",
                  "});"
                ]
              }
            }
          ],
          "request": {
            "auth": {
              "type": "bearer",
              "bearer": [
                {
                  "key": "token",
                  "value": "{{admin_token}}"
                }
              ]
            },
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"fullName\": \"Test Employee\",\n  \"email\": \"test.employee@imbank.com\",\n  \"departmentId\": 1\n}"
            },
            "url": {
              "raw": "{{base_url}}/api/employees",
              "host": ["{{base_url}}"],
              "path": ["api", "employees"]
            }
          }
        },
        {
          "name": "View Departments (Any Authenticated User)",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "pm.test('Status code is 200', function () {",
                  "    pm.response.to.have.status(200);",
                  "});"
                ]
              }
            }
          ],
          "request": {
            "auth": {
              "type": "bearer",
              "bearer": [
                {
                  "key": "token",
                  "value": "{{access_token}}"
                }
              ]
            },
            "method": "GET",
            "header": [],
            "url": {
              "raw": "{{base_url}}/api/departments",
              "host": ["{{base_url}}"],
              "path": ["api", "departments"]
            }
          }
        }
      ]
    }
  ]
}
```

---

## 7. Testing Workflow

### Complete Test Sequence:

```bash
# 1. Start your application
./mvnw spring-boot:run

# 2. Register a test user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@imbank.com","password":"Test123"}'

# 3. Login as admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"Admin@123"}' \
  | jq '.accessToken' | tr -d '"'

# Save the token in a variable
export TOKEN="paste_token_here"

# 4. Test authenticated access
curl -X GET http://localhost:8080/api/employees \
  -H "Authorization: Bearer $TOKEN"

# 5. Test current user endpoint
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"

# 6. Test logout
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer $TOKEN"

# 7. Verify token is invalid after logout
curl -X GET http://localhost:8080/api/employees \
  -H "Authorization: Bearer $TOKEN"
# Should return 401
```

---

## 8. Quick Reference Table

| Test Case | Endpoint | Method | Auth | Expected Status |
|-----------|----------|--------|------|-----------------|
| Register | `/api/auth/register` | POST | No | 201 |
| Login | `/api/auth/login` | POST | No | 200 |
| Get Profile | `/api/auth/me` | GET | Yes | 200 |
| Refresh Token | `/api/auth/refresh` | POST | No | 200 |
| Logout | `/api/auth/logout` | POST | Yes | 204 |
| View Employees | `/api/employees` | GET | Yes (Any) | 200 |
| Create Employee | `/api/employees` | POST | Yes (Manager/Admin) | 201 |
| Delete Employee | `/api/employees/{id}` | DELETE | Yes (Admin only) | 204 |
| No Token | `/api/employees` | GET | No | 401 |
| Wrong Password | `/api/auth/login` | POST | No | 401 |
| Insufficient Role | `/api/employees/{id}` | DELETE | Yes (User) | 403 |

---

**Created by:** Jack Kinyanjui
**Date:** February 17, 2026
**Version:** 1.0

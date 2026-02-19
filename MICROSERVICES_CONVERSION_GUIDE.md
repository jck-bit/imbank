# 🎯 IMBank Microservices Conversion Guide

## Your Current Monolith Structure

```
imbank/
└── src/main/java/com/example/imbank/
    ├── ImbankApplication.java
    ├── controller/
    │   ├── AuthController.java           ← Will go to Auth Service
    │   ├── UserManagementController.java ← Will go to Auth Service
    │   ├── EmployeeController.java       ← Will go to Employee Service
    │   └── DepartmentController.java     ← Will go to Employee Service
    ├── entity/
    │   ├── User.java                     ← Auth Service
    │   ├── Role.java                     ← Auth Service
    │   ├── RefreshToken.java             ← Auth Service
    │   ├── Employee.java                 ← Employee Service
    │   └── Department.java               ← Employee Service
    ├── security/                         ← Auth Service
    │   ├── JwtTokenProvider.java
    │   ├── CustomUserDetails.java
    │   └── ...
    └── ...
```

---

## 🎯 Goal: Split into 2 Microservices

```
Eureka Server (8761)          ← NEW project
    ↓
API Gateway (9090)            ← NEW project
    ↓
├── Auth Service (8081)       ← Extract from IMBank
│   ├── User, Role entities
│   ├── AuthController
│   ├── UserManagementController
│   ├── All security/* files
│   └── Database: auth_db
│
└── Employee Service (8082)   ← Extract from IMBank
    ├── Employee, Department entities
    ├── EmployeeController
    ├── DepartmentController
    └── Database: employee_db
```

---

## 📋 Step-by-Step Conversion Process

### Phase 1: Create Infrastructure Projects (Do this first!)

#### Step 1.1: Create Eureka Server
```bash
# In your IdeaProjects folder, create:
/IdeaProjects/imbank-eureka-server/

# Files you need:
├── pom.xml
├── src/main/java/com/example/eureka/EurekaServerApplication.java
└── src/main/resources/application.yml
```

**What to put in each file:**

**pom.xml:**
- Add Spring Cloud Netflix Eureka Server dependency
- Spring Boot version: 4.0.2
- Spring Cloud version: 2024.0.0

**EurekaServerApplication.java:**
```java
@SpringBootApplication
@EnableEurekaServer  // ← This is the key annotation!
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

**application.yml:**
```yaml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false  # Don't register itself
    fetch-registry: false
```

**Test it:** Run the app, go to http://localhost:8761 - you'll see Eureka dashboard!

---

#### Step 1.2: Create API Gateway
```bash
# In your IdeaProjects folder, create:
/IdeaProjects/imbank-api-gateway/

# Files you need:
├── pom.xml
├── src/main/java/com/example/gateway/ApiGatewayApplication.java
└── src/main/resources/application.yml
```

**pom.xml:**
- Add Spring Cloud Gateway dependency
- Add Eureka Client dependency

**ApiGatewayApplication.java:**
```java
@SpringBootApplication
@EnableDiscoveryClient  // ← Registers with Eureka
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

**application.yml:**
```yaml
server:
  port: 9090

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        # Route auth requests to Auth Service
        - id: auth-service
          uri: lb://AUTH-SERVICE  # lb = load balanced via Eureka
          predicates:
            - Path=/api/auth/**

        # Route employee requests to Employee Service
        - id: employee-service
          uri: lb://EMPLOYEE-SERVICE
          predicates:
            - Path=/api/employees/**,/api/departments/**

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

---

### Phase 2: Extract Auth Service from IMBank

#### Step 2.1: Create Auth Service Project Structure

```bash
# Copy your entire imbank project:
cp -r /IdeaProjects/imbank /IdeaProjects/imbank-auth-service
```

#### Step 2.2: Modify Auth Service

**In `imbank-auth-service/pom.xml`, ADD:**
```xml
<dependencies>
    <!-- Add Eureka Client -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>

    <!-- Keep all your existing dependencies -->
    ...
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2024.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

**In `ImbankApplication.java`, ADD:**
```java
@SpringBootApplication
@EnableDiscoveryClient  // ← Add this!
@EnableJpaAuditing
public class ImbankApplication {
    // ... same as before
}
```

**In `application.properties`, CHANGE:**
```properties
# Change port
server.port=8081

# Add service name
spring.application.name=AUTH-SERVICE

# Add Eureka config
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true

# Change database name
spring.datasource.url=jdbc:mysql://your-host:3306/auth_db
```

#### Step 2.3: Delete What Auth Service Doesn't Need

**DELETE these files from imbank-auth-service:**
```
controller/
├── EmployeeController.java      ← DELETE
└── DepartmentController.java    ← DELETE

entity/
├── Employee.java                ← DELETE
└── Department.java              ← DELETE

repository/
├── EmployeeRepository.java      ← DELETE
└── DepartmentRepository.java    ← DELETE

service/
├── EmployeeService.java         ← DELETE
├── EmployeeServiceImpl.java     ← DELETE
├── DepartmentService.java       ← DELETE
└── DepartmentServiceImpl.java   ← DELETE

dto/
├── EmployeeRequestDto.java      ← DELETE
├── EmployeeResponseDto.java     ← DELETE
├── DepartmentRequestDto.java    ← DELETE
└── DepartmentResponseDto.java   ← DELETE
```

**KEEP these in Auth Service:**
- AuthController.java ✓
- UserManagementController.java ✓
- User.java, Role.java, RefreshToken.java ✓
- All security/* files ✓
- All auth-related DTOs ✓

---

### Phase 3: Extract Employee Service from IMBank

#### Step 3.1: Create Employee Service Project Structure

```bash
# Copy your entire imbank project again:
cp -r /IdeaProjects/imbank /IdeaProjects/imbank-employee-service
```

#### Step 3.2: Modify Employee Service

**In `imbank-employee-service/pom.xml`, ADD:**
```xml
<dependencies>
    <!-- Add Eureka Client -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>

    <!-- Add for calling other services -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>

    <!-- Keep existing dependencies -->
    ...
</dependencies>
```

**In `ImbankApplication.java`, ADD:**
```java
@SpringBootApplication
@EnableDiscoveryClient  // ← Add this!
@EnableFeignClients     // ← Add this for calling Auth Service!
@EnableJpaAuditing
public class ImbankApplication {
    // ... same as before
}
```

**In `application.properties`, CHANGE:**
```properties
# Change port
server.port=8082

# Add service name
spring.application.name=EMPLOYEE-SERVICE

# Add Eureka config
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# Change database name
spring.datasource.url=jdbc:mysql://your-host:3306/employee_db
```

#### Step 3.3: Delete What Employee Service Doesn't Need

**DELETE these files from imbank-employee-service:**
```
controller/
├── AuthController.java              ← DELETE
└── UserManagementController.java    ← DELETE

entity/
├── User.java                        ← DELETE (mostly)
├── Role.java                        ← DELETE
└── RefreshToken.java                ← DELETE

security/
└── [KEEP BUT MODIFY - see below]

repository/
├── UserRepository.java              ← DELETE
├── RoleRepository.java              ← DELETE
└── RefreshTokenRepository.java      ← DELETE

service/
├── AuthService.java                 ← DELETE
├── AuthServiceImpl.java             ← DELETE
└── UserManagementService.java       ← DELETE

dto/
└── [DELETE all auth-related DTOs]
```

**KEEP these in Employee Service:**
- EmployeeController.java ✓
- DepartmentController.java ✓
- Employee.java, Department.java ✓
- Employee and Department repositories ✓
- Employee and Department services ✓

---

### Phase 4: Configure Service-to-Service Communication

**The impressive part: Employee Service calls Auth Service via Eureka!**

#### In Employee Service, create a Feign Client:

**Create file: `imbank-employee-service/src/main/java/com/example/imbank/client/AuthServiceClient.java`**

```java
package com.example.imbank.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "AUTH-SERVICE")  // ← Eureka will find this!
public interface AuthServiceClient {

    @GetMapping("/api/auth/validate")
    boolean validateToken(@RequestHeader("Authorization") String token);

    @GetMapping("/api/auth/me")
    UserDto getCurrentUser(@RequestHeader("Authorization") String token);
}
```

**Now in EmployeeController, you can call Auth Service:**

```java
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private AuthServiceClient authServiceClient;  // ← Injected!

    @PostMapping
    public EmployeeDto createEmployee(
        @RequestHeader("Authorization") String token,
        @RequestBody EmployeeDto dto) {

        // Call Auth Service via Eureka!
        UserDto user = authServiceClient.getCurrentUser(token);

        // Now you have user info without Auth Service being in same app!
        System.out.println("Creating employee, requested by: " + user.getUsername());

        return employeeService.createEmployee(dto);
    }
}
```

---

### Phase 5: Database Setup

You need to create 2 separate databases:

**In MySQL:**
```sql
-- Create Auth database
CREATE DATABASE auth_db;

-- Create Employee database
CREATE DATABASE employee_db;
```

**Run Liquibase migrations:**
```bash
# In Auth Service - migrate auth tables
cd imbank-auth-service
./mvnw liquibase:update

# In Employee Service - migrate employee tables
cd imbank-employee-service
./mvnw liquibase:update
```

---

## 🚀 Running the Microservices Demo

### Start Order (IMPORTANT!):

```bash
# Terminal 1: Start Eureka Server
cd imbank-eureka-server
./mvnw spring-boot:run
# Wait until you see "Started EurekaServerApplication"
# Go to http://localhost:8761 - dashboard should show

# Terminal 2: Start Auth Service
cd imbank-auth-service
./mvnw spring-boot:run
# Check Eureka dashboard - AUTH-SERVICE should appear!

# Terminal 3: Start Employee Service
cd imbank-employee-service
./mvnw spring-boot:run
# Check Eureka dashboard - EMPLOYEE-SERVICE should appear!

# Terminal 4: Start API Gateway
cd imbank-api-gateway
./mvnw spring-boot:run
# Gateway should register all services
```

---

## 🎯 Demo Flow for Your Tech Lead

### Demo 1: Service Discovery via Eureka

**Show Eureka Dashboard:**
```
1. Open http://localhost:8761
2. Show both services registered:
   - AUTH-SERVICE (port 8081)
   - EMPLOYEE-SERVICE (port 8082)
3. Point out: "Services found each other dynamically!"
```

### Demo 2: API Gateway Routing

**Test routing through Gateway:**

```bash
# Login via Gateway (routes to Auth Service)
curl -X POST http://localhost:9090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"Admin@123"}'

# Get employees via Gateway (routes to Employee Service)
curl -X GET http://localhost:9090/api/employees \
  -H "Authorization: Bearer <token>"
```

**Explain:** "Client only knows about Gateway at 9090. Gateway routes to correct service via Eureka!"

### Demo 3: Service-to-Service Communication

**Show the impressive part:**

```bash
# Create employee via Gateway
curl -X POST http://localhost:9090/api/employees \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","email":"test@example.com","departmentId":1}'
```

**Check Employee Service logs - you'll see:**
```
Creating employee, requested by: admin
Calling AUTH-SERVICE via Eureka to validate...
```

**Explain:** "Employee Service discovered Auth Service through Eureka and called it - no hardcoded URLs!"

### Demo 4: High Availability

**Show what happens if one service goes down:**

```bash
# Stop Auth Service (Ctrl+C in Terminal 2)
# Check Eureka - AUTH-SERVICE marked as DOWN

# Try to login via Gateway
curl -X POST http://localhost:9090/api/auth/login ...
# Gets error: "Service unavailable"

# But employees endpoint still works (separate service)!
curl -X GET http://localhost:9090/api/employees ...
# Works! (if not calling auth service)
```

**Explain:** "Services are isolated. Employee Service stays up even if Auth crashes!"

---

## 📊 Architecture Diagram to Show Tech Lead

```
                    Client (Postman/Browser)
                            ↓
                    API Gateway :9090
                    ┌──────────────────┐
                    │ • JWT Validation │
                    │ • Rate Limiting  │
                    │ • Routing        │
                    └────────┬─────────┘
                             │
                    Eureka Server :8761
                    ┌──────────────────┐
                    │ Service Registry │
                    │ ┌──────────────┐ │
                    │ │AUTH-SERVICE  │ │
                    │ │(8081)        │ │
                    │ └──────────────┘ │
                    │ ┌──────────────┐ │
                    │ │EMPLOYEE-SVC  │ │
                    │ │(8082)        │ │
                    │ └──────────────┘ │
                    └────────┬─────────┘
                             │
              ┌──────────────┴──────────────┐
              ↓                             ↓
    Auth Service :8081          Employee Service :8082
    ┌────────────────┐          ┌─────────────────────┐
    │• Login         │          │• CRUD Employees     │
    │• Register      │◄─────────│• Calls Auth via     │
    │• JWT           │  Feign   │  Feign Client       │
    │• User Mgmt     │          │• CRUD Departments   │
    └───────┬────────┘          └──────────┬──────────┘
            ↓                              ↓
      auth_db (MySQL)              employee_db (MySQL)
```

---

## 🎤 What to Say to Your Tech Lead

**Opening:**
"I've refactored our monolith into microservices architecture using Spring Cloud."

**Point 1 - Service Discovery:**
"Services register with Eureka Server. No hardcoded URLs - they discover each other dynamically."

**Point 2 - API Gateway:**
"Single entry point for all clients. Gateway handles routing, authentication, and load balancing."

**Point 3 - Service Communication:**
"Employee Service calls Auth Service using Feign Client with Eureka discovery - fully decoupled."

**Point 4 - Independent Deployment:**
"Each service has its own database and can be deployed independently. If Auth crashes, Employee Service stays up."

**Point 5 - Scalability:**
"We can run multiple instances of Employee Service for high traffic. Eureka handles load balancing automatically."

---

## ⚠️ Common Issues You Might Face

### Issue 1: Services Not Registering with Eureka
**Solution:** Check `eureka.client.service-url.defaultZone` is correct in both services

### Issue 2: Gateway Can't Route
**Solution:** Make sure service names in Gateway config match `spring.application.name` exactly

### Issue 3: Database Connection Issues
**Solution:** Each service needs its own database. Check URLs are different.

### Issue 4: Port Already in Use
**Solution:** Make sure each service runs on different port (8081, 8082, 8761, 9090)

### Issue 5: Feign Client Not Working
**Solution:** Add `@EnableFeignClients` to main application class

---

## 📝 Checklist Before Demo

- [ ] Eureka Server running on 8761
- [ ] Can see Eureka dashboard
- [ ] Auth Service running on 8081
- [ ] Employee Service running on 8082
- [ ] API Gateway running on 9090
- [ ] Both services visible in Eureka dashboard
- [ ] Can login via Gateway
- [ ] Can get employees via Gateway
- [ ] Employee Service logs show it's calling Auth Service
- [ ] Tested stopping one service - other stays up

---

## 🎓 Key Concepts to Explain

1. **Service Discovery**: Services find each other via Eureka (like a phone book)
2. **Load Balancing**: `lb://SERVICE-NAME` - Eureka picks healthy instance
3. **Client-Side Load Balancing**: Gateway/Feign decide which instance to call
4. **Circuit Breaker**: (Future) What happens when service is down
5. **Distributed Tracing**: (Future) Track requests across services

---

**Good luck with your demo! Your tech lead will be impressed! 🚀**

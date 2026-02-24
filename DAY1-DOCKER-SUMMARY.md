# Day 1 Complete: Docker Setup ✅

## What We Accomplished Today

### 1. Installed Docker (Colima)
- Installed Docker via Homebrew with Colima
- Colima is a lightweight Docker runtime for macOS
- Verified installation: Docker v29.2.1, Docker Compose v5.0.2

### 2. Created Dockerfiles for All 6 Microservices
Each Dockerfile uses a **multi-stage build**:

#### **Stage 1: Build (Maven)**
- Uses `maven:3.9-eclipse-temurin-21-alpine` image
- Copies parent `pom.xml` and service `pom.xml`
- Downloads dependencies
- Compiles Java code with `mvn clean package`
- Produces a JAR file

#### **Stage 2: Runtime (JRE)**
- Uses `eclipse-temurin:21-jre-alpine` (smaller, just runtime)
- Copies only the JAR file from build stage
- Runs as non-root user (security best practice)
- Includes health checks for container orchestration
- Exposes service port

**Services and Ports:**
- `imbank-config-server:latest` - Port 8888
- `imbank-eureka-server:latest` - Port 8761
- `imbank-api-gateway:latest` - Port 8080
- `imbank-auth-service:latest` - Port 8081
- `imbank-employee-service:latest` - Port 8082
- `imbank-department-service:latest` - Port 8083

### 3. Created docker-compose.yml
Orchestrates all 6 services with:
- **Startup order**: Config Server → Eureka → Other services → Gateway
- **Health checks**: Each service waits for dependencies to be healthy
- **Networking**: All services on `imbank-network` bridge network
- **Environment variables**: Spring profiles, config server URL, Eureka URL

### 4. Built All Docker Images
All 6 images successfully built and ready to run!

**Image sizes:**
- config-server: 414MB (compressed: 135MB)
- eureka-server: 398MB (compressed: 127MB)
- api-gateway: ~400MB
- auth-service: 477MB (compressed: 165MB)
- employee-service: 479MB (compressed: 166MB)
- department-service: 673MB (compressed: 257MB)

## Key Docker Concepts You Learned

### Multi-Stage Builds
- **Why?** Keeps final image small by separating build tools from runtime
- **Build stage**: Has Maven, compiles code (large)
- **Runtime stage**: Only has JRE and JAR file (small)
- Result: Production images are 60% smaller

### Docker Layers & Caching
- Each line in Dockerfile creates a layer
- Docker caches unchanged layers
- Order matters: put things that change less often first
- Example: Copy `pom.xml` → download deps → copy source code
- If source changes, deps layer is still cached

### Build Context
- The "context" is what Docker can access during build
- We set context to project root (`.`) in docker-compose.yml
- This allows access to parent `pom.xml` and all services
- Without this, each service couldn't see the parent POM

### Container Networking
- All containers on `imbank-network` can talk to each other
- Use service name as hostname: `http://config-server:8888`
- Ports are exposed to host: `8080:8080` (host:container)

## What to Verify Tomorrow Morning

Run these commands to check everything:

```bash
# List all images
docker images | grep imbank

# Should see 6 images:
# - imbank-config-server
# - imbank-eureka-server
# - imbank-api-gateway
# - imbank-auth-service
# - imbank-employee-service
# - imbank-department-service
```

## Tomorrow's Plan (Day 2)

### Morning Session (~2 hours)
1. **Test Docker locally**
   - Run `docker-compose up`
   - Verify all services start correctly
   - Test endpoints through gateway
   - Understand container logs

2. **Understand Dockerfiles in detail**
   - Walk through each Dockerfile line by line
   - Explain build stages, layers, and optimization
   - Show how to debug container issues

### Afternoon Session (~2-3 hours)
3. **Setup AWS**
   - Install AWS CLI
   - Configure AWS credentials
   - Create ECR (Elastic Container Registry) repositories
   - Tag and push images to ECR

4. **Setup Jenkins**
   - Install Jenkins locally OR on EC2
   - Configure Jenkins plugins (Docker, Git, AWS)
   - Create basic Jenkinsfile
   - Test pipeline locally

5. **Deploy to AWS ECS**
   - Create ECS cluster
   - Create task definitions
   - Deploy services
   - Setup load balancer
   - Test production deployment

## Files Created Today

```
imbank/
├── docker-compose.yml                    # Orchestrates all services
├── .dockerignore                         # Excludes files from build
├── DAY1-DOCKER-SUMMARY.md               # This file
├── imbank-config-server/
│   └── Dockerfile                        # Multi-stage build
├── imbank-eureka-server/
│   └── Dockerfile
├── imbank-api-gateway/
│   └── Dockerfile
├── imbank-auth-service/
│   └── Dockerfile
├── imbank-employee-service/
│   └── Dockerfile
└── imbank-department-service/
    └── Dockerfile
```

## Quick Reference Commands

```bash
# Build all images
docker-compose build

# Build single service
docker-compose build config-server

# Start all services
docker-compose up

# Start in background (detached)
docker-compose up -d

# View logs
docker-compose logs -f

# View logs for one service
docker-compose logs -f config-server

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# List running containers
docker ps

# Execute command in container
docker exec -it imbank-config-server sh

# View image details
docker inspect imbank-config-server:latest

# Remove all imbank images
docker images | grep imbank | awk '{print $1":"$2}' | xargs docker rmi
```

## Questions to Explore Tomorrow

1. **Docker:**
   - How does multi-stage build save space?
   - What are Docker layers and how does caching work?
   - How does container networking work?
   - What's the difference between CMD and ENTRYPOINT?

2. **Jenkins:**
   - How does Jenkins detect code changes?
   - What is a Jenkins pipeline?
   - How does Jenkins build Docker images?
   - How does Jenkins deploy to AWS?

3. **AWS:**
   - What is ECR vs Docker Hub?
   - What is ECS (Elastic Container Service)?
   - How does service discovery work in ECS?
   - What is a load balancer and why do we need it?

## Important Notes

- **RDS Database**: Your MySQL database is already on AWS RDS, so containers will connect to it
- **Costs**: Running on AWS ECS Fargate will cost ~$50-70/month (can reduce with EC2)
- **Security**: All containers run as non-root user for security
- **Health Checks**: Each service has health check endpoint at `/actuator/health`

---

Great work today! Get some rest and we'll dive deeper into how everything works tomorrow. 🚀

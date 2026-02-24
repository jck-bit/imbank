# Multi-stage build for Config Server
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /build

# Copy parent pom
COPY pom.xml ./pom.xml

# Copy service pom and source
COPY imbank-config-server/pom.xml ./imbank-config-server/pom.xml

# Download dependencies
RUN mvn -f imbank-config-server/pom.xml dependency:go-offline -B || true

# Copy source code
COPY imbank-config-server/src ./imbank-config-server/src

# Build
RUN mvn -f imbank-config-server/pom.xml clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /build/imbank-config-server/target/*.jar app.jar

EXPOSE 8888

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8888/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

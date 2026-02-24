FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /build

# Copy and install parent pom first
COPY pom.xml ./pom.xml
RUN mvn -N install

# Copy and build employee-service (dependency)
COPY imbank-employee-service/pom.xml ./imbank-employee-service/pom.xml
COPY imbank-employee-service/src ./imbank-employee-service/src
RUN mvn -f imbank-employee-service/pom.xml clean install -DskipTests

# Now build department-service
COPY imbank-department-service/pom.xml ./imbank-department-service/pom.xml
COPY imbank-department-service/src ./imbank-department-service/src
RUN mvn -f imbank-department-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /build/imbank-department-service/target/*.jar app.jar
EXPOSE 8083
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /build
COPY pom.xml ./pom.xml
COPY imbank-api-gateway/pom.xml ./imbank-api-gateway/pom.xml
RUN mvn -f imbank-api-gateway/pom.xml dependency:go-offline -B || true
COPY imbank-api-gateway/src ./imbank-api-gateway/src
RUN mvn -f imbank-api-gateway/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /build/imbank-api-gateway/target/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]

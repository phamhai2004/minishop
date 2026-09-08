# =========================
# Stage 1: Build application
# =========================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw clean package -DskipTests


# =========================
# Stage 2: Run application
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN addgroup --system spring \
    && adduser --system spring --ingroup spring

COPY --from=builder /app/target/*.jar app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
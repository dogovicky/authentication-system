FROM eclipse-temurin:21-jdk-alpine AS builder
LABEL authors="vicky"
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src/ src/
RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar authsystem.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "authsystem.jar"]
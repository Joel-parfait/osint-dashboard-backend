# Phase de build
FROM maven:3.8.4-openjdk-17-slim AS build
COPY . .
RUN mvn clean package -DskipTests

# Phase d'exécution
FROM openjdk:17-jdk-slim
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
# Use a Maven build image to compile the app
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Use a lightweight image to run the app
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Use port from your application.properties (8082)
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]

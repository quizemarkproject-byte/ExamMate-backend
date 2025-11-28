# Build Application
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app
COPY . .
RUN mvn package -DskipTests

# Runtime image (Java 21)
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

CMD ["java", "--enable-preview", "-jar", "app.jar"]

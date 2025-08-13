# Build Application
FROM maven:3.9.6 AS build

WORKDIR /app

COPY . .

RUN mvn package -DskipTests

FROM openjdk:21-bullseye

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar


CMD ["java", "--enable-preview", "-jar", "app.jar"]
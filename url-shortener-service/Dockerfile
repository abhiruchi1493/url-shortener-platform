FROM gradle:8.9-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :url-shortener-service:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/url-shortener-service/build/libs/app.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

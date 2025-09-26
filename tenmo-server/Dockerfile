# ---- Build stage: compile with Maven ----
FROM maven:3.9.7-eclipse-temurin-17 AS build
WORKDIR /app
# copy only pom first to leverage Docker layer cache
COPY pom.xml .
# pre-fetch dependencies (optional but speeds up builds)
RUN mvn -q -DskipTests dependency:go-offline
# now copy source
COPY src ./src
# build jar
RUN mvn -q -DskipTests package

# ---- Run stage: lightweight JRE image ----
FROM eclipse-temurin:17-jre
WORKDIR /app
# Render passes PORT; Spring picks it via server.port=${PORT:8080}
ENV PORT=8080
EXPOSE 8080
# copy the fat jar
COPY --from=build /app/target/*.jar app.jar
# start
ENTRYPOINT ["java","-jar","/app/app.jar"]
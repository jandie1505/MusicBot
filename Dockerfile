# ---------- MUSIC BOT DOCKERFILE ----------

# ----- 1. Build Stage -----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn clean compile package

# ----- 2. Runtime Stage -----
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/MusicBot-1.0.jar /app/app.jar

ENV JAVA_OPTS=""
VOLUME ["/data"]

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

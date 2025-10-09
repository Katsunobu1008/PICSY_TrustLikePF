# --- build stage ---
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src ./src
# package フェーズで spring-boot:repackage も走る
RUN mvn -B -DskipTests package

# --- run stage ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# pom.xml の <finalName>app</finalName> に合わせる
COPY --from=build /workspace/target/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]

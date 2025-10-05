# Java 21の公式イメージをベースにする
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# まず依存関係だけをダウンロードする（キャッシュ効率化のため）
RUN ./mvnw dependency:go-offline
COPY src ./src
# アプリケーションをビルド・パッケージ化する
RUN ./mvnw package -DskipTests

# 実行ステージ
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=0 /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

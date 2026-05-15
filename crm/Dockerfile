# crm/Dockerfile

# --- Стадия сборки ---
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml .
COPY domain/pom.xml domain/
COPY core/pom.xml core/
COPY mobile-api/pom.xml mobile-api/
COPY crm/pom.xml crm/

COPY domain/src domain/src
COPY core/src core/src
COPY crm/src crm/src

RUN mvn install -pl crm -am -DskipTests -q

# --- Стадия запуска ---
FROM bellsoft/hardened-liberica-runtime-container:jre-21-glibc AS runner

WORKDIR /app
COPY --from=build /build/crm/target/crm-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
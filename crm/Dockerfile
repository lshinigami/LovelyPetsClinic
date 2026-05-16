# crm/Dockerfile

# --- Стадия сборки ---
FROM maven:3.9-eclipse-temurin-21 AS build

RUN apt-get update && apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_24.x | bash - && \
    apt-get install -y nodejs && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /build

COPY pom.xml .
COPY domain/pom.xml domain/
COPY core/pom.xml core/
COPY mobile-api/pom.xml mobile-api/
COPY crm/pom.xml crm/

COPY domain/src domain/src
COPY core/src core/src
COPY crm/src crm/src

RUN mvn install -pl crm -am \
    -Dmaven.test.skip=true \
    -Dvaadin.productionMode=true \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -q

# --- Стадия запуска ---
FROM bellsoft/hardened-liberica-runtime-container:jre-21-glibc AS runner

WORKDIR /app
COPY --from=build /build/crm/target/crm-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
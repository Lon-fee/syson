# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Configure Maven with Aliyun mirror for better network access in China
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>\n\
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"\n\
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"\n\
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd">\n\
  <mirrors>\n\
    <mirror>\n\
      <id>aliyun-public</id>\n\
      <mirrorOf>central</mirrorOf>\n\
      <name>Aliyun Public Maven Mirror</name>\n\
      <url>https://maven.aliyun.com/repository/public</url>\n\
    </mirror>\n\
  </mirrors>\n\
  <profiles>\n\
    <profile>\n\
      <id>eclipse</id>\n\
      <repositories>\n\
        <repository>\n\
          <id>eclipse-releases</id>\n\
          <url>https://repo.eclipse.org/content/groups/releases/</url>\n\
        </repository>\n\
      </repositories>\n\
    </profile>\n\
  </profiles>\n\
  <activeProfiles>\n\
    <activeProfile>eclipse</activeProfile>\n\
  </activeProfiles>\n\
</settings>' > /root/.m2/settings.xml

# Copy the root pom.xml first
COPY pom.xml .

# Copy all backend module pom.xml files
COPY backend/application/pom.xml backend/application/
COPY backend/application/syson-application/pom.xml backend/application/syson-application/
COPY backend/metamodel/pom.xml backend/metamodel/
COPY backend/releng/pom.xml backend/releng/
COPY backend/services/pom.xml backend/services/
COPY backend/services/syson-ai-services/pom.xml backend/services/syson-ai-services/
COPY backend/services/syson-direct-edit-grammar/pom.xml backend/services/syson-direct-edit-grammar/
COPY backend/services/syson-diagram-services/pom.xml backend/services/syson-diagram-services/
COPY backend/services/syson-form-services/pom.xml backend/services/syson-form-services/
COPY backend/services/syson-model-services/pom.xml backend/services/syson-model-services/
COPY backend/services/syson-representation-services/pom.xml backend/services/syson-representation-services/
COPY backend/services/syson-services/pom.xml backend/services/syson-services/
COPY backend/services/syson-table-services/pom.xml backend/services/syson-table-services/
COPY backend/services/syson-tree-services/pom.xml backend/services/syson-tree-services/
COPY backend/services/syson-sysml-metamodel-services/pom.xml backend/services/syson-sysml-metamodel-services/
COPY backend/services/syson-sysml-rest-api-services/pom.xml backend/services/syson-sysml-rest-api-services/
COPY backend/tests/pom.xml backend/tests/
COPY backend/views/pom.xml backend/views/

# Copy all source code
COPY backend backend

# Build the application with retry and offline resilience
# Skip tests and checkstyle to speed up build
RUN mvn clean package -DskipTests -Dcheckstyle.skip \
    -Dmaven.repo.local=/app/.m2/repository \
    -Dmaven.wagon.http.retryHandler.count=3 \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    -pl backend/application/syson-application -am \
    || mvn clean package -DskipTests -Dcheckstyle.skip \
       -Dmaven.repo.local=/app/.m2/repository \
       -pl backend/application/syson-application -am

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install Node.js (required by some SysON components)
RUN apk add --update-cache --no-cache nodejs npm && rm -rf /var/cache/apk/*

# Create user
RUN adduser --disabled-password syson

# Copy JAR from build stage
COPY --from=build /app/backend/application/syson-application/target/syson-application-*.jar /app/syson-application.jar

EXPOSE 8080

USER syson

ENTRYPOINT ["java", "-jar", "/app/syson-application.jar"]

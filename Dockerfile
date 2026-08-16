# Single-stage build
FROM maven:3.9-eclipse-temurin-24
WORKDIR /app
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp package -DskipTests
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/subscription-monitor-1.0-SNAPSHOT.jar"]

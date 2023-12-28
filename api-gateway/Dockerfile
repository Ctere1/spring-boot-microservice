FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/api-gateway-0.0.1-SNAPSHOT.war /app/api-gateway.war

CMD ["java", "-jar", "api-gateway.war"]

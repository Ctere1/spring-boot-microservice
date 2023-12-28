FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/config-server-0.0.1-SNAPSHOT.war /app/config-server.war

CMD ["java", "-jar", "config-server.war"]

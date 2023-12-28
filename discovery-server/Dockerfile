FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/discovery-server-0.0.1-SNAPSHOT.war /app/discovery-server.war

CMD ["java", "-jar", "discovery-server.war"]

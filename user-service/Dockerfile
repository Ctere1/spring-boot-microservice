FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/user-service-0.0.1-SNAPSHOT.war /app/user-service.war

CMD ["java", "-jar", "user-service.war"]

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/product-service-0.0.1-SNAPSHOT.war /app/product-service.war

CMD ["java", "-jar", "product-service.war"]

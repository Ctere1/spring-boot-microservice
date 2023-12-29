FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/shopping-cart-service-0.0.1-SNAPSHOT.war /app/shopping-cart-service.war

CMD ["java", "-jar", "shopping-cart-service.war"]

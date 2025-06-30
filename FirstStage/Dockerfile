FROM eclipse-temurin:21-jre

COPY src/ /app/src

COPY *.jar /app/

WORKDIR /app

ENTRYPOINT ["java", "-jar", "rabbitmq-app.jar"]

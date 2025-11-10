FROM openjdk:17

WORKDIR /app

COPY target/app.jar /app/app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/pedido-service-*.jar app.jar
EXPOSE 8084 8085
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM openjdk:17-jdk-slim
ARG JAR_FILE=target/labtodo-1.0.0.jar
COPY ${JAR_FILE} app_labtodo.jar
EXPOSE 80
ENTRYPOINT ["java", "-jar", "app_labtodo.jar"]

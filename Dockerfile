FROM openjdk:21
WORKDIR /quizzy-app
COPY target/quizzy-0.0.1-SNAPSHOT.jar quizzy-app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "quizzy-app.jar"]

FROM openjdk:11
VOLUME /tmp
EXPOSE 8080
ARG JAR_FILE=target/executableJar.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
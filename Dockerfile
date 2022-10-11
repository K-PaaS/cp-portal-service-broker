FROM openjdk:8-jdk-alpine
#ENV JAVA_OPTS='-Dspring.config.location=application.yml -Dspring.profiles.active=dev'
#ENV JAVA_OPTS='-Dspring.config.location=application.yml'
ARG JAR_FILE=build/libs/paas-ta-caas-broker.jar
COPY ${JAR_FILE} paas-ta-caas-broker.jar
COPY application.yml /application.yml
ENTRYPOINT ["java","-jar","/paas-ta-caas-broker.jar"]

FROM maven:3.8.2-jdk-11 AS build
COPY src /home/app/src
COPY pom.xml /home/app/pom.xml
RUN mvn -B compile assembly:single --file /home/app/pom.xml

FROM openjdk:11-jre-slim
COPY --from=build /home/app/target/*.jar /usr/local/lib/mock.jar
ENV MOCK_PORT=3000
EXPOSE 3000
ENTRYPOINT ["java","-jar","/usr/local/lib/mock.jar"]

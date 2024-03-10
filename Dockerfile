FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /
COPY ./pom.xml ./
COPY ./src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /
COPY --from=build /target/currency-pair-conversion-1.0-SNAPSHOT-jar-with-dependencies.jar application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]
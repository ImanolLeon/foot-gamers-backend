# Imagen modelo

FROM eclipse-temurin:25.0.4.1_1-jdk
WORKDIR /root
COPY ./pom.xml /root
COPY ./.mvn /root/.mvn
COPY ./mvnw /root

RUN ./mvnw dependency:go-offline
COPY ./src /root/src
RUN ./mvnw clean install -DskipTests

ENTRYPOINT ["java","-jar","/root/target/FootGamers-0.0.1-SNAPSHOT.jar"]


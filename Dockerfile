FROM eclipse-temurin:21-jre
WORKDIR /work/
COPY target/quarkus-app/ /work/
EXPOSE 8081
EXPOSE 9001
CMD ["java", "-jar", "quarkus-run.jar"]
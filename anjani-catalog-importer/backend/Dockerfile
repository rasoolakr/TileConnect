FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/anjani-catalog-importer-1.0.0.jar app.jar
EXPOSE 9292
ENTRYPOINT ["java","-jar","app.jar"]

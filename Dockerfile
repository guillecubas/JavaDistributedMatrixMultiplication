# Dockerfile
FROM openjdk:17
WORKDIR /app
COPY target/hazelcast-matrix-multiplication-1.0-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "app.jar"]

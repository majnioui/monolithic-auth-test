# Step 1: Build the application
FROM maven:3.6.3-openjdk-17-slim as build

WORKDIR /app
COPY . /app
RUN apt-get update && \
    apt-get install -y git && \
    mvn clean package -DskipTests

# Step 2: Run the application with Docker-in-Docker
FROM docker:24.0-dind

# Install Java, Maven, and Git using apk (Alpine package manager)
RUN apk update && \
    apk add openjdk17 maven git

WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
COPY --from=build /app /app

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "dockerd-entrypoint.sh & java -jar /app/app.jar"]

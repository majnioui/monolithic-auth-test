# Step 1: Build the application
FROM maven:3.6.3-openjdk-17 as build
WORKDIR /app
COPY . /app
RUN mvn clean package -DskipTests

# Step 2: Run the application
FROM openjdk:17-oracle
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]

# Use official Java runtime
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy jar file (make sure you build it first)
COPY target/*.jar app.jar

# Expose port (Render uses 10000 by default, but we map dynamically)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","/app/app.jar"]

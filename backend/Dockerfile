FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy full backend source
COPY . .

# Build the project inside container
RUN mvn clean package -DskipTests

# Run the jar
CMD ["java","-jar","target/*.jar"]

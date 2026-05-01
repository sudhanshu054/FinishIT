FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

RUN apk add --no-cache maven

COPY . .

RUN mvn clean package -DskipTests

# Rename jar to fixed name
RUN cp target/*.jar app.jar

CMD ["java","-jar","app.jar"]

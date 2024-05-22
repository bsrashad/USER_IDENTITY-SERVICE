# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim
 
# Set the working directory in the container
WORKDIR /app
 
# Copy the Spring Boot application JAR file to the container
COPY security-0.0.1-SNAPSHOT.jar /app/spring-boot-app.jar
 
# Install Python3, pip, boto3, and AWS CLI
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    pip3 install boto3 && \
    apt-get install -y awscli && \
    apt-get install -y curl
 
# Copy the .env file to the container
COPY .env /app/.env
 
# Run aws configure using environment variables from .env file
RUN export $(cat /app/.env | xargs)
 
# Expose port 4321
EXPOSE 4321
 
# Define the command to run the Spring Boot application when the container starts
CMD ["java", "-jar", "spring-boot-app.jar"]
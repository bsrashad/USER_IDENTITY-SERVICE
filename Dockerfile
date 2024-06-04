FROM openjdk:17-jdk-slim
 
# Set the working directory in the container
WORKDIR /app
 
# Copy the Spring Boot application JAR file to the container
COPY user-creation-0.0.1-SNAPSHOT.jar /app/spring-boot-app.jar
 
# Install Python3, pip, boto3, and AWS CLI
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    pip3 install boto3 && \
    apt-get install -y awscli && \
    apt-get install -y curl
 
# Copy the .env file to the container
COPY .env /app/.env
 
# Set AWS credentials and region using aws configure during the Docker build
RUN aws configure set aws_access_key_id "$(grep AWS_ACCESS_KEY_ID /app/.env | cut -d '=' -f2)" --profile default && \
    aws configure set aws_secret_access_key "$(grep AWS_SECRET_ACCESS_KEY /app/.env | cut -d '=' -f2)" --profile default && \
    aws configure set region "$(grep AWS_REGION /app/.env | cut -d '=' -f2)" --profile default && \
    aws configure set output "$(grep AWS_DEFAULT_OUTPUT /app/.env | cut -d '=' -f2)" --profile default
 
# Expose port 4321
EXPOSE 4325
 
# Define the command to run the Spring Boot application when the container starts
CMD ["java", "-jar", "spring-boot-app.jar"]

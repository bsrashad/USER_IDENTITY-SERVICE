import boto3
import csv
import logging
import sys
from botocore.exceptions import ClientError
 
# Configure logging
logging.basicConfig(level=logging.DEBUG)
 
BUCKET_NAME = 'bc103-lms-user-token'
FILE_NAME = 'tokens.csv'
REGION_NAME = 'eu-west-2'
 
def get_token_from_s3(bucket_name, file_name, user_id):
    s3_client = boto3.client('s3', region_name=REGION_NAME)
    try:
        response = s3_client.get_object(Bucket=bucket_name, Key=file_name)
        logging.debug(f"Response from S3: {response}")
 
        content = response['Body'].read().decode('utf-8').splitlines()
        reader = csv.reader(content)
        for row in reader:
            if row[0] == user_id:
                return row[1]
        # If the user ID does not exist in the CSV file, return '0'
        return '0'
    except ClientError as e:
        logging.error(e)
        return None
 
if __name__ == "__main__":
    logging.debug("Starting Python script...")
 
    if len(sys.argv) != 2:
        logging.error("Usage: python script.py <user_id>")
        sys.exit(1)
 
    user_id = sys.argv[1]
 
    logging.debug(f"Received user ID: {user_id}")
 
    token = get_token_from_s3(BUCKET_NAME, FILE_NAME, user_id)
 
    logging.debug(f"Token for user {user_id}: {token}")
 
    print(token)
    logging.debug("Python script completed.")
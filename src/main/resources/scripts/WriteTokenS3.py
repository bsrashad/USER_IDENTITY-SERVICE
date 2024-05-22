import boto3
from botocore.exceptions import ClientError
import logging
import sys
 
# Configure logging
logging.basicConfig(level=logging.DEBUG)
 
BUCKET_NAME = 'bc103-lms-user-token'
FILE_NAME = 'tokens.csv'
REGION_NAME = 'eu-west-2'
 
def create_bucket_if_not_exists(bucket_name, region_name):
    logging.debug(f"Checking if bucket exists: {bucket_name}")
    s3_client = boto3.client('s3', region_name=region_name)
 
    try:
        s3_client.head_bucket(Bucket=bucket_name)
        logging.debug(f"Bucket {bucket_name} already exists.")
    except ClientError as e:
        if e.response['Error']['Code'] == '404':
            try:
                s3_client.create_bucket(Bucket=bucket_name,
                                        CreateBucketConfiguration={'LocationConstraint': region_name})
                logging.debug(f"Created new bucket: {bucket_name}")
            except ClientError as ce:
                logging.error(f"Error creating bucket: {ce}")
                sys.exit(1)
        else:
            logging.error(f"Error checking bucket existence: {e}")
            sys.exit(1)
 
def append_or_update_token_to_s3(bucket_name, file_name, user_id, token):
    logging.debug(f"Appending token to S3: {token} for user {user_id}")
    s3_client = boto3.client('s3', region_name=REGION_NAME)
    csv_data = f"{user_id},{token}\n"
 
    try:
        create_bucket_if_not_exists(bucket_name, REGION_NAME)
 
        try:
            existing_data = s3_client.get_object(Bucket=bucket_name, Key=file_name)['Body'].read().decode('utf-8')
        except ClientError as ce:
            if ce.response['Error']['Code'] == 'NoSuchKey':
                existing_data = ""
            else:
                logging.error(f"Error getting existing data: {ce}")
                sys.exit(1)
 
        lines = existing_data.split('\n')
        user_exists = any(line.startswith(user_id + ',') for line in lines)
 
        if user_exists:
            logging.debug(f"User {user_id} already exists. Not updating the token.")
        else:
            updated_lines = lines + [csv_data]
            updated_data = '\n'.join(updated_lines).strip()  # Remove trailing empty lines
            s3_client.put_object(Body=updated_data, Bucket=bucket_name, Key=file_name, ContentType='text/csv')
            logging.debug(f"Appended token {token} for new user {user_id}.")
 
    except ClientError as e:
        logging.error(f"Error appending token to S3: {e}")
        sys.exit(1)
 
if __name__ == "__main__":
    logging.debug("Starting Python script...")
 
    if len(sys.argv) != 3:
        logging.error("Usage: python WriteTokenS3.py <user_id> <token>")
        sys.exit(1)
 
    user_id = sys.argv[1]
    token = sys.argv[2]
 
    logging.debug(f"Received user ID: {user_id}")
    logging.debug(f"Received token: {token}")
 
    append_or_update_token_to_s3(BUCKET_NAME, FILE_NAME, user_id, token)
 
    logging.debug("Python script completed.")
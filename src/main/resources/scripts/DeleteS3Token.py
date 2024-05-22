import boto3
from botocore.exceptions import ClientError
import logging
import sys
 
# Configure logging
logging.basicConfig(level=logging.DEBUG)
 
BUCKET_NAME = 'bc103-lms-user-token'
FILE_NAME = 'tokens.csv'
REGION_NAME = 'eu-west-2'
 
def delete_token_from_s3(bucket_name, file_name, user_id):
    logging.debug(f"Deleting token for user {user_id} from S3")
    s3_client = boto3.client('s3', region_name=REGION_NAME)
 
    try:
        try:
            existing_data = s3_client.get_object(Bucket=bucket_name, Key=file_name)['Body'].read().decode('utf-8')
        except ClientError as ce:
            if ce.response['Error']['Code'] == 'NoSuchKey':
                logging.debug(f"No existing file found: {file_name}")
                return
            else:
                logging.error(f"Error getting existing data: {ce}")
                sys.exit(1)
 
        lines = existing_data.split('\n')
        updated_lines = [line for line in lines if line and not line.startswith(user_id + ',')]
 
        if len(updated_lines) == len(lines):
            logging.debug(f"No token found for user {user_id}.")
            return
 
        updated_data = '\n'.join(updated_lines)
        s3_client.put_object(Body=updated_data, Bucket=bucket_name, Key=file_name, ContentType='text/csv')
 
        logging.debug(f"Deleted token for user {user_id}.")
 
    except ClientError as e:
        logging.error(f"Error deleting token from S3: {e}")
        sys.exit(1)
 
if __name__ == "__main__":
    logging.debug("Starting Python script...")
 
    if len(sys.argv) != 2:
        logging.error("Usage: python DeleteTokenS3.py <user_id>")
        sys.exit(1)
 
    user_id = sys.argv[1]
 
    logging.debug(f"Received user ID: {user_id}")
 
    delete_token_from_s3(BUCKET_NAME, FILE_NAME, user_id)
 
    logging.debug("Python script completed.")
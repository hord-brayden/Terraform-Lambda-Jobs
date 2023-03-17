import boto3

# Initialize the S3 and Rekognition clients
s3 = boto3.client('s3')
rekognition = boto3.client('rekognition')

# Set the name of your S3 bucket
bucket_name = 'your-bucket-name'

# Set the name of the folder that contains the images you want to process
source_folder = 'source-images'

# Set the name of the folder where you want to save the results
results_folder = 'results'

# Get a list of all the objects in the source folder
response = s3.list_objects(Bucket=bucket_name, Prefix=source_folder)

# Loop through each object in the folder
for obj in response['Contents']:
    # Get the name of the object (i.e. the image filename)
    image_name = obj['Key'].split('/')[-1]
    
    # Call each of the Rekognition APIs on the image
    response_labels = rekognition.detect_labels(Image={'S3Object': {'Bucket': bucket_name, 'Name': obj['Key']}})
    response_faces = rekognition.detect_faces(Image={'S3Object': {'Bucket': bucket_name, 'Name': obj['Key']}})
    response_text = rekognition.detect_text(Image={'S3Object': {'Bucket': bucket_name, 'Name': obj['Key']}})
    # Add more APIs here as needed
    
    # Save the results to a file in the results folder
    results_file = f"{results_folder}/{image_name}-results.txt"
    with open(results_file, 'w') as f:
        f.write(f"Labels API: {response_labels}\n")
        f.write(f"Faces API: {response_faces}\n")
        f.write(f"Text API: {response_text}\n")
        # Add more API results here as needed
    
    # Upload the results file to the results folder in the same S3 bucket
    s3.upload_file(results_file, bucket_name, results_file)

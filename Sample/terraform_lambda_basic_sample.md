# Terraform Lambda-S3 Example

This Terraform configuration creates a simple AWS Lambda function that creates an S3 bucket and saves the function's return value into that bucket. This example demonstrates the power of Terraform in provisioning AWS resources and wiring them together.

## Prerequisites

Ensure you have the following installed and configured:
- Terraform
- AWS CLI
- An S3 bucket to store Terraform state (optional but recommended for remote state management)

## Terraform Configuration

### 1. Create the directory structure
```sh
mkdir terraform-lambda-s3
cd terraform-lambda-s3
mkdir lambda
```

### 2. Create `main.tf`
```hcl
provider "aws" {
  region = "us-west-2"
}

resource "aws_iam_role" "lambda_role" {
  name = "lambda_role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Sid    = ""
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      },
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_policy_attachment" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_s3_bucket" "lambda_bucket" {
  bucket = "my-lambda-bucket-${random_id.bucket_id.hex}"
  force_destroy = true
}

resource "random_id" "bucket_id" {
  byte_length = 8
}

data "archive_file" "lambda_zip" {
  type        = "zip"
  source_dir  = "${path.module}/lambda"
  output_path = "${path.module}/lambda.zip"
}

resource "aws_lambda_function" "my_lambda" {
  filename         = data.archive_file.lambda_zip.output_path
  function_name    = "MyLambdaFunction"
  role             = aws_iam_role.lambda_role.arn
  handler          = "lambda_function.lambda_handler"
  source_code_hash = filebase64sha256(data.archive_file.lambda_zip.output_path)
  runtime          = "python3.9"
  environment {
    variables = {
      BUCKET_NAME = aws_s3_bucket.lambda_bucket.bucket
    }
  }
}

output "bucket_name" {
  value = aws_s3_bucket.lambda_bucket.bucket
}
```

### 3. Create `lambda/lambda_function.py`
```python
import json
import boto3
import os

def lambda_handler(event, context):
    s3 = boto3.client('s3')
    bucket_name = os.environ['BUCKET_NAME']
    data = {
        'message': 'Hello, World!',
        'event': event
    }
    s3.put_object(Bucket=bucket_name, Key='output.json', Body=json.dumps(data))
    return {
        'statusCode': 200,
        'body': json.dumps(data)
    }
```

### 4. Create `variables.tf` (optional)
```hcl
variable "region" {
  description = "The AWS region to deploy the Lambda function in"
  default     = "us-west-2"
}
```

### 5. Create `outputs.tf` (optional)
```hcl
output "lambda_function_name" {
  value = aws_lambda_function.my_lambda.function_name
}

output "lambda_s3_bucket" {
  value = aws_s3_bucket.lambda_bucket.bucket
}
```

### 6. Initialize and apply the Terraform configuration
```sh
terraform init
terraform apply
```

## Summary

This configuration does the following:
1. Defines an IAM role for the Lambda function with basic execution permissions.
2. Creates a new S3 bucket to store the Lambda function output.
3. Packages a simple Python Lambda function that writes a JSON message to the S3 bucket.
4. Deploys the Lambda function and sets an environment variable for the bucket name.

Make sure to review and modify any specific settings or configurations according to your requirements before applying the Terraform configuration.

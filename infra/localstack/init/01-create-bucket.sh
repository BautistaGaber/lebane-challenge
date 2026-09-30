#!/bin/sh
set -eu

BUCKET_NAME="lebane-images"
REGION="us-east-1"

echo "Initializing S3 bucket: ${BUCKET_NAME}"

if awslocal s3api head-bucket \
    --bucket "${BUCKET_NAME}" 2>/dev/null; then
    echo "Bucket already exists."
else
    awslocal s3api create-bucket \
        --bucket "${BUCKET_NAME}" \
        --region "${REGION}"

    echo "Bucket created successfully."
fi
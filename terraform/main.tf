terraform {
    required_providers {
        aws = {
            source  = "hashicorp/aws"
            version = "5.81.0"
        }
    }
}
provider "aws" {
    region = "us-east-1"
}

resource "aws_s3_bucket" "rehab_ai" {
    bucket = "rehab-ai-bucket"

    tags = {
        Name        = "Rehab.AI Bucket"
        Environment = "Dev"
    }
}

resource "aws_s3_bucket_lifecycle_configuration" "delete-after-30-days" {
    bucket = "rehab-ai-bucket"
    rule {
        id     = "Delete inactive files after 30 days"
        status = "Enabled"


        filter {
            prefix = "inactive/"
        }
        expiration {
            days = 30
        }
    }
}
# Setup
## Install Docker

## Install Python3

## Install LocalStack
```bash
pip install localstack
```

## Start LocalStack
```bash
localstack start
```

## Install [AWS-CLI](https://awscli.amazonaws.com/AWSCLIV2.msi)
## Check version
```bash
aws -version
## output: aws-cli/2.15.32 Python/3.11.8 Windows/10 exe/AMD64 prompt/off
```

## Configure LocalStack

```bash
## You don’t need real AWS credentials. Just run:
aws configure

#AWS Access Key ID:     test
#AWS Secret Access Key: test
#Default region name:   us-east-1
#Default output format: json
```

## Create a secret using the AWS CLI
```bash
aws --endpoint-url=http://localhost:4566 secretsmanager create-secret \
  --name my-db-credentials \
  --secret-string '{"username":"admin","password":"mypassword"}'

##output
#{
#    "ARN": "arn:aws:secretsmanager:us-west-2:000000000000:secret:my-db-credentials-zDYSpz",
#    "Name": "my-db-credentials",
#    "VersionId": "129f6a36-5371-4c1f-9f2c-dfede64d7d82"
#}
```

## Check Secrets were created
```bash
aws --endpoint-url=http://localhost:4566 secretsmanager list-secrets
## output
#{
#    "SecretList": [
#        {
#            "ARN": "arn:aws:secretsmanager:us-west-2:000000000000:secret:my-db-credentials-zDYSpz",
#            "Name": "my-db-credentials",
#            "LastChangedDate": "2025-07-25T21:08:11.538937-03:00",
#            "SecretVersionsToStages": {
#                "129f6a36-5371-4c1f-9f2c-dfede64d7d82": [
#                    "AWSCURRENT"
#                ]
#            },
#            "CreatedDate": "2025-07-25T21:08:11.538937-03:00"
#        }
#   ]
#}
```


## OFF-topic
### Issues to generate markdown file using Gemini
``` 
To get this exact result every time, the key is to ask me to present the file as a "literal raw text file" or a ".txt file". This tells me to wrap the entire content in a way that prevents the Canvas from rendering the markdown.
Here are a few phrases that will work perfectly:
"Please give me the file, but display it as a literal raw text file so I can see all the tags."
"Can you show me the raw markdown source? Don't render it."
"I need to copy the entire file, including the immersive tags. Please present it as a .txt file."
```
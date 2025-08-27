# Guide: Running a Java S3/SQS Application with LocalStack

This guide provides all the necessary steps to set up and run a Java application that interacts with S3 and SQS services locally using LocalStack, without connecting to the real AWS cloud.

---

### ## Prerequisites

Before you begin, ensure you have the following installed:
- **JDK 11+**
- **Maven**
- **Docker and Docker Compose**
- **AWS CLI v2**

---

### ## Step 1: Run LocalStack with Docker 🐳

The easiest way to run LocalStack is with Docker.

1.  Create a file named `docker-compose.yml` with the following content. This configuration tells Docker to start the LocalStack container and enable the S3 and SQS services.

    ```yaml
    version: "3.8"

    services:
      localstack:
        container_name: "localstack-main"
        image: localstack/localstack:latest
        ports:
          - "127.0.0.1:4566:4566"            # LocalStack Gateway
          - "127.0.0.1:4510-4559:4510-4559"  # External services
        environment:
          - SERVICES=s3,sqs
          - DEBUG=0
          - DOCKER_HOST=unix:///var/run/docker.sock
        volumes:
          - "${LOCALSTACK_VOLUME_DIR:-./volume}:/var/lib/localstack"
          - "/var/run/docker.sock:/var/run/docker.sock"
    ```

2.  Open your terminal in the same directory and run the following command to start the container in the background:
    ```bash
    docker-compose up -d
    ```

---

### ## Step 2: Create Local AWS Resources ⚙️

You must use the standard `aws` CLI with the `--endpoint-url` flag to direct commands to your local container instead of the real AWS.

1.  **Create the S3 bucket:**
    ```bash
    aws s3 mb s3://my-local-bucket --endpoint-url http://localhost:4566
    ```

2.  **Create the SQS queue:**
    ```bash
    aws sqs create-queue --queue-name my-local-queue --endpoint-url http://localhost:4566
    ```

3.  **Get the Queue URL:** Run the `list-queues` command to get the exact URL for your queue. **You will need this for the Java code.**
    ```bash
    aws sqs list-queues --endpoint-url http://localhost:4566
    ```
    The output will look like this. Copy the `QueueUrl` value.
    ```json
    {
        "QueueUrls": [
            "http://localhost:4566/000000000000/my-local-queue"
        ]
    }
    ```

---

### ## Step 3: The Java Application 🧑‍💻
#### **`pom.xml` (Maven Dependencies)**
#### **`src/main/java/com/example/LocalStackS3SqsDemo.java`**

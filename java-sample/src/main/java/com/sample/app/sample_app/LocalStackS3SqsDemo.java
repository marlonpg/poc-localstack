package com.sample.app.sample_app;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class LocalStackS3SqsDemo {

    // Common configuration for LocalStack
    private static final String LOCALSTACK_ENDPOINT = "http://localhost:4566";
    private static final Region AWS_REGION = Region.US_EAST_1;

    // Dummy credentials for LocalStack
    private static final StaticCredentialsProvider DUMMY_CREDENTIALS =
            StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test"));

    // Resource names
    private static final String BUCKET_NAME = "my-local-bucket";
    // IMPORTANT: Replace with the Queue URL from Step 2
    private static final String QUEUE_URL = "http://sqs.us-west-2.localhost.localstack.cloud:4566/000000000000/my-local-queue";


    public static void main(String[] args) throws InterruptedException {
        // 1. Create clients configured for LocalStack
        S3Client s3 = buildS3Client();
        SqsClient sqs = buildSqsClient();

        System.out.println("✅ AWS clients created successfully.");

        // --- PRODUCER ---
        // 2. Upload a file to the S3 bucket
        String fileKey = "my-test-file.txt";
        String fileContent = "Hello from LocalStack S3!";

        s3.putObject(PutObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(fileKey)
                        .build(),
                RequestBody.fromString(fileContent));

        System.out.println("⬆️  File uploaded to S3 bucket '" + BUCKET_NAME + "' with key '" + fileKey + "'.");

        // 3. Send a message to the SQS queue with file details
        String messageBody = "{\"bucketName\":\"" + BUCKET_NAME + "\", \"fileKey\":\"" + fileKey + "\"}";
        sqs.sendMessage(SendMessageRequest.builder()
                .queueUrl(QUEUE_URL)
                .messageBody(messageBody)
                .build());

        System.out.println("✉️  Message sent to SQS queue.");
        System.out.println("------------------------------------");

        // --- CONSUMER ---
        // 4. Poll the SQS queue for messages
        System.out.println("⬇️  Polling for messages...");
        Thread.sleep(2000); // Give a moment for the message to be available

        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(QUEUE_URL)
                .maxNumberOfMessages(1)
                .waitTimeSeconds(10) // Use long polling
                .build();

        List<Message> messages = sqs.receiveMessage(receiveRequest).messages();

        if (messages.isEmpty()) {
            System.out.println("📭 No messages found in the queue.");
        } else {
            // 5. Process the received message
            Message message = messages.get(0);
            System.out.println("📬 Message received with body: " + message.body());

            // Simple parsing (in a real app, use a JSON library like Jackson)
            String receivedBucket = message.body().split("\"")[3];
            String receivedKey = message.body().split("\"")[7];

            // 6. Download the corresponding file from S3
            ResponseInputStream<GetObjectResponse> s3Object = s3.getObject(GetObjectRequest.builder()
                    .bucket(receivedBucket)
                    .key(receivedKey)
                    .build());

            // 7. Read and print the file content
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object, StandardCharsets.UTF_8))) {
                String content = reader.lines().collect(Collectors.joining("\n"));
                System.out.println("📄 File content from S3: '" + content + "'");
            } catch (IOException e) {
                System.err.println("Error reading file from S3: " + e.getMessage());
            }

            // 8. CRITICAL: Delete the message from the queue after processing
            sqs.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(QUEUE_URL)
                    .receiptHandle(message.receiptHandle())
                    .build());
            System.out.println("🗑️  Message deleted from the queue.");
        }

        System.out.println("🚀 Demo finished.");
    }

    /**
     * Builds an S3Client configured to connect to LocalStack.
     */
    private static S3Client buildS3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(LOCALSTACK_ENDPOINT))
                .credentialsProvider(DUMMY_CREDENTIALS)
                .region(AWS_REGION)
                .forcePathStyle(true) // <-- ADD THIS LINE
                .build();
    }

    /**
     * Builds an SqsClient configured to connect to LocalStack.
     */
    private static SqsClient buildSqsClient() {
        return SqsClient.builder()
                .endpointOverride(URI.create(LOCALSTACK_ENDPOINT))
                .credentialsProvider(DUMMY_CREDENTIALS)
                .region(AWS_REGION)
                .build();
    }
}
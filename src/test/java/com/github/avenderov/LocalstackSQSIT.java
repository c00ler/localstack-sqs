package com.github.avenderov;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageResult;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.LogMessageWaitStrategy;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alexey Venderov
 */
public class LocalstackSQSIT {

    private static final Logger LOG = LoggerFactory.getLogger(LocalstackSQSIT.class);

    private static final int LOCALSTACK_SQS_PORT = 4576;

    private static final String REGION = Regions.EU_CENTRAL_1.getName();

    @ClassRule
    public static GenericContainer localstack =
            new GenericContainer("atlassianlabs/localstack:0.6.0")
                    .withEnv("SERVICES", "sqs")
                    .withEnv("DEFAULT_REGION", REGION)
                    .withExposedPorts(LOCALSTACK_SQS_PORT)
                    .waitingFor((new LogMessageWaitStrategy()
                            .withRegEx(".*Ready\\.\n").withStartupTimeout(Duration.ofSeconds(10L))));

    private AmazonSQSAsync amazonSQSAsync;

    private String queueUrl;

    @Before
    public void beforeEach() {
        final String serviceEndpoint = String.format("http://%s:%d",
                localstack.getContainerIpAddress(), localstack.getMappedPort(LOCALSTACK_SQS_PORT));

        amazonSQSAsync = AmazonSQSAsyncClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials("LocalStackDummyAccessKey", "LocalStackDummySecretKey")))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, REGION))
                .build();

        final CreateQueueResult createQueueResult = amazonSQSAsync.createQueue(
                new CreateQueueRequest().withQueueName(String.format("queue-%s", UUID.randomUUID().toString())));

        queueUrl = createQueueResult.getQueueUrl();

        LOG.info("Test queue created: {}", queueUrl);
    }

    @Test
    public void shouldSendAndReceive() {
        final String messageBody = "{\"message\":\"foo\"}";

        final SendMessageResult sendMessageResult = amazonSQSAsync.sendMessage(queueUrl, messageBody);
        final String messageId = sendMessageResult.getMessageId();

        assertThat(messageId).isNotBlank();

        final ReceiveMessageResult receiveMessageResult =
                amazonSQSAsync.receiveMessage(
                        new ReceiveMessageRequest().withQueueUrl(queueUrl).withMaxNumberOfMessages(10)
                                .withWaitTimeSeconds(1));

        assertThat(receiveMessageResult.getMessages()).hasSize(1);
        assertThat(receiveMessageResult.getMessages().get(0))
                .hasFieldOrPropertyWithValue("messageId", messageId)
                .hasFieldOrPropertyWithValue("body", messageBody);
    }

    @Test
    public void shouldPurge() {
        final Set<String> messageIds = Stream.of("foo", "bar", "test")
                .map(b -> amazonSQSAsync.sendMessage(queueUrl, b).getMessageId())
                .collect(Collectors.toSet());

        assertThat(messageIds).filteredOn(StringUtils::isNotBlank).hasSize(3);

        amazonSQSAsync.purgeQueue(new PurgeQueueRequest().withQueueUrl(queueUrl));

        final ReceiveMessageResult receiveMessageResult =
                amazonSQSAsync.receiveMessage(
                        new ReceiveMessageRequest().withQueueUrl(queueUrl).withMaxNumberOfMessages(10)
                                .withWaitTimeSeconds(1));

        assertThat(receiveMessageResult.getMessages()).isNullOrEmpty();
    }

}

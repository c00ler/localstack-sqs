package com.github.avenderov;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageResult;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalstackSQSIT {

    private AmazonSQSAsyncClient asyncClient;

    private String queueUrl;

    @Before
    public void beforeEach() {
        final String port =
                Optional.ofNullable(System.getProperty("sqs.server.port")).filter(p -> !p.isEmpty()).orElse("4576");

        asyncClient = new AmazonSQSAsyncClient(new BasicAWSCredentials("AWS_ACCESS_KEY_ID", "AWS_SECRET_ACCESS_KEY"));
        asyncClient.setEndpoint(String.format("http://localhost:%s", port));

        final CreateQueueResult createQueueResult = asyncClient.createQueue(
                new CreateQueueRequest().withQueueName(String.format("queue-%s", UUID.randomUUID().toString())));

        queueUrl = createQueueResult.getQueueUrl();
    }

    @Test
    public void shouldSendAndReceive() {
        final String messageBody = "{\"message\":\"foo\"}";

        final SendMessageResult sendMessageResult = asyncClient.sendMessage(queueUrl, messageBody);
        final String messageId = sendMessageResult.getMessageId();

        assertThat(messageId).isNotBlank();

        final ReceiveMessageResult receiveMessageResult = asyncClient.receiveMessage(
                new ReceiveMessageRequest().withQueueUrl(queueUrl).withMaxNumberOfMessages(10).withWaitTimeSeconds(1));

        assertThat(receiveMessageResult.getMessages()).hasSize(1);
        assertThat(receiveMessageResult.getMessages().get(0))
                .hasFieldOrPropertyWithValue("messageId", messageId)
                .hasFieldOrPropertyWithValue("body", messageBody);
    }

    @Test
    public void shouldPurge() {
        final Set<String> messageIds = Stream.of("foo", "bar", "test")
                                             .map(b -> asyncClient.sendMessage(queueUrl, b).getMessageId())
                                             .collect(Collectors.toSet());

        assertThat(messageIds.stream().filter(m -> m != null && !m.isEmpty()).count()).isEqualTo(3);

        asyncClient.purgeQueue(new PurgeQueueRequest().withQueueUrl(queueUrl));

        final ReceiveMessageResult receiveMessageResult = asyncClient.receiveMessage(
                new ReceiveMessageRequest().withQueueUrl(queueUrl).withMaxNumberOfMessages(10).withWaitTimeSeconds(1));

        assertThat(receiveMessageResult.getMessages()).isNullOrEmpty();
    }

}

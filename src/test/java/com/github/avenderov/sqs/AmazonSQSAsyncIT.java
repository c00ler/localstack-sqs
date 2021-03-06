package com.github.avenderov.sqs;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.github.avenderov.support.AbstractIntegrationTest;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alexey Venderov
 */
public class AmazonSQSAsyncIT extends AbstractIntegrationTest {

    @Autowired
    private AmazonSQSAsync amazonSQSAsync;

    @Test
    public void shouldPurgeQueue() {
        final String queue = UUID.randomUUID().toString();
        final CreateQueueResult createQueueResult = amazonSQSAsync.createQueue(queue);
        final String queueUrl = createQueueResult.getQueueUrl();

        final Set<String> messageIds = IntStream.range(0, 3)
                .boxed()
                .map(i -> String.format("message-%d", i))
                .map(m -> amazonSQSAsync.sendMessage(queueUrl, m))
                .map(SendMessageResult::getMessageId)
                .collect(Collectors.toSet());

        assertThat(messageIds).filteredOn(StringUtils::isNotBlank).hasSize(3);

        amazonSQSAsync.purgeQueue(new PurgeQueueRequest().withQueueUrl(queueUrl));

        final ReceiveMessageResult receiveMessageResult = amazonSQSAsync.receiveMessage(
                new ReceiveMessageRequest().withQueueUrl(queueUrl).withMaxNumberOfMessages(10).withWaitTimeSeconds(1));

        assertThat(receiveMessageResult.getMessages()).isNullOrEmpty();
    }

}

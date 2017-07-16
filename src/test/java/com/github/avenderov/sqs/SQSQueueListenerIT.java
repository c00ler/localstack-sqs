package com.github.avenderov.sqs;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.github.avenderov.configuration.AmazonSQSProperties;
import com.github.avenderov.support.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Alexey Venderov
 */
public class SQSQueueListenerIT extends AbstractIntegrationTest {

    @Autowired
    private AmazonSQSAsync amazonSQSAsync;

    @Autowired
    private AmazonSQSProperties amazonSQSProperties;

    @Autowired
    private SQSQueueListener sqsQueueListener;

    @Test
    public void shouldReceiveMessage() throws Exception {
        final String message = "{\"message\":\"test\"}";

        final SendMessageResult sendMessageResult =
                amazonSQSAsync.sendMessage(amazonSQSProperties.getMessagesQueue(), message);
        final String messageId = sendMessageResult.getMessageId();

        await().atMost(1L, TimeUnit.SECONDS)
                .until(() -> sqsQueueListener.getMessages().get(messageId), equalTo(message));
    }

}

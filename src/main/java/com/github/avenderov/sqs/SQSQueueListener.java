package com.github.avenderov.sqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Alexey Venderov
 */
@Component
public class SQSQueueListener {

    private static final Logger LOG = LoggerFactory.getLogger(SQSQueueListener.class);

    private final Map<String, String> messages = new ConcurrentHashMap<>();

    @SqsListener(value = "${aws.sqs.messages-queue}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void handle(final String message, @Headers final MessageHeaders headers) {
        final String messageId = (String) headers.get("MessageId");

        LOG.info("Message received. MessageId: {}, Message: {}", messageId, message);

        messages.put(messageId, message);
    }

    Map<String, String> getMessages() {
        return messages;
    }

}

package com.github.avenderov.configuration;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;

/**
 * @author Alexey Venderov
 */
@Component
@Validated
@ConfigurationProperties(prefix = "aws.sqs")
public class AmazonSQSProperties {

    @Max(value = 20L)
    private Integer waitTimeSeconds = 20;

    @Max(value = 10L)
    private Integer maxNumberOfMessages = 10;

    @NotBlank
    private String messagesQueue;

    public Integer getWaitTimeSeconds() {
        return waitTimeSeconds;
    }

    public void setWaitTimeSeconds(final Integer waitTimeSeconds) {
        this.waitTimeSeconds = waitTimeSeconds;
    }

    public Integer getMaxNumberOfMessages() {
        return maxNumberOfMessages;
    }

    public void setMaxNumberOfMessages(final Integer maxNumberOfMessages) {
        this.maxNumberOfMessages = maxNumberOfMessages;
    }

    public void setMessagesQueue(final String messagesQueue) {
        this.messagesQueue = messagesQueue;
    }

    public String getMessagesQueue() {
        return messagesQueue;
    }

}

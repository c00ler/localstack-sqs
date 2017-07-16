package com.github.avenderov.configuration;

import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Alexey Venderov
 */
@Configuration
public class SQSConfiguration {

    @Bean
    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(
            final AmazonSQSProperties amazonSQSProperties) {
        final SimpleMessageListenerContainerFactory containerFactory =
                new SimpleMessageListenerContainerFactory();

        containerFactory.setWaitTimeOut(amazonSQSProperties.getWaitTimeSeconds());
        containerFactory.setMaxNumberOfMessages(amazonSQSProperties.getMaxNumberOfMessages());

        return containerFactory;
    }

}

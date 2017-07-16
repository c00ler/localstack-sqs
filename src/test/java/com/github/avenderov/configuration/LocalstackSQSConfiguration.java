package com.github.avenderov.configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.core.region.RegionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Alexey Venderov
 */
@Configuration
public class LocalstackSQSConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(LocalstackSQSConfiguration.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Primary
    @Bean
    public AmazonSQSAsync testAmazonSQSAsync(@Value("${aws.sqs.endpoint}") final String endpoint,
                                             final RegionProvider regionProvider,
                                             final AmazonSQSProperties amazonSQSProperties) {
        final AmazonSQSAsync amazonSQSAsync = AmazonSQSAsyncClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials("LocalStackDummyAccessKey", "LocalStackDummySecretKey")))
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(endpoint, regionProvider.getRegion().getName()))
                .build();

        final CreateQueueResult createQueueResult =
                amazonSQSAsync.createQueue(StringUtils.substringAfterLast(amazonSQSProperties.getMessagesQueue(), "/"));
        LOG.debug("Messages queue successfully created: {}", createQueueResult.getQueueUrl());

        return amazonSQSAsync;
    }

}

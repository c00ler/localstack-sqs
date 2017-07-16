package com.github.avenderov.support;

import com.amazonaws.regions.Regions;
import com.github.avenderov.Launcher;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.LogMessageWaitStrategy;

import java.time.Duration;

/**
 * @author Alexey Venderov
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Launcher.class)
@ContextConfiguration(initializers = AbstractIntegrationTest.Initializer.class)
public abstract class AbstractIntegrationTest {

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

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(final ConfigurableApplicationContext configurableApplicationContext) {
            EnvironmentTestUtils.addEnvironment("testcontainers", configurableApplicationContext.getEnvironment(),
                    "aws.sqs.endpoint=" + String.format("http://%s:%d",
                            localstack.getContainerIpAddress(), localstack.getMappedPort(LOCALSTACK_SQS_PORT)));
        }

    }

}

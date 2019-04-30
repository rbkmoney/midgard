package com.rbkmoney.midgard.base.tests.integration.kafka;

import com.rbkmoney.midgard.base.tests.integration.AbstractIntegrationTest;
import com.rbkmoney.midgard.service.MidgardClearingApplication;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.KafkaContainer;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.boot.test.util.TestPropertyValues.Type.MAP;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = MidgardClearingApplication.class, initializers = AbstractKafkaTest.Initializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractKafkaTest extends AbstractIntegrationTest {

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_NS = "source_ns";

    private static final String CONFLUENT_PLATFORM_VERSION = "5.0.1";

    @ClassRule
    public static KafkaContainer kafka = new KafkaContainer(CONFLUENT_PLATFORM_VERSION).withEmbeddedZookeeper();

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of("spring.kafka.bootstrap-servers=" + kafka.getBootstrapServers(),
                    "spring.kafka.properties.security.protocol=PLAINTEXT",
                    "spring.kafka.consumer.group-id=TestListener",
                    "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                    "spring.kafka.consumer.value-deserializer=com.rbkmoney.midgard.service.load.serde.SinkEventDeserializer",
                    "spring.kafka.consumer.enable-auto-commit=false",
                    "spring.kafka.consumer.auto-offset-reset=earliest",
                    "spring.kafka.consumer.client-id=test",
                    "spring.kafka.listener.type=batch",
                    "spring.kafka.listener.ack-mode=manual",
                    "spring.kafka.listener.concurrency=1",
                    "spring.kafka.listener.poll-timeout=1000",
                    "spring.kafka.listener.no-poll-threshold=5.0",
                    "spring.kafka.listener.log-container-config=true",
                    "spring.kafka.listener.monitor-interval=10s",
                    "spring.kafka.client-id=test",
                    "invoicing.kafka.topic=test-topic")
                    .applyTo(configurableApplicationContext.getEnvironment(), MAP, "testcontainers");
        }
    }
}

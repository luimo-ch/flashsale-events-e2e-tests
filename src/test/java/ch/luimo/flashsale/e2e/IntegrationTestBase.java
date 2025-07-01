package ch.luimo.flashsale.e2e;

import ch.luimo.flashsale.e2e.config.*;
import ch.luimo.flashsale.e2e.eventservice.avro.AvroEventStatus;
import ch.luimo.flashsale.e2e.eventservice.avro.AvroFlashSaleEvent;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.ComposeContainer;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Import({FlashSaleEventsTestProducerConfig.class, GenericTestConsumerConfig.class, KafkaConsumerConfig.class,
        KafkaProducerConfig.class, RedisConfig.class})
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestBase.class);

    private static final int FLASHSALE_EVENTS_API_PORT = 8080;
    private static final int FLASHSALE_EVENT_SERVICE_PORT = 8082;
    private static final int FLASHSALE_PURCHASE_PROCESSOR = 8083;
    private static final int SCHEMA_REGISTRY = 8081;
    private static final int KAFKA_PORT = 9092;

    @Value("${application.kafka-topics.flashsale-events}")
    protected String flashsaleEventsTopic;

    protected static final ComposeContainer composeContainer = new ComposeContainer(
            new File("docker-compose.yml"))
            .withLocalCompose(true)
            .withTailChildContainers(true)
            .withPull(false)
            .withExposedService("schema-registry", SCHEMA_REGISTRY);

    @Autowired
    protected GenericTestConsumerConfig.GenericTestConsumer genericTestConsumer;

    @Autowired
    protected FlashSaleEventsTestProducerConfig.FlashSaleEventsTestProducer flashSaleEventsTestProducer;

    @BeforeAll
    static void startContainers() {
        composeContainer.start();

        createTestKafkaTopic("flashsale.purchase.requests", "localhost:9092", false);
        createTestKafkaTopic("flashsale.events", "localhost:9092", true);
    }

    private static void createTestKafkaTopic(String topicName, String bootstrapServers, boolean logCompaction) {
        try (AdminClient adminClient = AdminClient.create(Map.of("bootstrap.servers", bootstrapServers))) {
            NewTopic topic = new NewTopic(topicName, 1, (short) 1);
            if (logCompaction) {
                topic = topic.configs(Map.of("cleanup.policy", "compact"));
            }
            adminClient.createTopics(List.of(topic)).all().get();
            LOG.info("Created topic {}", topicName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create topic " + topicName, e);
        }
    }

    protected void assertEventPublished(String expectedEventId) {
        genericTestConsumer.subscribe(flashsaleEventsTopic);
        LOG.info("Starting await for event with ID: {}", expectedEventId);
        Awaitility.await()
                .atMost(20, TimeUnit.SECONDS)
                .with().pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    ConsumerRecords<String, String> records = genericTestConsumer.poll(Duration.ofMillis(500));
                    for (var record : records) {
                        LOG.info("Successfully received record: key = {}, value = {}", record.key(), record.value());
                        assertThat(record.key()).isEqualTo(expectedEventId);
                    }
                });
        LOG.info("Await finished for event: {}", expectedEventId);
    }

    protected String getBaseUrl(String service, int port) {
        return "http://localhost:" + composeContainer.getServicePort(service, port);
    }

    protected void printUrls() {
        LOG.info(getBaseUrl("kafka", 9092));
    }

    protected AvroFlashSaleEvent flashSaleEventOf() {
        return AvroFlashSaleEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventName("test event")
                .setStartTime(Instant.now())
                .setDuration(3600)
                .setProductId(UUID.randomUUID().toString())
                .setSellerId(UUID.randomUUID().toString())
                .setStockQuantity(1000)
                .setMaxPerCustomer(10)
                .setEventStatus(AvroEventStatus.STARTED)
                .build();
    }

    @AfterAll
    public static void tearDown() {
        composeContainer.stop();
    }
}

package ch.luimo.flashsale.e2e;

import ch.luimo.flashsale.e2e.config.*;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Import({FlashSaleEventsTestProducerConfig.class, GenericTestConsumerConfig.class, KafkaConsumerConfig.class,
KafkaProducerConfig.class, RedisConfig.class})
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestBase.class);

    @Value("${application.kafka-topics.flashsale-events}")
    protected String flashsaleEventsTopic;

    protected static final PostgreSQLContainer<?> mysqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.4"))
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    private static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>("redis:7.0.12")
            .withExposedPorts(6379);

    public static final String CONFLUENT_PLATFORM_VERSION = "7.4.0";
    private static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("confluentinc/cp-kafka")
            .asCompatibleSubstituteFor("apache/kafka")
            .withTag(CONFLUENT_PLATFORM_VERSION);
    private static final DockerImageName SCHEMA_REGISTRY_IMAGE = DockerImageName.parse("confluentinc/cp-schema-registry")
            .withTag(CONFLUENT_PLATFORM_VERSION);
    private static final Network SHARED_NETWORK = Network.newNetwork();

    private static final ConfluentKafkaContainer KAFKA_CONTAINER = new ConfluentKafkaContainer(KAFKA_IMAGE)
            .withListener("kafka:19092")
            .withNetworkAliases("kafka")
            .withNetwork(SHARED_NETWORK)
            .withReuse(true);

    private static final GenericContainer<?> SCHEMA_REGISTRY = new GenericContainer<>(SCHEMA_REGISTRY_IMAGE)
            .withExposedPorts(8085)
            .withNetworkAliases("schema-registry")
            .withNetwork(SHARED_NETWORK)
            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:19092")
            .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8085")
            .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_SECURITY_PROTOCOL", "PLAINTEXT")
            .waitingFor(Wait.forHttp("/subjects"))
            .withStartupTimeout(Duration.of(120, ChronoUnit.SECONDS));

    private static GenericContainer<?> eventsApi;

    @Autowired
    protected GenericTestConsumerConfig.GenericTestConsumer genericTestConsumer;

    @Autowired
    protected FlashSaleEventsTestProducerConfig.FlashSaleEventsTestProducer flashSaleEventsTestProducer;

    @BeforeAll
    static void startContainers() {
        mysqlContainer.start();
        KAFKA_CONTAINER.start();
        SCHEMA_REGISTRY.start();
        REDIS_CONTAINER.start();

        System.setProperty("spring.datasource.url", mysqlContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", mysqlContainer.getUsername());
        System.setProperty("spring.datasource.password", mysqlContainer.getPassword());

        System.setProperty("KAFKA_BOOTSTRAP_SERVERS", KAFKA_CONTAINER.getBootstrapServers());
        System.setProperty("SCHEMA_REGISTRY_URL", "http://schema-registry:" + SCHEMA_REGISTRY.getFirstMappedPort());

        System.setProperty("spring.data.redis.host", REDIS_CONTAINER.getHost());
        System.setProperty("spring.data.redis.port", REDIS_CONTAINER.getMappedPort(6379).toString());

        LOG.info("Starting Kafka container at {}", KAFKA_CONTAINER.getBootstrapServers());
        LOG.info("Starting MySQL container at {}", mysqlContainer.getJdbcUrl());
        LOG.info("Started Redis container at {}", REDIS_CONTAINER.getHost() + ":" + REDIS_CONTAINER.getMappedPort(6379));

        createTestKafkaTopic("flashsale.purchase.requests", KAFKA_CONTAINER.getBootstrapServers(), false);
        createTestKafkaTopic("flashsale.events", KAFKA_CONTAINER.getBootstrapServers(), true);

        LOG.info("bootstrap servers:{}", KAFKA_CONTAINER.getBootstrapServers());
        String port = KAFKA_CONTAINER.getBootstrapServers().split(":")[1];
        eventsApi = new GenericContainer<>("flashsale/flashsale-events-api:local")
                .withExposedPorts(8081)
                .withNetwork(SHARED_NETWORK)
                .withEnv("KAFKA_BOOTSTRAP_SERVERS", "kafka:" + port)
                .withEnv("SCHEMA_REGISTRY_URL", "http://schema-registry:" + SCHEMA_REGISTRY.getFirstMappedPort())
                .withEnv("REDIS_HOST", REDIS_CONTAINER.getHost())
                .withEnv("REDIS_PORT", REDIS_CONTAINER.getMappedPort(6379).toString());
//                .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200));

        eventsApi.start();
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


    @AfterAll
    public static void tearDown() {
        if (SCHEMA_REGISTRY != null) {
            SCHEMA_REGISTRY.stop();
        }
        if (KAFKA_CONTAINER != null) {
            KAFKA_CONTAINER.stop();
        }
        if (mysqlContainer.isRunning()) {
            mysqlContainer.stop();
        }
        if (REDIS_CONTAINER != null) {
            REDIS_CONTAINER.stop();
        }
    }
}

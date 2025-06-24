package ch.luimo.flashsale.e2e;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;


public class FlashSaleEndToEndIntTest extends IntegrationTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(FlashSaleEndToEndIntTest.class);

    @Test
    public void test() {
        LOG.info("Starting test");

        Awaitility.await()
                .with().pollInterval(Duration.ofSeconds(5))
                .atMost(Duration.ofMinutes(3))
                .untilAsserted(() -> Assertions.fail());

    }
}

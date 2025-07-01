package ch.luimo.flashsale.e2e;

import ch.luimo.flashsale.e2e.eventservice.avro.AvroFlashSaleEvent;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FlashSaleEndToEndIntTest extends IntegrationTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(FlashSaleEndToEndIntTest.class);

    @Test
    public void test() {
        LOG.info("Starting test");
        LOG.info(composeContainer.getServiceHost("schema-registry", 8081));
        LOG.info("port:" + composeContainer.getServicePort("schema-registry", 8081));

        AvroFlashSaleEvent event = flashSaleEventOf();
        flashSaleEventsTestProducer.publishEvent(event);

        assertEventPublished(event.getEventId());
    }
}

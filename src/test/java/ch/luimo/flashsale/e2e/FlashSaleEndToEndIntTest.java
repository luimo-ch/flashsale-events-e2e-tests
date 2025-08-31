package ch.luimo.flashsale.e2e;

import ch.luimo.flashsale.e2e.rest.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class FlashSaleEndToEndIntTest extends IntegrationTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(FlashSaleEndToEndIntTest.class);

    @Test
    public void test() {
        CreateFlashSaleEventRequest request = new CreateFlashSaleEventRequest();
        request.setEventName("Test Flash Sale");
        request.setStartTime(OffsetDateTime.now());
        request.setDuration(3600); // duration in seconds
        request.setProductId(UUID.randomUUID());
        request.setSellerId(UUID.randomUUID());
        request.setStockQuantity(10);
        request.setMaxPerCustomer(2);

        String eventServiceBaseUrl = "http://localhost:" + FLASHSALE_EVENT_SERVICE_PORT;
        String eventsApiBaseUrl = "http://localhost:" + FLASHSALE_EVENTS_API_PORT;
        String createEventUrl = eventServiceBaseUrl + "/api/v1/events";
        String scanStartedEventsUrl = eventServiceBaseUrl + "/scheduled-tasks/scan-started-events";
        String submitPurchaseRequestUrl = eventsApiBaseUrl + "/v1/purchases";

        // create test event
        ResponseEntity<String> createEventResponse = testRestTemplate.postForEntity(createEventUrl, request, String.class);
        assertThat(createEventResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createEventResponse.getBody()).isNotNull();
        String flashSaleEventId = createEventResponse.getBody();
        LOG.info("Test event created with id: {}", createEventResponse.getBody());

        // call scan started events
        ResponseEntity<Void> scanStartedEventsResponse = testRestTemplate.getForEntity(scanStartedEventsUrl, Void.class);
        assertThat(scanStartedEventsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        LOG.info("Will attempt to submit purchase request");
        pollUntilAsserted(() -> {
            // submit purchase request
            FlashsalePurchaseRequestREST purchaseRequestREST = createPurchaseRequestREST(flashSaleEventId);
            ResponseEntity<String> stringResponseEntity = testRestTemplate.postForEntity(submitPurchaseRequestUrl, purchaseRequestREST, String.class);
            LOG.info("Attempting to submit purchase request. Response body: {}", stringResponseEntity.getBody());
            FlashsalePurchaseResponseREST flashsalePurchaseResponseREST = objectMapper.readValue(stringResponseEntity.getBody(), FlashsalePurchaseResponseREST.class);
            LOG.info("Response status: {}", stringResponseEntity.getStatusCode());
            assertTrue(flashsalePurchaseResponseREST.getStatus().equals(PurchaseRequestStatusREST.CONFIRMED));
        });
    }

    private FlashsalePurchaseRequestREST createPurchaseRequestREST(String flashSaleEventId) {
        FlashsalePurchaseRequestREST purchaseRequest = new FlashsalePurchaseRequestREST();
        purchaseRequest.setFlashsaleEventId(flashSaleEventId);
        purchaseRequest.setRequestedAt(OffsetDateTime.now());
        purchaseRequest.setPurchaseRequestId(UUID.randomUUID().toString());
        purchaseRequest.setQuantity(1);
        purchaseRequest.setUserId(UUID.randomUUID().toString());
        purchaseRequest.setItemId(UUID.randomUUID().toString());
        purchaseRequest.setSourceType(SourceType.WEB);
        return purchaseRequest;
    }
}

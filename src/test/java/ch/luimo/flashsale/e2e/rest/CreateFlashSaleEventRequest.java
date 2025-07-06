package ch.luimo.flashsale.e2e.rest;

import java.time.OffsetDateTime;
import java.util.UUID;

public class CreateFlashSaleEventRequest {

    private String eventName;
    private OffsetDateTime startTime;
    private Integer duration;
    private UUID productId;
    private UUID sellerId;
    private Integer stockQuantity;
    private Integer maxPerCustomer;

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public void setSellerId(UUID sellerId) {
        this.sellerId = sellerId;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getMaxPerCustomer() {
        return maxPerCustomer;
    }

    public void setMaxPerCustomer(Integer maxPerCustomer) {
        this.maxPerCustomer = maxPerCustomer;
    }

    @Override
    public String toString() {
        return "CreateFlashSaleEventRequest{" +
                "eventName='" + eventName + '\'' +
                ", startTime=" + startTime +
                ", duration=" + duration +
                ", productId=" + productId +
                ", sellerId=" + sellerId +
                ", stockQuantity=" + stockQuantity +
                ", maxPerCustomer=" + maxPerCustomer +
                '}';
    }
}

package ch.luimo.flashsale.e2e.rest;

import java.time.OffsetDateTime;

public class FlashsalePurchaseRequestREST {
    String purchaseRequestId;
    String flashsaleEventId;
    String userId;
    String itemId;
    Integer quantity;
    OffsetDateTime requestedAt;
    SourceType sourceType;

    public String getPurchaseRequestId() {
        return purchaseRequestId;
    }

    public void setPurchaseRequestId(String purchaseRequestId) {
        this.purchaseRequestId = purchaseRequestId;
    }

    public String getFlashsaleEventId() {
        return flashsaleEventId;
    }

    public void setFlashsaleEventId(String flashsaleEventId) {
        this.flashsaleEventId = flashsaleEventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public OffsetDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(OffsetDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }
}

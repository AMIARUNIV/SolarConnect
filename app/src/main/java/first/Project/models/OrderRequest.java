// first/Project/models/OrderRequest.java
package first.Project.models;

import java.util.List;

public class OrderRequest {
    private Integer clientId;
    private String deliveryAddress;
    private String installationDate;
    private String notes;
    private List<OrderItemRequest> items;

    // Getters and Setters
    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getInstallationDate() { return installationDate; }
    public void setInstallationDate(String installationDate) { this.installationDate = installationDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }

    public static class OrderItemRequest {
        private Integer productId;
        private Integer sellerId;
        private Integer quantity;

        // Getters and Setters
        public Integer getProductId() { return productId; }
        public void setProductId(Integer productId) { this.productId = productId; }

        public Integer getSellerId() { return sellerId; }
        public void setSellerId(Integer sellerId) { this.sellerId = sellerId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
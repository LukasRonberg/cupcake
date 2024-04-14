package app.entities;

public class Order {

    private int orderId;
    private int userId;
    private String orderDate;
    private int orderPrice;
    private boolean status;

    public Order(int orderId, int userId, String orderDate, int orderPrice, boolean status) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderDate = orderDate;
        this.orderPrice = orderPrice;
        this.status = status;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(int orderPrice) {
        this.orderPrice = orderPrice;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }
}

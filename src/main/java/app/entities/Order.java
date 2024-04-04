package app.entities;

public class Order {
    private String email;
    private String name;
    private String mobile;
    private int balance;
    private String topping;
    private String bottom;
    private int quantity;
    private int orderlinePrice;

    public Order(String email, String name, String mobile, int balance, String topping, String bottom, int quantity, int orderlinePrice) {
        this.email = email;
        this.name = name;
        this.mobile = mobile;
        this.balance = balance;
        this.topping = topping;
        this.bottom = bottom;
        this.quantity = quantity;
        this.orderlinePrice = orderlinePrice;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }

    public int getBalance() {
        return balance;
    }

    public String getTopping() {
        return topping;
    }

    public String getBottom() {
        return bottom;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getOrderlinePrice() {
        return orderlinePrice;
    }
}

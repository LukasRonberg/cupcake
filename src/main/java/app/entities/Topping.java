package app.entities;

public class Topping {

    private int toppingId;
    private String topping;
    private int price;
    public Topping(int toppingId, String topping, int price) {
        this.toppingId = toppingId;
        this.topping = topping;
        this.price = price;
    }

    public int getToppingId() {
        return toppingId;
    }

    public String getTopping() {
        return topping;
    }

    public int getPrice() {
        return price;
    }
}

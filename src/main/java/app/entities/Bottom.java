package app.entities;

public class Bottom {

    private int bottomId;
    private String bottom;
    private int price;
    public Bottom(int bottomId, String bottom, int price) {
        this.bottomId = bottomId;
        this.bottom = bottom;
        this.price = price;
    }

    public int getBottomId() {
        return bottomId;
    }

    public String getBottom() {
        return bottom;
    }

    public int getPrice() {
        return price;
    }
}

package app.entities;

public class User {
    private String email;
    private String name;
    private String mobile;
    private int balance;

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

    public User(String email, String name, String mobile, int balance) {
        this.email = email;
        this.name = name;
        this.mobile = mobile;
        this.balance = balance;
    }
}

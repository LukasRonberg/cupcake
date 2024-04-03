package app.entities;

public class User {
    private String email;
    private String name;
    private int mobile;
    private int balance;

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public int getMobile() {
        return mobile;
    }

    public int getBalance() {
        return balance;
    }

    public User(String email, String name, int mobile, int balance) {
        this.email = email;
        this.name = name;
        this.mobile = mobile;
        this.balance = balance;
    }
}

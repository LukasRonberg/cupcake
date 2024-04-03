package app.entities;

public class User
{
    private int userId;
    private String email;
    private String password;
    private boolean isAdmin;
    private String name;
    private String mobile;
    private int balance;

    public User(int userId, String email, String password, boolean isAdmin, String name, String mobile, int balance)
    {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
        this.name = name;
        this.mobile = mobile;
        this.balance = balance;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}

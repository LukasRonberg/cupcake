package app.entities;

public class User
{
    private int user_id;
    private String name;
    private String mail;
    private String password;
    private int balance;
    private int mobile;
    private String is_admin;

    public User(int user_id, String name, String mail, String password) {
        this.user_id = user_id;
        this.name = name;
        this.mail = mail;
        this.password = password;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getName() {
        return name;
    }

    public String getMail() {
        return mail;
    }

    public String getPassword() {
        return password;
    }

    public int getBalance() {
        return balance;
    }

    public int getMobile() {
        return mobile;
    }

    public String getIs_admin() {
        return is_admin;
    }
    @Override
    public String toString()
    {
        return "User{" +
                "user_id=" + user_id +
                ", name='" + name + '\'' +
                ", mail='" + mail + '\'' +
                ", password='" + password + '\'' +
                ", balance= '" + balance + '\''+
                ", mobile= '" + mobile + '\''+
                ", is_admin='" + is_admin + '\'' +
                '}';
    }
}

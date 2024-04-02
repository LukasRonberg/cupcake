package app.persistence;

//import app.entities.User;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminMapper {

    public static void showCustomer(String userName,ConnectionPool connectionPool) throws DatabaseException {
        String sql = "select * from public.\"user\" where user_name=?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, userName);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String user_Id = rs.getString("user_id");
                String user_Name = rs.getString("name");
                int user_Mobile = rs.getInt("mobile");
                int user_Balance = rs.getInt("balance");
            } else {
                throw new DatabaseException("Fejl i login. Prøv igen");
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }

    public static void showCustomerOrders(String userName, ConnectionPool connectionPool) throws DatabaseException {
        String query = "SELECT " +
                "o.order_id, u.user_id, u.email, u.name, u.mobile, u.balance, " +
                "ol.topping_id, t.topping, ol.bottom_id, b.bottom, ol.quantity, ol.price AS orderline_price " +
                "FROM " +
                "public.orders o " +
                "JOIN public.users u ON o.user_id = u.user_id " +
                "JOIN public.orderline ol ON o.order_id = ol.order_id " +
                "JOIN public.topping t ON ol.topping_id = t.topping_id " +
                "JOIN public.bottom b ON ol.bottom_id = b.bottom_id" +
                "WHERE u.name = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setString(1,userName);
            ResultSet resultSet = ps.executeQuery();
            // Process the result set
            while (resultSet.next()) {
                // Retrieve data from each row
                int orderId = resultSet.getInt("order_id");
                int userId = resultSet.getInt("user_id");
                String email = resultSet.getString("email");
                String name = resultSet.getString("name");
                int mobile = resultSet.getInt("mobile");
                int balance = resultSet.getInt("balance");
                int toppingId = resultSet.getInt("topping_id");
                String topping = resultSet.getString("topping");
                int bottomId = resultSet.getInt("bottom_id");
                String bottom = resultSet.getString("bottom");
                int quantity = resultSet.getInt("quantity");
                int orderlinePrice = resultSet.getInt("orderline_price");

                // Print or process the retrieved data as needed
                System.out.println("Order ID: " + orderId);
                System.out.println("User ID: " + userId);
                System.out.println("Email: " + email);
                System.out.println("Name: " + name);
                System.out.println("Mobile: " + mobile);
                System.out.println("Balance: " + balance);
                System.out.println("Topping ID: " + toppingId);
                System.out.println("Topping: " + topping);
                System.out.println("Bottom ID: " + bottomId);
                System.out.println("Bottom: " + bottom);
                System.out.println("Quantity: " + quantity);
                System.out.println("Orderline Price: " + orderlinePrice);
                System.out.println();

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static void showAllOrders(ConnectionPool connectionPool) throws DatabaseException {
        String query = "SELECT " +
                "o.order_id, u.user_id, u.email, u.name, u.mobile, u.balance, " +
                "ol.topping_id, t.topping, ol.bottom_id, b.bottom, ol.quantity, ol.price AS orderline_price " +
                "FROM " +
                "public.orders o " +
                "JOIN public.users u ON o.user_id = u.user_id " +
                "JOIN public.orderline ol ON o.order_id = ol.order_id " +
                "JOIN public.topping t ON ol.topping_id = t.topping_id " +
                "JOIN public.bottom b ON ol.bottom_id = b.bottom_id";
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ) {

            ResultSet resultSet = ps.executeQuery();
            // Process the result set
            while (resultSet.next()) {
                // Retrieve data from each row
                int orderId = resultSet.getInt("order_id");
                int userId = resultSet.getInt("user_id");
                String email = resultSet.getString("email");
                String name = resultSet.getString("name");
                int mobile = resultSet.getInt("mobile");
                int balance = resultSet.getInt("balance");
                int toppingId = resultSet.getInt("topping_id");
                String topping = resultSet.getString("topping");
                int bottomId = resultSet.getInt("bottom_id");
                String bottom = resultSet.getString("bottom");
                int quantity = resultSet.getInt("quantity");
                int orderlinePrice = resultSet.getInt("orderline_price");

                // Print or process the retrieved data as needed
                System.out.println("Order ID: " + orderId);
                System.out.println("User ID: " + userId);
                System.out.println("Email: " + email);
                System.out.println("Name: " + name);
                System.out.println("Mobile: " + mobile);
                System.out.println("Balance: " + balance);
                System.out.println("Topping ID: " + toppingId);
                System.out.println("Topping: " + topping);
                System.out.println("Bottom ID: " + bottomId);
                System.out.println("Bottom: " + bottom);
                System.out.println("Quantity: " + quantity);
                System.out.println("Orderline Price: " + orderlinePrice);
                System.out.println();

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
package app.persistence;

import app.entities.Bottom;
import app.entities.Order;
import app.entities.Topping;
import app.entities.User;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemMapper {


    public static List<Topping> showToppings(ConnectionPool connectionPool) throws DatabaseException {
        List<Topping> toppingList = new ArrayList<>();
        String sql = "select * from topping";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                int topping_id = rs.getInt("topping_id");
                String topping = rs.getString("topping");
                int price = rs.getInt("price");
                toppingList.add(new Topping(topping_id, topping, price));
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl!!!!", e.getMessage());
        }
        return toppingList;
    }

    public static List<Bottom> showBottoms(ConnectionPool connectionPool) throws DatabaseException
    {
        List<Bottom> bottomList = new ArrayList<>();
        String sql = "select * from bottom";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                int bottom_id = rs.getInt("bottom_id");
                String bottom = rs.getString("bottom");
                int price = rs.getInt("price");
                bottomList.add(new Bottom(bottom_id, bottom, price));
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl!!!!", e.getMessage());
        }
        return bottomList;
    }

    public static Bottom getBottomById(int bottomId, ConnectionPool connectionPool) throws DatabaseException {
        Bottom bottom = null;

        String sql = "SELECT * FROM bottom WHERE bottom_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, bottomId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("bottom_id");
                String name = rs.getString("bottom");
                int price = rs.getInt("price");
                bottom = new Bottom(id, name, price);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving bottom with id = " + bottomId, e.getMessage());
        }
        return bottom;
    }

    public static Topping getToppingById(int toppingId, ConnectionPool connectionPool) throws DatabaseException {
        Topping topping = null;

        String sql = "SELECT * FROM topping WHERE topping_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, toppingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("topping_id");
                String name = rs.getString("topping");
                int price = rs.getInt("price");
                topping = new Topping(id, name, price);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving topping with id = " + toppingId, e.getMessage());
        }
        return topping;
    }


    // Denne metode sletter brugerens gamle kurv
    public static void deleteUsersBasket(int userId, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "DELETE FROM basket WHERE user_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                System.out.println("Brugeren har ikke noget i kurven at slette");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl: " + e.getMessage());
        }
    }


    //Denne metode henter brugerens gamle kurv og returnerer orderLines-listen til login-metoden i UserController
    public static List<Order> getBasket(User user, List<Bottom> bottomList, List<Topping> toppingList, ConnectionPool connectionPool) throws DatabaseException {
        List<Order> orderLines = new ArrayList<>();

        String sql = "SELECT * FROM basket WHERE user_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, user.getUserId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int toppingId = rs.getInt("topping_id");
                int bottomId = rs.getInt("bottom_id");
                int orderlinePrice = rs.getInt("orderlineprice");
                int quantity = rs.getInt("quantity");

                String toppingName = "";
                for (Topping topping : toppingList) {
                    if (topping.getToppingId() == toppingId) {
                        toppingName = topping.getTopping();
                    }
                }
                String bottomName = "";
                for (Bottom bottom : bottomList) {
                    if (bottom.getBottomId() == (bottomId)) {
                        bottomName = bottom.getBottom();
                    }
                }

                Order order = new Order(user.getUserId(), user.getEmail(), user.getName(), user.getMobile(), user.getBalance(), toppingName, bottomName, quantity, orderlinePrice);
                orderLines.add(order);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving topping with id = " + e.getMessage());
        }
        return orderLines;
    }

    public static int insertOrder(int userId, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "INSERT INTO orders (user_id) VALUES (?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            ps.setInt(1, userId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw new DatabaseException("Fejl ved insert af order");
            }

            // Retrieve the generated keys
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Assuming the first column is the auto-generated ID
                } else {
                    throw new DatabaseException("No generated keys returned");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public static void payForOrder(int orderId, int toppingId, int bottomId, int quantity,int price, ConnectionPool connectionPool) throws DatabaseException
    {
        String sql = "insert into orderline (order_id, topping_id, bottom_id, quantity, price) values (?,?,?,?,?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setInt(1, orderId);
            ps.setInt(2, toppingId);
            ps.setInt(3, bottomId);
            ps.setInt(4, quantity);
            ps.setInt(5, price);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1)
            {
                throw new DatabaseException("Fejl ved sendelse af betalling");
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e.getMessage());
        }
    }

    // Denne metode indsætter hver enkelt ordreline fra brugerens kurv i tabellen basket
    public static void insertOrderline(int userId, int toppingId, int bottomId, int quantity, int orderlineprice, ConnectionPool connectionPool) throws DatabaseException {

        String sql = "insert into basket (user_id, topping_id, bottom_id, quantity, orderlineprice) values (?,?,?,?,?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setInt(1, userId);
            ps.setInt(2, toppingId);
            ps.setInt(3, bottomId);
            ps.setInt(4, quantity);
            ps.setInt(5, orderlineprice);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1)
            {
                throw new DatabaseException("Fejl ved indsættelse af ordrelinie i tabellen basket");
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e.getMessage());
        }
    }
}

package app.persistence;

import app.entities.Bottom;
import app.entities.Topping;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}

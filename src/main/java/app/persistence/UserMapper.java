package app.persistence;

import app.entities.User;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper
{

    public static User login(String email, String password, ConnectionPool connectionPool) throws DatabaseException
    {
        String sql = "select * from public.\"users\" where email=? and password=?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                int id = rs.getInt("user_id");
                boolean isAdmin = rs.getBoolean("is_admin");
                String name = rs.getString("name");
                String mobile = rs.getString("mobile");
                int balance = rs.getInt("balance");
                return new User(id, email, password, isAdmin, name, mobile, balance);

            } else
            {
                throw new DatabaseException("Fejl i login. Prøv igen");
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }


    public static void createuser(String email, String password, String name, String mobile, ConnectionPool connectionPool) throws DatabaseException
    {
        String sql = "insert into users (email, password, is_admin, name, mobile, balance) values (?,?,?,?,?,?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            int balance = 0;
            ps.setString(1, email);
            ps.setString(2, password);
            ps.setBoolean(3, false);
            ps.setString(4, name);
            ps.setString(5, mobile);
            ps.setInt(6, balance);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1)
            {
                throw new DatabaseException("Fejl ved oprettelse af ny bruger");
            }
        }
        catch (SQLException e)
        {
            String msg = "Der er sket en fejl. Prøv igen";
            if (e.getMessage().startsWith("ERROR: duplicate key value "))
            {
                
                msg = "Din e-email findes allerede. Vælg en andet";

            }
            throw new DatabaseException(msg, e.getMessage());
        }
    }

    public static boolean userexist(String email, ConnectionPool connectionPool) throws DatabaseException
    {
        String sql = "select * from public.\"users\" where email=?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
        catch (SQLException e)
        {
            throw new DatabaseException("DB fejl", e.getMessage());

        }

    }

    public static void updateBalance(int userId, int newBalance, ConnectionPool connectionPool) throws DatabaseException
    {
        String sql = "update users set balance = ? where user_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setInt(1, newBalance);
            ps.setInt(2, userId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1)
            {
                throw new DatabaseException("Fejl i opdatering af en task");
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl i opdatering af en task", e.getMessage());
        }
    }
}
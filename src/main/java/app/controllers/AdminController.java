package app.controllers;

import app.exceptions.DatabaseException;
import app.persistence.AdminMapper;
import app.persistence.ConnectionPool;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class AdminController
{
    public static void addRoutes(Javalin app, ConnectionPool connectionPool)
    {
        app.post("getAllOrders", ctx -> getAllOrders(ctx, connectionPool));
        app.post("showCustomer", ctx -> showCustomer(ctx, connectionPool));
    }

    private static void getAllOrders(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        AdminMapper.showAllOrders(connectionPool);
        ctx.render("???.html");
    }

    private static void showCustomer(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String username = ctx.formParam("username_input");
        AdminMapper.showCustomer(username, connectionPool);
        ctx.render("???.html");
    }

    private static void showCustomerOrders(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String username = ctx.formParam("username_input");
        AdminMapper.showCustomerOrders(username, connectionPool);
        ctx.render("???.html");
    }
}

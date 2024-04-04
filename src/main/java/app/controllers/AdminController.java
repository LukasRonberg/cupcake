package app.controllers;

import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.AdminMapper;
import app.persistence.ConnectionPool;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;

public class AdminController
{

    public static void addRoutes(Javalin app)
    {
        app.get("/adminSite", ctx -> index(ctx));

        app.post("/getAllOrders", ctx -> getAllOrders(ctx, ConnectionPool.getInstance()));
        app.post("/showCustomer", ctx -> showCustomer(ctx, ConnectionPool.getInstance()));
        app.post("/showCustomerOrders", ctx -> showCustomerOrders(ctx, ConnectionPool.getInstance()));

        app.post("/addToBalance", ctx -> addMoneyToCustomerBalance(ctx, ConnectionPool.getInstance()));
    }

    private static void index(Context ctx)
    {
        ctx.render("adminSite.html");
    }

    private static void getAllOrders(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        AdminMapper.showAllOrders(connectionPool);
        ArrayList<Order> customerOrders = AdminMapper.showAllOrders(connectionPool);
        ctx.attribute("customerOrders", customerOrders);

        ctx.render("adminSite.html");
    }

    private static void showCustomer(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String username = ctx.formParam("username_input");
        ArrayList<User> customer = AdminMapper.showCustomer(username, connectionPool);
        ctx.attribute("customer", customer);

        ctx.render("adminSite.html");
    }

    private static void showCustomerOrders(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String username = ctx.formParam("username_input");

        ArrayList<Order> customerOrders = AdminMapper.showCustomerOrders(username, connectionPool);
        ctx.attribute("customerOrders", customerOrders);

        ctx.render("adminSite.html");
    }

    private static void addMoneyToCustomerBalance(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        String customerEmail = ctx.formParam("email");
        String amountToAdd = ctx.formParam("balance_input");

        AdminMapper.addMoneyToCustomerBalance(customerEmail,amountToAdd, connectionPool);


        ctx.render("adminSite.html");
    }
}

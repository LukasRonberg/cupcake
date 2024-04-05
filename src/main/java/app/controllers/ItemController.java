package app.controllers;

import app.entities.Bottom;
import app.entities.Order;
import app.entities.Topping;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.ItemMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;

public class ItemController {
        public static void addRoutes(Javalin app)
        {
            app.get("/", ctx -> {
               showTopping(ctx,ConnectionPool.getInstance());
               showBottom(ctx, ConnectionPool.getInstance());
               ctx.render("index.html");
            });
            app.post("/createorder", ctx -> {
                createOrder(ctx,ConnectionPool.getInstance());
            });
        }

    private static void createOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        User currentUser = ctx.sessionAttribute("currentUser");
        if (currentUser == null) {
            ctx.render("login.html");
            return;
        }

        String email = currentUser.getEmail();
        String name = currentUser.getName();
        String mobile = currentUser.getMobile();
        int balance = currentUser.getBalance();

        int toppingId = Integer.parseInt(ctx.formParam("topping")); // Assumes you have toppingId in form
        int bottomId = Integer.parseInt(ctx.formParam("bund"));   // Assumes you have bottomId in form
        int quantity = Integer.parseInt(ctx.formParam("antal"));

        Topping topping = ItemMapper.getToppingById(toppingId, connectionPool);
        Bottom bottom = ItemMapper.getBottomById(bottomId, connectionPool);
        if (topping == null || bottom == null) {
            throw new DatabaseException("cant find id for bottom or topping");
        }

        int orderlinePrice = calculateOrderLinePrice(topping, bottom, quantity);

        Order order = new Order(email, name, mobile, balance, topping.getTopping(), bottom.getBottom(), quantity, orderlinePrice);

        List<Order> orders = ctx.sessionAttribute("orders");
        if(orders == null){
            orders = new ArrayList<>();
        }

        orders.add(order);

        System.out.println("Successfully added order: " + order);
        ctx.render("index.html");
    }

    private static int calculateOrderLinePrice(Topping topping, Bottom bottom, int quantity) {
        int totalItemPrice = topping.getPrice() + bottom.getPrice();
        return totalItemPrice * quantity;
    }



    public static void showBottom(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        List<Bottom> bottomList = ItemMapper.showBottoms(connectionPool);
        ctx.attribute("bottomList",bottomList);
    }

    public static void showTopping(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        List<Topping> toppingList = ItemMapper.showToppings(connectionPool);
        ctx.attribute("toppingList",toppingList);
    }

}

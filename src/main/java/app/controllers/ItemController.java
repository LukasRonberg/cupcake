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
import java.util.Map;
import java.util.Objects;

public class ItemController {
    static ArrayList<Order> orderLine = new ArrayList<>();
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
            app.get("/showcupcakes", ctx -> {
                ctx.render("checkoutpage.html");
            });

            app.post("/payorder", ctx -> {
                payForOrder(ctx,ConnectionPool.getInstance());
                //ctx.render("checkoutpage.html");
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

        Order order = new Order(currentUser.getUserId(),email, name, mobile, balance, topping.getTopping(), bottom.getBottom(), quantity, orderlinePrice);
        orderLine.add(order);

        ctx.sessionAttribute("orders", orderLine);

        double totalAmount = 0; // Her skal du erstatte calculateTotalAmount med din egen logik til at beregne det samlede beløb
        for(Order orderline: orderLine) {
            totalAmount += orderline.getOrderlinePrice();
        }

        ctx.sessionAttribute("totalAmount", totalAmount); // Send det samlede beløb som en attribut til HTML-skabelonen

        System.out.println("Successfully added order: " + order);


            showTopping(ctx,ConnectionPool.getInstance());
            showBottom(ctx, ConnectionPool.getInstance());
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



    public static void payForOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Order> tempOrderLine = ctx.sessionAttribute("orders");

        int generatedOrderId = ItemMapper.insertOrder(tempOrderLine.get(0).getUserId(),connectionPool);
        List<Topping> toppingList = ItemMapper.showToppings(connectionPool);
        List<Bottom> bottomList = ItemMapper.showBottoms(connectionPool);

        for (Order order:tempOrderLine) {
            int toppingId = 0;
            for (Topping topping: toppingList) {
                if(order.getTopping().equals(topping.getTopping())) toppingId = topping.getToppingId();
            }
            int bottomId = 0;
            for (Bottom bottom: bottomList) {
                if(order.getBottom().equals(bottom.getBottom())) bottomId = bottom.getBottomId();
            }

            ItemMapper.payForOrder(generatedOrderId, toppingId, bottomId, order.getQuantity(), order.getOrderlinePrice(), connectionPool);
            ctx.attribute("message", "Tak for din ordre. Din ordre har fået ordrenummer "+generatedOrderId+". Du hører fra os når din ordre er færdig!");
            ctx.attribute("ordercreated", true);
            ctx.render("index.html");
        }
    }

}

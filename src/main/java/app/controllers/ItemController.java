package app.controllers;

import app.entities.Bottom;
import app.entities.Order;
import app.entities.Topping;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.ItemMapper;
import app.persistence.UserMapper;
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
                showItemsInCheckout(ctx);
                ctx.render("checkoutpage.html");
            });
            app.post("/ordermore", ctx -> {
                showTopping(ctx,ConnectionPool.getInstance());
                showBottom(ctx, ConnectionPool.getInstance());
                ctx.render("index.html");
            });

            app.post("/payorder", ctx -> {
                payForOrder(ctx,ConnectionPool.getInstance());
                //ctx.render("checkoutpage.html");
            });
            app.post("deleteorderline", ctx -> deleteorderline(ctx, ConnectionPool.getInstance()));
        }


   

    private static void showItemsInCheckout(Context ctx){
        ctx.sessionAttribute("orders", orderLine);
    }
    private static void deleteorderline(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        //ArrayList<Order> orderlines = ctx.sessionAttribute("orders");
        int orderId = Integer.parseInt(ctx.formParam("orderId"));

        for (Order order : orderLine) {
            if (order.getOrderId() == orderId) {
                orderLine.remove(order);
                break;
            }
        }

        ctx.sessionAttribute("orders", orderLine);
        User currentUser = ctx.sessionAttribute("currentUser");

        int orderCount = 0;
        int totalAmount = 0;
        for (Order orderline : orderLine) {
            if (orderline.getUserId() == currentUser.getUserId()) {
                totalAmount += orderline.getOrderlinePrice();
                orderCount++;
            }
        }

        ctx.sessionAttribute("orderCount", orderCount);
        ctx.sessionAttribute("totalAmount", totalAmount);


        if (orderLine.isEmpty()) {
            showTopping(ctx, ConnectionPool.getInstance());
            showBottom(ctx, ConnectionPool.getInstance());
            ctx.render("index.html");
        } else {
            ctx.render("checkoutpage.html");
        }
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

        Order order = new Order(currentUser.getUserId(), email, name, mobile, balance, topping.getTopping(), bottom.getBottom(), quantity, orderlinePrice);
        orderLine.add(order);

        ctx.sessionAttribute("orders", orderLine);


        int totalAmount = 0;
        int orderCount = 0;
        for (Order orderline : orderLine) {
            if (order.getUserId() == currentUser.getUserId()) {
                totalAmount += orderline.getOrderlinePrice();
                orderCount++;
            }
        }

        ctx.sessionAttribute("totalAmount", totalAmount); // Sender det samlede beløb som en attribut til HTML-skabelonen
        ctx.sessionAttribute("orderCount", orderCount);

        showTopping(ctx, ConnectionPool.getInstance());
        showBottom(ctx, ConnectionPool.getInstance());
        ctx.render("index.html");

    }

    private static int calculateOrderLinePrice(Topping topping, Bottom bottom, int quantity) {
        int totalItemPrice = topping.getPrice() + bottom.getPrice();
        return totalItemPrice * quantity;
    }

    public static void showBottom(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        List<Bottom> bottomList = ItemMapper.showBottoms(connectionPool);
        ctx.attribute("bottomList", bottomList);
    }

    public static void showTopping(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        List<Topping> toppingList = ItemMapper.showToppings(connectionPool);
        ctx.attribute("toppingList", toppingList);
    }

    public static void payForOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        User currentUser = ctx.sessionAttribute("currentUser");
        int orderprice = ctx.sessionAttribute("totalAmount");

        if (currentUser.getBalance() >= orderprice) {
            ArrayList<Order> tempOrderLine = ctx.sessionAttribute("orders");

            int generatedOrderId = ItemMapper.insertOrder(tempOrderLine.get(0).getUserId(), connectionPool);
            List<Topping> toppingList = ItemMapper.showToppings(connectionPool);
            List<Bottom> bottomList = ItemMapper.showBottoms(connectionPool);

            for (Order order : tempOrderLine) {
                int toppingId = 0;
                for (Topping topping : toppingList) {
                    if (order.getTopping().equals(topping.getTopping())) toppingId = topping.getToppingId();
                }
                int bottomId = 0;
                for (Bottom bottom : bottomList) {
                    if (order.getBottom().equals(bottom.getBottom())) bottomId = bottom.getBottomId();
                }

                ItemMapper.payForOrder(generatedOrderId, toppingId, bottomId, order.getQuantity(), order.getOrderlinePrice(), connectionPool);

            }
            tempOrderLine.clear();
            int newBalance = currentUser.getBalance() - orderprice;
            currentUser.setBalance(newBalance);
            UserMapper.updateBalance(currentUser.getUserId(), newBalance, connectionPool);
            ctx.attribute("message", "Tak for din ordre. Din ordre har fået ordrenummer " + generatedOrderId + ". Du hører fra os når din ordre er parat til afhentning!");
            ctx.attribute("ordercreated", true);
            ctx.attribute("totalAmount", 0);
            showTopping(ctx, ConnectionPool.getInstance());
            showBottom(ctx, ConnectionPool.getInstance());
            ctx.render("index.html");
        } else {
            ctx.attribute("message", "Din saldo lyder på " + currentUser.getBalance() + " kr. hvilket ikke er nok til at betale for ordren! Fjern nogle varer fra din kurv eller indbetal penge på din konto!");
            ctx.attribute("notenoughtmoney", true);
            ctx.render("checkoutpage.html");
        }
    }
}

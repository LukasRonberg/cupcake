package app.controllers;

import app.entities.*;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.ItemMapper;
import app.persistence.UserMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ItemController {
    static ArrayList<Orderline> orderLine = new ArrayList<>();
    static Date today = new Date();
    static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
    static String formattedDate = formatter.format(today);
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
            app.post("/showcupcakes", ctx -> {
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
            app.post("deleteorderline", ctx -> deleteorderline(ctx));
            app.post("/deleteorder", ctx -> deleteorder(ctx, ConnectionPool.getInstance()));

        }

    private static void deleteorderline(Context ctx) throws DatabaseException {
        List<Orderline> orderLines = ctx.sessionAttribute("orders");
        int orderlineId = Integer.parseInt(ctx.formParam("orderlineId"));
        // Her fjerner jeg den ordrelinie som kunden ønsker at fjerne
        for (Orderline order : orderLines) {
            if (order.getOrderId() == orderlineId) {
                orderLines.remove(order);
                break;
            }
        }
        // Her lægger jeg den opdaterede orderline-liste ind i en sessionAtribut ved navn orders
        ctx.sessionAttribute("orders", orderLines);
        // Her udregner jeg hvor mange ordrelinier der er og hvad den samlede pris er for dem
        int orderCount = 0;
        int totalAmount = 0;
        for (Orderline orderline : orderLines) {
            totalAmount += orderline.getOrderlinePrice();
            orderCount++;
        }
        // Her opdaterer jeg orderCount og totalAmount i deres respektive sessionatributter
        ctx.sessionAttribute("orderCount", orderCount);
        ctx.sessionAttribute("totalAmount", totalAmount);

        // Hvis kunden sletter alle sine ordrelinier sender jeg ham tilbage til index.html. Er er stadig
        // ordrelinier smider jeg ham tilbage til checkoutpage.html
        if (orderLines.isEmpty()) {
            showTopping(ctx, ConnectionPool.getInstance());
            showBottom(ctx, ConnectionPool.getInstance());
            ctx.render("index.html");
        } else {
            ctx.render("checkoutpage.html");
        }
    }

    private static void deleteorder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        User currentUser = ctx.sessionAttribute("currentUser");
        int orderId = Integer.parseInt(ctx.formParam("orderId"));
        System.out.println(""+orderId);
        int orderPrice = ItemMapper.getOrderPrice(orderId, connectionPool);
        int newBalance = currentUser.getBalance()+orderPrice;
        ItemMapper.deleteOrder(orderId, connectionPool);
        ItemMapper.deleteOrderlines(orderId, connectionPool);
        UserMapper.updateBalance(currentUser.getUserId(), newBalance, connectionPool);

        List<Order> orderList = ItemMapper.getOrderList(currentUser.getUserId(), connectionPool);
        ctx.sessionAttribute("orderlist", orderList);
        boolean hasOpenOrders = false;
        for (Order order : orderList) {
            if (!order.getStatus()) {
                hasOpenOrders = true;
            }
        }
        ctx.sessionAttribute("openorders", hasOpenOrders);
        currentUser.setBalance(newBalance);
        ctx.sessionAttribute("currentUser", currentUser);
        ctx.attribute("message", "Din ordre, med ordrenummer "+ orderId + " er nu slettet fra vores system og beløbet er tilbageført til deres konto!");
        ctx.attribute("orderdeleted", true);
        System.out.println("orderdeleted = "+ctx.attribute("orderdeleted"));
        ctx.render("showorders.html");
    }

    private static void createOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {

        if (ctx.sessionAttribute("orders") != null) {
            orderLine = ctx.sessionAttribute("orders");
        }

        int toppingId = Integer.parseInt(ctx.formParam("topping")); // Assumes you have toppingId in form
        int bottomId = Integer.parseInt(ctx.formParam("bund"));   // Assumes you have bottomId in form
        int quantity = Integer.parseInt(ctx.formParam("antal"));

        Topping topping = ItemMapper.getToppingById(toppingId, connectionPool);
        Bottom bottom = ItemMapper.getBottomById(bottomId, connectionPool);
        if (topping == null || bottom == null) {
            throw new DatabaseException("cant find id for bottom or topping");
        }

        int orderlinePrice = calculateOrderLinePrice(topping, bottom, quantity);

        Orderline order = new Orderline(topping.getTopping(), bottom.getBottom(), quantity, orderlinePrice);
        orderLine.add(order);

        ctx.sessionAttribute("orders", orderLine);

        int totalAmount = 0;
        int orderCount = 0;
        for (Orderline orderline : orderLine) {
            totalAmount += orderline.getOrderlinePrice();
            orderCount++;
        }

        ctx.sessionAttribute("totalAmount", totalAmount); // Sender det samlede beløb som en attribut til HTML-skabelonen
        ctx.sessionAttribute("orderCount", orderCount); // Sender det samlede antal ordrelinier til HTML-skabelonen

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
        if (currentUser == null) {
            ctx.sessionAttribute("fromcheckout", true);
            ctx.render("login.html");
            return;
        }

        int orderprice = ctx.sessionAttribute("totalAmount");

        if (orderprice != 0) {
            if (currentUser.getBalance() >= orderprice) {
                ArrayList<Orderline> tempOrderLine = ctx.sessionAttribute("orders");
                int generatedOrderId = ItemMapper.insertOrder(currentUser.getUserId(),formattedDate, orderprice, connectionPool);
                List<Order> orderList = ItemMapper.getOrderList(currentUser.getUserId(), connectionPool);
                boolean hasFinishedOrders = false;
                boolean hasOpenOrders = false;
                for (Order order : orderList) {
                    if (order.getStatus()) {
                        hasFinishedOrders = true;
                    } else {
                        hasOpenOrders = true;
                    }
                }
                ctx.sessionAttribute("finishedorders", hasFinishedOrders);
                ctx.sessionAttribute("openorders", hasOpenOrders);
                ctx.sessionAttribute("orderlist", orderList);
                List<Topping> toppingList = ItemMapper.showToppings(connectionPool);
                List<Bottom> bottomList = ItemMapper.showBottoms(connectionPool);

                for (Orderline orderline : tempOrderLine) {
                    int toppingId = 0;
                    for (Topping topping : toppingList) {
                        if (orderline.getTopping().equals(topping.getTopping())) toppingId = topping.getToppingId();
                    }
                    int bottomId = 0;
                    for (Bottom bottom : bottomList) {
                        if (orderline.getBottom().equals(bottom.getBottom())) bottomId = bottom.getBottomId();
                    }

                    ItemMapper.payForOrder(generatedOrderId, toppingId, bottomId, orderline.getQuantity(), orderline.getOrderlinePrice(), connectionPool);

                }
                tempOrderLine.clear();
                ItemMapper.deleteUsersBasket(currentUser.getUserId(), connectionPool);
                int newBalance = currentUser.getBalance() - orderprice;
                currentUser.setBalance(newBalance);
                UserMapper.updateBalance(currentUser.getUserId(), newBalance, connectionPool);
                ctx.attribute("message", "Tak for din ordre. Din ordre har fået ordrenummer " + generatedOrderId + ". Du hører fra os når din ordre er parat til afhentning!");
                ctx.attribute("ordercreated", true);
                ctx.sessionAttribute("totalAmount", 0);
                ctx.sessionAttribute("orderCount", 0);
                showTopping(ctx, ConnectionPool.getInstance());
                showBottom(ctx, ConnectionPool.getInstance());
                ctx.render("index.html");
            } else {
                ctx.attribute("message", "Din saldo lyder på " + currentUser.getBalance() + " kr. hvilket ikke er nok til at betale for ordren! Fjern nogle varer fra din kurv eller indbetal penge på din konto!");
                ctx.attribute("notenoughtmoney", true);
                ctx.render("checkoutpage.html");
            }
        } else {
            ctx.attribute("message", "Der er ingen varer i din kurv at betale for!");
            ctx.attribute("notenoughtmoney", true);
            ctx.render("checkoutpage.html");
        }

    }

}

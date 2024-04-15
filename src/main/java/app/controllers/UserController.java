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

public class UserController {
    public static void addRoutes(Javalin app) {
        app.post("login", ctx -> {
            ItemController.showBottom(ctx, ConnectionPool.getInstance());
            ItemController.showTopping(ctx, ConnectionPool.getInstance());
            login(ctx, ConnectionPool.getInstance());
        });
        app.get("login", ctx -> {
            ItemController.showBottom(ctx, ConnectionPool.getInstance());
            ItemController.showTopping(ctx, ConnectionPool.getInstance());
            ctx.sessionAttribute("fromcheckout", true);
            ctx.render("login.html");
        });
        app.get("loginfromindex", ctx -> {
            ItemController.showBottom(ctx, ConnectionPool.getInstance());
            ItemController.showTopping(ctx, ConnectionPool.getInstance());
            ctx.sessionAttribute("fromcheckout", false);
            ctx.render("login.html");
        });
        app.get("index.html", ctx -> {
            ItemController.showBottom(ctx, ConnectionPool.getInstance());
            ItemController.showTopping(ctx, ConnectionPool.getInstance());
            ctx.render("index.html");
        });
        app.get("showorders.html", ctx -> {
            ctx.render("showorders.html");
        });
        app.get("logout", ctx -> logout(ctx, ConnectionPool.getInstance()));
        app.post("createuser", ctx -> createUser(ctx, ConnectionPool.getInstance()));
        app.get("createuser", ctx -> ctx.render("createuser.html"));
    }

    private static void createUser(Context ctx, ConnectionPool connectionPool) throws DatabaseException {

        // Hent form parametre
        String email = ctx.formParam("email");
        String password1 = ctx.formParam("password1");
        String password2 = ctx.formParam("password2");
        String name = ctx.formParam("name");
        String mobile = ctx.formParam("mobile");

        boolean userexist = UserMapper.userexist(email, connectionPool);
        ctx.attribute("createuser", true);
        ctx.attribute("usercreated", false);

        if (!userexist) {
            if (password1.equals(password2)) {
                try {
                    UserMapper.createuser(email, password1, name, mobile, connectionPool);
                    ctx.attribute("message", "Du er hermed oprettet med brugernavn: " + email +
                            ". Nu kan du logge på.");
                    ctx.attribute("login", true);
                    ctx.render("login.html");
                } catch (DatabaseException e) {
                    ctx.attribute("message", "Dit brugernavn findes allerede. Prøv igen, eller log ind");
                    ctx.attribute("login", true);
                    ctx.render("login.html");
                }
            } else {
                ctx.attribute("message", "Dine to passwords matcher ikke! Prøv igen");
                ctx.attribute("createuser", true);
                ctx.render("createuser.html");
            }
        } else {
            ctx.attribute("message", "Dit brugernavn findes allerede. Prøv igen, eller log ind");
            ctx.attribute("login", true);
            ctx.render("login.html");
        }
    }

    private static void logout(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
            // Her henter jeg currentUser fra sessionattributten så jeg kan kalde på brugerens userid
            User currentUser = ctx.sessionAttribute("currentUser");
            // Her henter jeg brugerens ordrelinier
            ArrayList<Orderline> tempOrderLine = ctx.sessionAttribute("orders");
            // Her henter jeg toppingList og bottomList
            List<Topping> toppingList = ItemMapper.showToppings(connectionPool);
            List<Bottom> bottomList = ItemMapper.showBottoms(connectionPool);

            if(tempOrderLine != null) {
                // Her sletter jeg brugerens gamle kurv i tabellen basket
                ItemMapper.deleteUsersBasket(currentUser.getUserId(), connectionPool);
                for (Orderline order : tempOrderLine) {
                    // Her henter jeg hver enkel ordrelinies toppingid og bottomid
                    int toppingId = 0;
                    for (Topping topping : toppingList) {
                        if (order.getTopping().equals(topping.getTopping())) toppingId = topping.getToppingId();
                    }
                    int bottomId = 0;
                    for (Bottom bottom : bottomList) {
                        if (order.getBottom().equals(bottom.getBottom())) bottomId = bottom.getBottomId();
                    }
                    // Her gemmer jeg hver enkelt ordrelinie i tabellen basket
                    ItemMapper.insertOrderline(currentUser.getUserId(), toppingId, bottomId, order.getQuantity(), order.getOrderlinePrice(), connectionPool);
                }
                // Her sletter jeg tempOrderLine arraylisten
                tempOrderLine.clear();
            }

            // Her sletter jeg alle sessionAttributter
            ctx.req().getSession().invalidate();
            // Her sender jeg brugeren tilbage til forsiden index.html
            ctx.redirect("/");
    }

    public static void login(Context ctx, ConnectionPool connectionPool) {
        // Hent form parametre
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        // Denne attribut bruger jeg til mine javascript-bokse. Boksen skal kun vises hvis den er true
        ctx.attribute("login", false);

        // Check om bruger findes i DB med de angivne username + password
        try {
            User user = UserMapper.login(email, password, connectionPool);
            ctx.sessionAttribute("currentUser", user);
            List<Order> orderList = ItemMapper.getOrderList(user.getUserId(), connectionPool);
            ctx.sessionAttribute("orderlist", orderList);
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
            List<Topping> toppingList = ItemMapper.showToppings(connectionPool);
            List<Bottom> bottomList = ItemMapper.showBottoms(connectionPool);
            ArrayList<Orderline> oldOrderLines = ItemMapper.getBasket(user, bottomList, toppingList, connectionPool);
            ArrayList<Orderline> orderLines = ctx.sessionAttribute("orders");

            if(orderLines != null) {
                for (Orderline oldorderlines : oldOrderLines) {
                    orderLines.add(oldorderlines);
                }
                ctx.sessionAttribute("orders", orderLines);
            } else {
                ctx.sessionAttribute("orders", oldOrderLines);
            }

            // Hvis brugeren er admin, sendes han videre til adminsite.html
            if (user.isAdmin()) {
                ctx.render("adminSite.html");
            } else {
                // Her udregner jeg hvor mange ordrelinier der er og hvad den samlede pris er for dem
                int orderCount = 0;
                int totalAmount = 0;
                ArrayList<Orderline> newOrderLines = ctx.sessionAttribute("orders");
                if (newOrderLines != null) {
                    for (Orderline orderline : newOrderLines) {
                        orderCount++;
                        totalAmount += orderline.getOrderlinePrice();
                        ctx.sessionAttribute("hasorderlines", true);
                    }
                } else {
                    ctx.sessionAttribute("hasorderlines", false);
                }
                // Her opdaterer jeg orderCount og totalAmount i deres respektive sessionatributter
                ctx.sessionAttribute("totalAmount", totalAmount);
                System.out.println(""+ctx.sessionAttribute("totalAmount"));
                ctx.sessionAttribute("orderCount", orderCount);
                // Her sender jeg brugeren tilbage til index.html
                if(ctx.sessionAttribute("fromcheckout") != null) {
                    if(ctx.sessionAttribute("fromcheckout")) {
                        ctx.render("checkoutpage.html");
                    } else {
                        ctx.render("index.html");
                    }

                } else {
                    ctx.render("index.html");
                }
            }
        } catch (DatabaseException e) {
            // Her sætter jeg attributten til true hvilket gør at javascriptet vises
            ctx.attribute("login", true);
            // Hvis nej, sendes brugeren tilbage til login siden med fejl besked
            ctx.attribute("message", "Forkert brugernavn eller password. Prøv igen eller opret brugeren!");
            ctx.render("login.html");
        }
    }
}

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
            ctx.render("login.html");
        });
        app.get("index.html", ctx -> {
            ItemController.showBottom(ctx, ConnectionPool.getInstance());
            ItemController.showTopping(ctx, ConnectionPool.getInstance());
            ctx.render("index.html");
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

            User currentUser = ctx.sessionAttribute("currentUser");

            ArrayList<Order> tempOrderLine = ctx.sessionAttribute("orders");

            List<Topping> toppingList = ItemMapper.showToppings(connectionPool);
            List<Bottom> bottomList = ItemMapper.showBottoms(connectionPool);

            if(tempOrderLine != null) {
                ItemMapper.deleteUsersBasket(currentUser.getUserId(), connectionPool);
                for (Order order : tempOrderLine) {
                    int toppingId = 0;
                    for (Topping topping : toppingList) {
                        if (order.getTopping().equals(topping.getTopping())) toppingId = topping.getToppingId();
                    }
                    int bottomId = 0;
                    for (Bottom bottom : bottomList) {
                        if (order.getBottom().equals(bottom.getBottom())) bottomId = bottom.getBottomId();
                    }

                    ItemMapper.insertOrderline(currentUser.getUserId(), toppingId, bottomId, order.getQuantity(), order.getOrderlinePrice(), connectionPool);

                }
            }
            tempOrderLine.clear();
            ctx.req().getSession().invalidate();
            ctx.redirect("/");
    }

    public static void login(Context ctx, ConnectionPool connectionPool) {
        // Hent form parametre
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        ctx.attribute("login", true);
        ctx.attribute("loginsuccess", false);

        // Check om bruger findes i DB med de angivne username + password
        try {
            User user = UserMapper.login(email, password, connectionPool);
            ctx.sessionAttribute("currentUser", user);
            List<Topping> toppingList = ItemMapper.showToppings(connectionPool);
            List<Bottom> bottomList = ItemMapper.showBottoms(connectionPool);
            List<Order> orderLines = ItemMapper.getBasket(user, bottomList, toppingList, connectionPool);
            if(!orderLines.isEmpty()) {
                ctx.sessionAttribute("orders", orderLines);
            }

            // Hvis ja, send videre til forsiden med login besked
            ctx.attribute("message", "Du er nu logget ind");
            ctx.attribute("loginsuccess", true);
            if (user.isAdmin()) {
                ctx.render("adminSite.html");
            } else {
                int orderCount = 0;
                int totalAmount = 0;
                if (orderLines != null) {
                    for (Order orderline : orderLines) {
                        orderCount++;
                        totalAmount += orderline.getOrderlinePrice();
                    }
                }
                ctx.sessionAttribute("totalAmount", totalAmount);
                ctx.sessionAttribute("orderCount", orderCount);
                ctx.render("index.html");

            }
        } catch (DatabaseException e) {
            // Hvis nej, send tilbage til login side med fejl besked
            ctx.attribute("message", "Forkert brugernavn eller password. Prøv igen eller opret brugeren!");

            ctx.render("login.html");
        }
    }
}

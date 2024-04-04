package app.controllers;

import app.entities.Bottom;
import app.entities.Topping;
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
                createOrder();
            });
        }

    private static void createOrder() {

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

package app.controllers;

import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.ItemMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class ItemController {
        public static void addRoutes(Javalin app, ConnectionPool connectionPool)
        {
            app.post("showBottom", ctx -> showBottom(ctx, connectionPool));
            app.post("showTopping", ctx -> showTopping(ctx, connectionPool));
        }

    private static void showBottom(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        ItemMapper.showBottoms(connectionPool);
        ctx.render("???.html");
    }

    private static void showTopping(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        ItemMapper.showToppings(connectionPool);
        ctx.render("???.html");
    }

}

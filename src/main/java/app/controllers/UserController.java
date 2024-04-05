package app.controllers;

import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.UserMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class UserController
{
    public static void addRoutes(Javalin app)
    {
        app.post("login", ctx -> {
            ItemController.showBottom(ctx,ConnectionPool.getInstance());
            ItemController.showTopping(ctx,ConnectionPool.getInstance());
            login(ctx, ConnectionPool.getInstance());
        });
        app.get("login", ctx -> {
            ItemController.showBottom(ctx,ConnectionPool.getInstance());
            ItemController.showTopping(ctx,ConnectionPool.getInstance());
            ctx.render("login.html");
        });
        app.get("index.html", ctx -> {
            ItemController.showBottom(ctx,ConnectionPool.getInstance());
            ItemController.showTopping(ctx,ConnectionPool.getInstance());
            ctx.render("index.html");
        });
        app.get("logout", ctx -> logout(ctx));
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

        if(!userexist) {
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
        } else
        {
            ctx.attribute("message", "Dit brugernavn findes allerede. Prøv igen, eller log ind");
            ctx.attribute("login", true);
            ctx.render("login.html");
        }
    }

    private static void logout(Context ctx)
    {
        ctx.req().getSession().invalidate();
        ctx.redirect("/");
    }

    public static void login(Context ctx, ConnectionPool connectionPool)
    {
        // Hent form parametre
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        ctx.attribute("login", true);
        ctx.attribute("loginsuccess", false);

        // Check om bruger findes i DB med de angivne username + password
        try
        {
            User user = UserMapper.login(email, password, connectionPool);
            ctx.sessionAttribute("currentUser", user);
            // Hvis ja, send videre til forsiden med login besked
            ctx.attribute("message", "Du er nu logget ind");
            ctx.attribute("loginsuccess", true);
            if(user.isAdmin()){
                ctx.render("adminSite.html");
            } else {
                ctx.render("login.html");
            }
        }
        catch (DatabaseException e)
        {
            // Hvis nej, send tilbage til login side med fejl besked
            ctx.attribute("message", "Forkert brugernavn eller password. Prøv igen eller opret brugeren!");

            ctx.render("login.html");
        }
    }
}

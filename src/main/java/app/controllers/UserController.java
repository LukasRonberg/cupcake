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
        app.post("login", ctx -> login(ctx, ConnectionPool.getInstance()));
        app.get("login", ctx -> ctx.render("login.html"));
        app.get("index.html", ctx -> ctx.render("index.html"));
        app.get("logout", ctx -> logout(ctx));
        app.post("createuser", ctx -> createUser(ctx, ConnectionPool.getInstance()));
        app.get("createuser", ctx -> ctx.render("createuser.html"));
    }

    private static void createUser(Context ctx, ConnectionPool connectionPool)
    {
        // Hent form parametre
        String email = ctx.formParam("email");
        String password1 = ctx.formParam("password1");
        String password2 = ctx.formParam("password2");
        String name = ctx.formParam("name");
        String mobile = ctx.formParam("mobile");

        ctx.attribute("created", true);

        if (password1.equals(password2))
        {
            try
            {
                UserMapper.createuser(email, password1, name, mobile, connectionPool);
                ctx.attribute("message", "Du er hermed oprettet med brugernavn: " + email +
                        ". Nu kan du logge på.");
                ctx.render("createuser.html");
            }

            catch (DatabaseException e)
            {
                ctx.attribute("message", "Dit brugernavn findes allerede. Prøv igen, eller log ind");
                ctx.render("createuser.html");
            }
        } else
        {
            ctx.attribute("message", "Dine to passwords matcher ikke! Prøv igen");
            ctx.render("createuser.html");
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

        // Check om bruger findes i DB med de angivne username + password
        try
        {
            User user = UserMapper.login(email, password, connectionPool);
            ctx.sessionAttribute("currentUser", user);
            // Hvis ja, send videre til forsiden med login besked
            ctx.attribute("message", "Du er nu logget ind");
            ctx.render("index.html");
        }
        catch (DatabaseException e)
        {
            // Hvis nej, send tilbage til login side med fejl besked
            ctx.attribute("message", e.getMessage() );
            ctx.render("index.html");
        }
    }
}

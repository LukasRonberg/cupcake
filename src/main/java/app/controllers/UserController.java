package app.controllers;

import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.UserMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class UserController
{
    public static void addRoutes(Javalin app, ConnectionPool connectionPool)
    {
        app.post("login", ctx -> login(ctx, connectionPool));
        app.get("logout", ctx -> logout(ctx));
        app.get("createuser", ctx -> ctx.render("createuser.html"));
        app.post("createuer", ctx -> createUser(ctx, connectionPool));
    }

    private static void createUser(Context ctx, ConnectionPool connectionPool)
    {
        //Hent form parametre
        String name = ctx.formParam("name");
        String mail = ctx.formParam("mail");
        int mobile = Integer.parseInt(ctx.formParam("mobile"));
        String password1 = ctx.formParam("password1");
        String password2 = ctx.formParam("password2");

        if (password1.equals(password2))
        {
            try
            {
                UserMapper.createuser(name, mail, password1, mobile, connectionPool);
                ctx.attribute("message", "Du er oprettet med e-mail: " + mail +
                        ". Nu skal du logge på.");
                ctx.render("index.html");
            }
            catch (DatabaseException e)
            {
                ctx.attribute("message", "Dine to passwords matcher ikke! Prøv igen");
                ctx.render("createuser.html");
            }
        }
    }

    private static void logout(Context ctx)
    {
        ctx.req().getSession().invalidate();
        ctx.redirect("/");
    }

    public static void login(Context ctx, ConnectionPool connectionPool)
    {
        String mail = ctx.formParam("mail");
        String password = ctx.formParam("password");

        try
        {
            User user = UserMapper.login(mail, password, connectionPool);
            ctx.sessionAttribute("currentUser", user);
            //Hvis ja, send videre til forsiden med login besked
            ctx.attribute("message", "Du er nu logget ind");
            ctx.render("index.html");
        }
        catch (DatabaseException e)
        {
            //Hvis nej, send tilbage til login side med fejl besked
            ctx.attribute("message", e.getMessage());
            ctx.render("index.html");
        }
    }
}

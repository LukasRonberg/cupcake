package app;


import app.config.ThymeleafConfig;
import app.controllers.ItemController;
import app.controllers.UserController;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;


public class Main
{
    public static void main(String[] args) {

        // Initializing Javalin and Jetty webserver

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            config.fileRenderer(new JavalinThymeleaf(ThymeleafConfig.templateEngine()));
        }).start(7070);


        //Routing

        ItemController.addRoutes(app);

        UserController.addRoutes(app);


    }
}


package server;

import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/session", new LoginHandler());
        Spark.post("/user", new RegisterHandler());
        Spark.delete("/db", new ClearHandler());
        Spark.delete("/session", new LogoutHandler());

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}

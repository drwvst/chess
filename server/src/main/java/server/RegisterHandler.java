package server;

import com.google.gson.Gson;
import model.*;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Objects;

public class RegisterHandler implements Route{
    private final UserService userService = new UserService();
    private final Gson gson = new Gson();

    @Override
    public Object handle(Request request, Response result) {
        try {
            RegisterRequest registerRequest = gson.fromJson(request.body(), RegisterRequest.class);
            RegisterResult registerResult = userService.register(registerRequest);

            result.status(200);
            return gson.toJson(registerResult);
        } catch (Exception e){
            if(Objects.equals(e.getMessage(), "Username already taken")){
                result.status(403);
                return gson.toJson(new RegisterHandler.ErrorMessage("Error: " + e.getMessage()));
            }
            result.status(400);
            return gson.toJson(new RegisterHandler.ErrorMessage("Error: " + e.getMessage()));
        }
    }
    private record ErrorMessage(String message) {}
}

package server;

import com.google.gson.Gson;
import model.LoginRequest;
import model.LoginResult;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Objects;

public class LoginHandler implements Route {
    private final UserService userService = new UserService();
    private final Gson gson = new Gson();

    @Override
    public Object handle(Request request, Response result) {
        try {
            LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);
            LoginResult loginResult = userService.login(loginRequest);

            result.status(200);
            return gson.toJson(loginResult);
        } catch (Exception e){
            if(Objects.equals(e.getMessage(), "unauthorized")){
                result.status(401);
                return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
            }
            result.status(500);
            return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
        }
    }
    private record ErrorMessage(String message) {}


}

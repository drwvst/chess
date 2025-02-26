package server;

import com.google.gson.Gson;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

public class LogoutHandler implements Route{
    private final UserService userService = new UserService();
    private final Gson gson = new Gson();

    @Override
    public Object handle(Request request, Response response) {
        try{
            String authToken = request.headers("authorization");
            if(authToken == null || authToken.isBlank()){
                response.status(401);
                return gson.toJson(new ErrorMessage("Error: Unauthorized"));
            }

            userService.logout(authToken);
            return gson.toJson(new SuccessMessage());
        } catch (Exception e){
            response.status(401);
            return gson.toJson(new ErrorMessage("Error: Unauthorized"));
        }
    }

    private record SuccessMessage() {}  // Empty JSON response `{}`

    private record ErrorMessage(String message) {}
}

package server;

import com.google.gson.Gson;
import model.CreateGameRequest;
import model.CreateGameResult;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Objects;

public class CreateGameHandler implements Route{
    private final GameService gameService = new GameService();
    private final Gson gson = new Gson();

    @Override
    public Object handle(Request request, Response response){
        try{
            String authToken = request.headers("authorization");
            CreateGameRequest cgRequest = gson.fromJson(request.body(), CreateGameRequest.class);
            CreateGameResult cgResult = new CreateGameResult(gameService.createGame(authToken, cgRequest.gameName()).gameID());
            response.status(200);
            return gson.toJson(cgResult);
        } catch (Exception e){
            if(Objects.equals(e.getMessage(), "unauthorized")){
                response.status(401);
                return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
            }
            if(Objects.equals(e.getMessage(), "bad request")){
                response.status(400);
                return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
            }
            response.status(501);
            return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private record ErrorMessage(String message) {}
}

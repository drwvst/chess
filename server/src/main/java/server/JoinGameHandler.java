package server;

import com.google.gson.Gson;
import model.JoinGameRequest;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;


import java.util.Objects;

public class JoinGameHandler implements Route{
    private final GameService gameService = new GameService();
    private final Gson gson = new Gson();

    @Override
    public Object handle(Request request, Response response){
        try{
            String authToken = request.headers("authorization");
            JoinGameRequest jgRequest = gson.fromJson(request.body(), JoinGameRequest.class);
            gameService.joinGame(authToken, jgRequest.gameID(), jgRequest.playerColor());
            response.status(200);
            return gson.toJson(new SuccessMessage());
        } catch (Exception e) {
            if(Objects.equals(e.getMessage(), "bad request")){
                response.status(400);
                return gson.toJson(new ErrorMessage(e.getMessage()));
            }
            if(Objects.equals(e.getMessage(), "unauthorized")){
                response.status(401);
                return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
            }
            if(Objects.equals(e.getMessage(), "already taken")){
                response.status(403);
                return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
            }
            response.status(500);
            return gson.toJson(new ErrorMessage(e.getMessage()));
        }
    }

    private record SuccessMessage() {}
    private record ErrorMessage(String message) {}
}

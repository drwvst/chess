package server;

import com.google.gson.Gson;
import model.ListGamesResult;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

public class ListGamesHandler implements Route{
    private final GameService gameService = new GameService();
    private final Gson gson = new Gson();

    @Override
    public Object handle(Request request, Response response){
        try{
            String authToken = request.headers("authorization");
            ListGamesResult result = new ListGamesResult(gameService.listGames(authToken));
            response.status(200);
            return gson.toJson(result);
        } catch (Exception e){
            response.status(401);
            return gson.toJson(new ErrorMessage("Error: Unauthorized"));
        }
    }

    private record ErrorMessage(String message) {}
}

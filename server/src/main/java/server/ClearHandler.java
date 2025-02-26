package server;

import com.google.gson.Gson;
import service.ClearService;
import spark.Request;
import spark.Response;
import spark.Route;

public class ClearHandler implements Route{
    private final ClearService clearService = new ClearService();
    private final Gson gson = new Gson();

    @Override
    public Object handle(Request request, Response result) {
        try {
            clearService.clear();
            result.status(200);
            return gson.toJson(new SuccessMessage());
        } catch (Exception e){
            result.status(500);
            return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
        }
    }
    private record ErrorMessage(String message) {}
    private record SuccessMessage() {}
}

package serverfacade;

import com.google.gson.Gson;
import exception.ResponseException;
import model.*;

import java.io.*;
import java.net.*;
import java.util.List;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url){
        serverUrl = url;
    }

    /// Implement the web API calls here as listed on notes doc
    public AuthData login(String username, String password) throws ResponseException{
        var path = "/session";
        return this.makeRequest("POST", path, new LoginRequest(username, password), AuthData.class, null);
    }

    public AuthData register(String username,String password, String email) throws ResponseException{
        var path = "/user";
        return this.makeRequest("POST", path, new RegisterRequest(username, password, email), AuthData.class, null);
    }

    public void clear() throws ResponseException{
        var path = "/db";
        this.makeRequest("DELETE", path, null, null, null);
    }

    public void logout(String authToken) throws ResponseException {
        makeRequest("DELETE", "/session", null, null, authToken);
    }

    public GameData createGame(String authToken, String gameName) throws ResponseException{
        var path = "/game";
        return this.makeRequest("POST", path, new CreateGameRequest(gameName), GameData.class, authToken);
    }

    public List<GameData> listGames(String authToken) throws ResponseException {
        var path = "/game";
        return this.makeRequest("GET", path, null, ListGamesResult.class, authToken).games();
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws ResponseException{
        var path = "/game";
        makeRequest("PUT", path, new JoinGameRequest(playerColor,gameID), null, authToken);
    }



    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws ResponseException {
        try {
            String fullUrl = serverUrl + path;
            // System.out.println("Attempting request to: " + fullUrl);

            URL url = (new URI(fullUrl)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (authToken != null) {
                http.setRequestProperty("Authorization", authToken);
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(500, "Failed to connect: " + ex.getMessage());
        }
    }



    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();

        if (!isSuccessful(status)) {
            // Read error response body
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    throw ResponseException.fromJson(respErr);
                }
            }

            throw new ResponseException(status, "other failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }


    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }

}

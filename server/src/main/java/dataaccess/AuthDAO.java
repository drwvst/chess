package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthDAO {
    private final Map<String, AuthData> authTokens = new HashMap<>();
    //<AuthToken string, AuthData object>

    public AuthData createAuth(String username) {
        String token = UUID.randomUUID().toString();
        AuthData authData = new AuthData(token, username);
        authTokens.put(token, authData);
        return authData;
    }

    public AuthData getAuthToken(String token) throws DataAccessException {
        if(!authTokens.containsKey(token)) {
            throw new DataAccessException("Invalid auth token");
        }
        return authTokens.get(token);
    }

    public void deleteAuth(String token) {
        authTokens.remove(token);
    }


}

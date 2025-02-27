package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthDAO {
    private static final AuthDAO instance = new AuthDAO();
    private final Map<String, AuthData> authTokens = new HashMap<>();
    //<AuthToken string, AuthData object>

    private AuthDAO() {}  //private constructor to prevent external instantiation
    public static AuthDAO getInstance() {
        return instance;
    }

    public AuthData createAuth(String username) {
        String token = UUID.randomUUID().toString();
        AuthData authData = new AuthData(token, username);
        authTokens.put(token, authData);
        return authData;
    }

    public AuthData getAuthToken(String token) throws DataAccessException {
        if(!authTokens.containsKey(token)) {
            throw new DataAccessException("unauthorized");
        }
        return authTokens.get(token);
    }

    public void deleteAuth(String token) {
        authTokens.remove(token);
    }


    public void clear(){
        authTokens.clear();
    }

}

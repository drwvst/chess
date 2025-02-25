package dataaccess;

import model.UserData;
import java.util.HashMap;
import java.util.Map;

public class UserDAO {
    private final Map<String, UserData> users = new HashMap<>();

    public void createUser(UserData user) throws DataAccessException {
        if(users.containsKey(user.username())){
            throw new DataAccessException("Username already exists");
        }
        users.put(user.username(), user);
    }
    public UserData getUser(String username) throws DataAccessException {
        if(!users.containsKey(username)){
            throw new DataAccessException("User does not exist");
        }
        return users.get(username);
    }
}

package dataaccess;

import model.UserData;
import java.util.HashMap;
import java.util.Map;

public class UserDAO {
    private static final UserDAO instance = new UserDAO(); //makes only one instance of UserDAO
    private final Map<String, UserData> users = new HashMap<>();

    private UserDAO() {}  //private constructor to prevent external instantiation
    public static UserDAO getInstance() {
        return instance;
    }

    public void createUser(UserData user) throws DataAccessException {
        if(users.containsKey(user.username())){
            throw new DataAccessException("Username already taken");
        }
        users.put(user.username(), user);
    }
    public UserData getUser(String username) throws DataAccessException {
        if(!users.containsKey(username)) {
            throw new DataAccessException("Unauthorized");
        }
        return users.get(username);
    }

    public void clear(){
        users.clear();
    }
}

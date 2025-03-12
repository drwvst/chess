package service;

import dataaccess.*;
import model.*;

public class UserService {
//    private final UserDAO userDAO = UserDAO.getInstance();
//    private final AuthDAO authDAO = AuthDAO.getInstance();
    private final MySQLUserDAO mySQLUserDAO = MySQLUserDAO.getInstance();
    private final MySQLAuthDAO mySQLAuthDAO = MySQLAuthDAO.getInstance();


    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        UserData user = mySQLUserDAO.getUser(loginRequest.username());

        if(!user.password().equals(loginRequest.password())) {
            throw new DataAccessException("unauthorized");
        }

        AuthData authData = mySQLAuthDAO.createAuth(loginRequest.username());
        return new LoginResult(authData.username(), authData.authToken());
    }

    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException{
        //validate
        if (registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null ||
                registerRequest.username().isBlank() || registerRequest.password().isBlank() || registerRequest.email().isBlank()) {
            throw new DataAccessException("Bad request");
        }

        //create new user
        UserData newUserData = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
        mySQLUserDAO.createUser(newUserData);

        AuthData authData = mySQLAuthDAO.createAuth(newUserData.username());
        return new RegisterResult(newUserData.username(), authData.authToken());
    }

    public void logout(String token) throws DataAccessException {
//        if(mySQLAuthDAO.getAuthToken(token) == null){
//            throw new DataAccessException("Unauthorized");
//        }
        mySQLAuthDAO.deleteAuth(token);
    }


}


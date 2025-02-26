package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import model.*;

public class UserService {
    private final UserDAO userDAO = UserDAO.getInstance();
    private final AuthDAO authDAO = AuthDAO.getInstance();


    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        UserData user = userDAO.getUser(loginRequest.username());

        if(!user.password().equals(loginRequest.password())) {
            throw new DataAccessException("Unauthorized");
        }

        AuthData authData = authDAO.createAuth(loginRequest.username());
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
        userDAO.createUser(newUserData);

        AuthData authData = authDAO.createAuth(newUserData.username());
        return new RegisterResult(newUserData.username(), authData.authToken());
    }




    /*
    public void logout(LogoutRequest logoutRequest) {}
    */

}


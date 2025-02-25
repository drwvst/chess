package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.LoginRequest;
import model.LoginResult;
import model.UserData;

public class UserService {
    private final UserDAO userDAO = new UserDAO();
    private final AuthDAO authDAO = new AuthDAO();

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        UserData user = userDAO.getUser(loginRequest.username());

        if(!user.password().equals(loginRequest.password())) {
            throw new DataAccessException("Unauthorized");
        }

        AuthData authData = authDAO.createAuth(loginRequest.username());
        return new LoginResult(authData.username(), authData.authToken());
    }

    /*
    public RegisterResult register(RegisterRequest registerRequest) {}
    public void logout(LogoutRequest logoutRequest) {}
     */

}


package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;

public class LogoutService {
    private final AuthDAO authDAO = AuthDAO.getInstance();

    public void logout(String token) throws DataAccessException {
        if(authDAO.getAuthToken(token) == null){
            throw new DataAccessException("Unauthorized");
        }
        authDAO.deleteAuth(token);
    }
}

package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import dataaccess.GameDAO;
import dataaccess.DataAccessException;

public class ClearService {
    private final UserDAO userDAO = UserDAO.getInstance();
    private final AuthDAO authDAO = AuthDAO.getInstance();
    private final GameDAO gameDAO = GameDAO.getInstance();

    public void clear(){
        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
    }
}

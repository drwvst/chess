package service;

import dataaccess.DatabaseManager;
import dataaccess.DataAccessException;

public class ClearService {

    public void clear() throws DataAccessException{
        DatabaseManager.clear();
    }
}

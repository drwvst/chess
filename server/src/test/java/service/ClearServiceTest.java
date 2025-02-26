package service;

import dataaccess.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ClearServiceTest {
    private ClearService clearService;

    @BeforeEach
    void setUp() {
        clearService = new ClearService();
    }

    @Test
    void testClear(){
        assertDoesNotThrow(() -> clearService.clear());
    }

}

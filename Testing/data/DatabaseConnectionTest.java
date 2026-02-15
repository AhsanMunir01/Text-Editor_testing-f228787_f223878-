package data;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import dal.DatabaseConnection;

/**
 * Test Class: DatabaseConnectionTest
 * Purpose: Test Singleton pattern implementation and database connectivity
 */
public class DatabaseConnectionTest {
    
    @Before
    public void setUp() throws Exception {
        // Setup code if needed
    }
    
    @After  
    public void tearDown() throws Exception {
        // Cleanup code if needed
    }
    
    /**
     * Test Singleton pattern - same instance returned
     */
    @Test
    public void testDatabaseConnection_Singleton_ReturnsSameInstance() {
        // Act
        DatabaseConnection instance1 = DatabaseConnection.getInstance();
        DatabaseConnection instance2 = DatabaseConnection.getInstance();
        
        // Assert
        assertNotNull("First instance should not be null", instance1);
        assertNotNull("Second instance should not be null", instance2);
        assertSame("Database connection should return same instance", instance1, instance2);
    }
    
    /**
     * Test that database connection is not null
     */
    @Test
    public void testDatabaseConnection_GetConnection_NotNull() {
        // Arrange
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        
        // Act
        // Note: Connection might be null if database is not available, 
        // but getInstance() should never return null
        
        // Assert
        assertNotNull("DatabaseConnection instance should not be null", dbConnection);
        // Test that getConnection method is accessible
        assertNotNull("getConnection method should be accessible", dbConnection.getConnection());
    }
    
    /**
     * Test thread safety of Singleton implementation
     */
    @Test
    public void testDatabaseConnection_ThreadSafety_MultipleThreadsGetSameInstance() 
            throws InterruptedException {
        // Arrange
        final DatabaseConnection[] instances = new DatabaseConnection[2];
        final Exception[] exceptions = new Exception[2];
        
        Thread thread1 = new Thread(new Runnable() {
            public void run() {
                try {
                    instances[0] = DatabaseConnection.getInstance();
                } catch (Exception e) {
                    exceptions[0] = e;
                }
            }
        });
        
        Thread thread2 = new Thread(new Runnable() {
            public void run() {
                try {
                    instances[1] = DatabaseConnection.getInstance();
                } catch (Exception e) {
                    exceptions[1] = e;
                }
            }
        });
        
        // Act
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        
        // Assert
        assertNull("Thread 1 should not throw exception", exceptions[0]);
        assertNull("Thread 2 should not throw exception", exceptions[1]);
        assertNotNull("Thread 1 should get instance", instances[0]);
        assertNotNull("Thread 2 should get instance", instances[1]);
        assertSame("Both threads should get same singleton instance", instances[0], instances[1]);
    }
    
    /**
     * Test that connection can be closed gracefully
     */
    @Test
    public void testDatabaseConnection_CloseConnection_NoExceptionThrown() {
        // Arrange
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        
        // Act & Assert
        try {
            dbConnection.closeConnection();
            // If we reach here, no exception was thrown
            assertTrue("Connection close should not throw exception", true);
        } catch (Exception e) {
            fail("Connection close should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * Test singleton behavior after multiple getInstance calls
     */
    @Test
    public void testDatabaseConnection_MultipleGetInstance_AlwaysSameObject() {
        // Act
        DatabaseConnection instance1 = DatabaseConnection.getInstance();
        DatabaseConnection instance2 = DatabaseConnection.getInstance();
        DatabaseConnection instance3 = DatabaseConnection.getInstance();
        
        // Assert
        assertSame("First and second instance should be same", instance1, instance2);
        assertSame("Second and third instance should be same", instance2, instance3);
        assertSame("First and third instance should be same", instance1, instance3);
    }
}
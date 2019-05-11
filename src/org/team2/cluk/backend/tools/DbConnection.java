package org.team2.cluk.backend.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
 * DbConnection Class for handling SQL connection for the backend
 */

public class DbConnection {
    private static Connection connection = null;
    private static String _username = "";
    private static String _password = "";
    private static String _url = "";

    /*
    * method to connect the backend with the frontend 
    * @param username 
    * @param password
    * @param url
    */
    public static void connect(String username, String password, String url) {
        try {
            // save login credentials so connection can be restored later
            _username = username;
            _password = password;
            _url = url;
            // importing MySQL driver as per MySQL website
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            connection = DriverManager.getConnection(url, username, password);
            ServerLog.writeLog("Database connection established");
        } catch (ClassNotFoundException e1) {
            ServerLog.writeLog("JDBC driver not found");
        } catch (SQLException e2) {
            ServerLog.writeLog("Database connection error");
        } catch (Exception e) {
            ServerLog.writeLog("Unrecognised exception encountered");
            e.printStackTrace();
        }
    }

    /*
    * method to disconnect from the database
    */
    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            ServerLog.writeLog("Exception occurred when disconnecting from the database");
        }
    }

    /*
    * method to get the database connection
    */
    public static Connection getConnection() {
        if (connection == null) {
            try {
                ServerLog.writeLog("Restoring database connection");
                connection = DriverManager.getConnection(_url, _username, _password);
            } catch (SQLException e) {
                ServerLog.writeLog("Cannot restore connection with database");
            }
        }
        return connection;
    }
}

package storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCConnection {

    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String SERVER_PATH = "localhost:3306";
    private static final String DB_NAME = "test_task";
    private static final String DB_LOGIN = "root";
    private static final String DB_PASSWORD = "pass";
    private static final String DBMS = "mysql";
    private static Connection connection;

    private JDBCConnection() {
        initDriver();
        initConnection();
    }

    private static void initConnection() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("jdbc:")
                .append(DBMS)
                .append("://")
                .append(SERVER_PATH)
                .append("/")
                .append(DB_NAME)
                .append("?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");

        try {
            connection = DriverManager.getConnection(urlBuilder.toString(), DB_LOGIN, DB_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void initDriver() {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        if (connection == null) {
            initDriver();
            initConnection();
        }

        return connection;
    }
}

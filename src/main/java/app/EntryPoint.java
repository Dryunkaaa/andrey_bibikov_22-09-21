package app;

import service.SqlExecutor;
import storage.JDBCConnection;

import java.io.File;
import java.net.URISyntaxException;

public class EntryPoint {

    private static final String SCRIPT_NAME = "script.sql";

    public static void main(String[] args) throws URISyntaxException {
        SqlExecutor sqlExecutor = new SqlExecutor();
        sqlExecutor.executeScript(JDBCConnection.getConnection(), new File(EntryPoint.class.getResource("/" + SCRIPT_NAME).toURI()));
    }
}

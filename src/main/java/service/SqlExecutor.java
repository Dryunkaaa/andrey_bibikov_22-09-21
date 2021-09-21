package service;

import org.hsqldb.cmdline.SqlFile;
import org.hsqldb.cmdline.SqlToolError;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class SqlExecutor {

    public boolean executeScript(Connection connection, File scriptFile) {
        try {
            SqlFile sqlFile = new SqlFile(scriptFile);
            sqlFile.setConnection(connection);
            sqlFile.execute();

            return true;
        } catch (IOException | SQLException | SqlToolError e) {
            e.printStackTrace();
        }

        return false;
    }
}

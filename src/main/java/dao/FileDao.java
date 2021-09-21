package dao;

import model.File;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FileDao {

    public interface CFG {
        String ID = "ContentId";
        String FULL_PATH = "FileName";
        String IS_DIRECTORY = "IsDirectory";
        String PARENT_ID = "ParentId";
    }

    private Connection connection;

    public FileDao(Connection connection) {
        this.connection = connection;
    }

    public List<File> getInnerFiles(long catalogId) {
        return getListByQuery("select * from files where " + CFG.PARENT_ID + " = " + catalogId);
    }

    private List<File> getListByQuery(String query) {
        List<File> result = new ArrayList<>();
        ResultSet rs = null;
        try {
            Statement statement = connection.createStatement();
            rs = statement.executeQuery(query);

            while (rs.next()) {
                result.add(createFile(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResultSet(rs);
        }

        return result;
    }

    public File getRootDirectory() {
        File result = null;
        ResultSet rs = null;
        try {
            Statement statement = connection.createStatement();
            rs = statement.executeQuery("select * from files where parentId is null");

            while (rs.next()) {
                result = createFile(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResultSet(rs);
        }

        return result;
    }

    private void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private File createFile(ResultSet rs) throws SQLException {
        File file = new File();

        file.setId(rs.getLong(CFG.ID));
        file.setFullPath(rs.getString(CFG.FULL_PATH));
        file.setDirectory(rs.getBoolean(CFG.IS_DIRECTORY));
        file.setParentId(rs.getLong(CFG.PARENT_ID));

        return file;
    }
}

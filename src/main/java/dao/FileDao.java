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

    public List<File> getAllFiles(long dirId) {
        return getListByQuery("select* from files where IsDirectory = false and parentId = " + dirId);
    }

    public List<File> getAllDirs(long dirId) {
        return getListByQuery("select* from files where IsDirectory = true and parentId = " + dirId);
    }

    public List<File> getInnerFiles(long catalogId) {
        return getListByQuery("select * from files where " + CFG.PARENT_ID + " = " + catalogId);
    }

    public List<File> getFilesToEncode(List<String> encodeExtensions) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select * from files");

        for (int i = 0; i < encodeExtensions.size(); i++) {
            sqlBuilder.append(i == 0 ? " where " : " or ")
                    .append(CFG.FULL_PATH)
                    .append(" like '%")
                    .append(encodeExtensions.get(i))
                    .append("%'");
        }

        return getListByQuery(sqlBuilder.toString());
    }

    public List<File> getFilesToCopy(List<String> encodeExtensions) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select * from files");

        for (int i = 0; i < encodeExtensions.size(); i++) {
            sqlBuilder.append(i == 0 ? " where " : " and ")
                    .append(CFG.FULL_PATH)
                    .append(" not like '%")
                    .append(encodeExtensions.get(i))
                    .append("'");
        }

        sqlBuilder.append(" and ")
                .append(CFG.IS_DIRECTORY)
                .append(" = false");

        return getListByQuery(sqlBuilder.toString());
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
        return getSingleFileByQuery("select * from files where parentId is null");
    }

    public File getFileById(long id) {
        return getSingleFileByQuery("select * from files where contentid = " + id);
    }

    private File getSingleFileByQuery(String query) {
        File result = null;
        ResultSet rs = null;
        try {
            Statement statement = connection.createStatement();
            rs = statement.executeQuery(query);

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

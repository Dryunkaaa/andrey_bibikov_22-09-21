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

    public List<File> getEncodeFilesForDir(long dirId, List<String> encodeExtensions) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select * from files");

        for (int i = 0; i < encodeExtensions.size(); i++) {
            sqlBuilder.append(i == 0 ? " where (" : " or ")
                    .append(CFG.FULL_PATH)
                    .append(" like '%")
                    .append(encodeExtensions.get(i))
                    .append("%'");
        }

        sqlBuilder.append(") and ")
                .append(CFG.PARENT_ID)
                .append(" = ")
                .append(dirId);

        return getListByQuery(sqlBuilder.toString());
    }

    public List<File> getCopyFilesForDir(long dirId, List<String> encodeExtensions) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select * from files");

        for (int i = 0; i < encodeExtensions.size(); i++) {
            sqlBuilder.append(i == 0 ? " where " : " and ")
                    .append(CFG.FULL_PATH)
                    .append(" not like '%")
                    .append(encodeExtensions.get(i))
                    .append("%'");
        }

        sqlBuilder.append(" and ")
                .append(CFG.IS_DIRECTORY)
                .append("= false and ")
                .append(CFG.PARENT_ID)
                .append("=")
                .append(dirId);

        return getListByQuery(sqlBuilder.toString());
    }

    public List<File> getDirectoriesWithCopingFiles(List<String> encodeExtensions) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select* from files where contentid in (select parentId from files");

        for (int i = 0; i < encodeExtensions.size(); i++) {
            sqlBuilder.append(i == 0 ? " where " : " and ")
                    .append(CFG.FULL_PATH)
                    .append(" not like '%")
                    .append(encodeExtensions.get(i))
                    .append("'");
        }

        sqlBuilder.append(" and ")
                .append(CFG.IS_DIRECTORY)
                .append("= false group by ")
                .append(CFG.PARENT_ID)
                .append(")");

        return getListByQuery(sqlBuilder.toString());
    }

    public List<File> getDirectoriesWithEncodingFiles(List<String> encodeExtensions) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select * from files where ")
                .append(CFG.ID).append(" in (select ")
                .append(CFG.PARENT_ID)
                .append(" from files");

        for (int i = 0; i < encodeExtensions.size(); i++) {
            sqlBuilder.append(i == 0 ? " where " : " or ")
                    .append(CFG.FULL_PATH)
                    .append(" like '%")
                    .append(encodeExtensions.get(i))
                    .append("%'");
        }

        sqlBuilder.append(" group by ")
                .append(CFG.PARENT_ID)
                .append(")");

        return getListByQuery(sqlBuilder.toString());
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

    public File getFileById(long id) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select * from files where ")
                .append(CFG.ID)
                .append("=")
                .append(id);

        return getSingleFileByQuery(sqlBuilder.toString());
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

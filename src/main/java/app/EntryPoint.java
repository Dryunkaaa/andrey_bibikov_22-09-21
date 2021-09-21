package app;

import service.FileService;
import service.SqlExecutor;
import storage.JDBCConnection;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

public class EntryPoint {

    private static final String SCRIPT_NAME = "script.sql";

    public static void main(String[] args) throws URISyntaxException {
//        initDataToDB();

        FileService fileService = new FileService(JDBCConnection.getConnection());

        List<model.File> filesToEncode = fileService.getFilesToEncode();
        List<model.File> filesToCopy = fileService.getFilesToCopy();

        printDividingLine();
        System.out.println("Files\\Directories to encode:");
        filesToEncode.forEach(file -> System.out.println(file.getFullPath()));

        printDividingLine();
        System.out.println("Files\\Directories to copy:");
        filesToCopy.forEach(file -> System.out.println(file.getFullPath()));
    }

    private static void printDividingLine() {
        System.out.println("\n----------------\n");
    }

    private static void initDataToDB() throws URISyntaxException {
        SqlExecutor sqlExecutor = new SqlExecutor();
        sqlExecutor.executeScript(JDBCConnection.getConnection(), new File(EntryPoint.class.getResource("/" + SCRIPT_NAME).toURI()));
    }
}

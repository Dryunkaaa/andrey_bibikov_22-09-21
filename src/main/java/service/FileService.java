package service;

import dao.FileDao;
import model.File;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileService {

    private static final List<String> CODING_EXTENSIONS = Arrays.asList(".tif", ".mov", ".avi");
    private FileDao fileDao;

    public FileService(Connection connection) {
        fileDao = new FileDao(connection);
    }

    public List<File> getFilesToEncode() {
        List<File> result = new ArrayList<>();
        File rootDirectory = getRootDirectory();

        fillEncodingFiles(rootDirectory, result);

        return result;
    }

    public List<File> getFilesToCopy() {
        List<File> result = new ArrayList<>();
        File rootDirectory = getRootDirectory();

        fillFilesToCopy(rootDirectory, result);


        return result;
    }

    public List<File> getInnerFiles(long catalogId) {
        return fileDao.getInnerFiles(catalogId);
    }

    public File getRootDirectory() {
        return fileDao.getRootDirectory();
    }

    private boolean canBeEncode(File file) {
        if (file.isDirectory()) {
            List<File> innerFiles = getInnerFiles(file.getId());
            return !innerFiles.isEmpty() && innerFiles.stream().allMatch(this::canBeEncode);
        }

        return canBeEncoded(file.getFullPath());
    }

    private void fillEncodingFiles(File file, List<File> encodeFiles) {
        if (canBeEncode(file)) {
            encodeFiles.add(file);
        } else if (file.isDirectory()) {
            getInnerFiles(file.getId()).forEach(innerFile -> fillEncodingFiles(innerFile, encodeFiles));
        }
    }

    private boolean canBeCopy(File file) {
        if (file.isDirectory()) {
            List<File> innerFiles = getInnerFiles(file.getId());
            return !innerFiles.isEmpty() && innerFiles.stream().allMatch(this::canBeCopy);
        }

        return canBeCopy(file.getFullPath());
    }

    private void fillFilesToCopy(File file, List<File> filesToCopy) {
        if (canBeCopy(file)) {
            filesToCopy.add(file);
        } else if (file.isDirectory()) {
            getInnerFiles(file.getId()).forEach(innerFile -> fillFilesToCopy(innerFile, filesToCopy));
        }
    }

    private boolean canBeEncoded(String filePath) {
        return CODING_EXTENSIONS.stream().anyMatch(filePath::endsWith);
    }

    private boolean canBeCopy(String filePath) {
        return CODING_EXTENSIONS.stream().noneMatch(filePath::endsWith);
    }
}

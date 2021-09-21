package service;

import dao.FileDao;
import model.File;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileService {

    private static final List<String> CODING_EXTENSIONS = Arrays.asList(".tif", ".mov", ".avi");
    private FileDao fileDao;

    public FileService(Connection connection) {
        fileDao = new FileDao(connection);
    }

    public List<File> getFilesToEncode() {
        List<File> filesToEncode = fileDao.getFilesToEncode(CODING_EXTENSIONS);
        List<File> result = new ArrayList<>();

        for (File encodeFile : filesToEncode) {
            File moreFarthestDirectory = getMoreFarthestDirectory(result, encodeFile);

            if (!containsPath(result, moreFarthestDirectory.getFullPath())) {
                result.add(moreFarthestDirectory);
            }
        }

        return result;
    }

    public List<File> getFilesToCopy() {
        List<File> filesToCopy = fileDao.getFilesToCopy(CODING_EXTENSIONS);
        List<File> result = new ArrayList<>();

        for (File copyFile : filesToCopy) {
            File moreFarthestDirectory = getMoreFarthestDirectoryToCopy(result, copyFile);

            if (!containsPath(result, moreFarthestDirectory.getFullPath())) {
                result.add(moreFarthestDirectory);
            }
        }

        return result;
    }

    private File getMoreFarthestDirectory(List<File> filesToEncode, File file) {
        File parentFile = fileDao.getFileById(file.getParentId());
        List<File> notFilesToEncode = getNotFilesToEncode(file, parentFile);

        if (!notFilesToEncode.isEmpty() || directoryContainsFilesNotToEncode(parentFile)) {
            return file;
        }

        return getMoreFarthestDirectory(filesToEncode, file, fileDao.getFileById(parentFile.getParentId()));
    }

    private File getMoreFarthestDirectory(List<File> filesToEncode, File file, File dir) {
        if (dir == null) {
            return file;
        }

        if (containsPath(filesToEncode, dir.getFullPath())) {
            return fileDao.getFileById(file.getParentId());
        }

        return getMoreFarthestDirectory(filesToEncode, fileDao.getFileById(file.getParentId()));
    }

    private File getMoreFarthestDirectoryToCopy(List<File> filesToCopy, File file) {
        File parentFile = fileDao.getFileById(file.getParentId());
        List<File> filesToEncode = fileDao.getFilesToCopy(CODING_EXTENSIONS);

        if (!filesToEncode.isEmpty() && directoryContainsFilesNotToCopy(parentFile)) {
            return file;
        }

        return getMoreFarthestDirectoryToCopy(filesToCopy, file, fileDao.getFileById(parentFile.getParentId()));
    }

    private File getMoreFarthestDirectoryToCopy(List<File> filesToCopy, File file, File dir) {
        if (dir == null) {
            return file;
        }

        if (containsPath(filesToCopy, dir.getFullPath())) {
            return fileDao.getFileById(file.getParentId());
        }

        return getMoreFarthestDirectoryToCopy(filesToCopy, fileDao.getFileById(file.getParentId()));
    }

    public List<File> getInnerFiles(long catalogId) {
        return fileDao.getInnerFiles(catalogId);
    }

    private List<File> getNotFilesToEncode(File inputFile, File directory) {
        return getInnerFiles(directory.getId()).stream()
                .filter(file -> CODING_EXTENSIONS.stream().noneMatch(extension -> file.getFullPath().endsWith(extension)) && !file.isDirectory() && !file.getFullPath().equals(inputFile.getFullPath()))
                .collect(Collectors.toList());
    }

    private boolean directoryContainsFilesNotToEncode(File dir) {
        List<File> allFiles = fileDao.getAllFiles(dir.getId());
        List<File> allDirs = fileDao.getAllDirs(dir.getId());

        if (allFiles.stream().anyMatch(file -> CODING_EXTENSIONS.stream().noneMatch(extension -> file.getFullPath().endsWith(extension)))) {
            return true;
        } else {
            for (File innerDir : allDirs) {
                if (directoryContainsFilesNotToEncode(innerDir)) {
                    return true;
                }
            }

            return false;
        }
    }

    private boolean directoryContainsFilesNotToCopy(File dir) {
        List<File> allFiles = fileDao.getAllFiles(dir.getId());
        List<File> allDirs = fileDao.getAllDirs(dir.getId());

        if (allFiles.stream().anyMatch(file -> CODING_EXTENSIONS.stream().anyMatch(extension -> file.getFullPath().endsWith(extension)))) {
            return true;
        } else {
            for (File innerDir : allDirs) {
                if (directoryContainsFilesNotToCopy(innerDir)) {
                    return true;
                }
            }

            return false;
        }
    }

    private boolean containsPath(List<File> files, String path) {
        return files.stream().anyMatch(file -> path.startsWith(file.getFullPath()));
    }
}

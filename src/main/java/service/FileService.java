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
        List<File> filesToEncode = new ArrayList<>();

        List<File> dirs = fileDao.getDirectoriesWithEncodingFiles(CODING_EXTENSIONS);

        for (File dir : dirs) {
            if (canEncodeDirectory(dir)) {
                addFilesToEncode(filesToEncode, findFarthestDitToEncode(filesToEncode, dir));
            } else {
                List<File> encodeFilesOfDir = fileDao.getEncodeFilesForDir(dir.getId(), CODING_EXTENSIONS);
                filesToEncode.addAll(encodeFilesOfDir);
            }
        }

        return filesToEncode;
    }

    public List<File> getFilesToCopy() {
        List<File> filesToCopy = new ArrayList<>();
        List<File> dirs = fileDao.getDirectoriesWithCopingFiles(CODING_EXTENSIONS);

        for (File dir : dirs) {
            if (canCopyDirectory(dir)) {
                addFilesToCopy(filesToCopy, findFarthestDirToCopy(filesToCopy, dir));
            } else {
                List<File> copyFilesOfDir = fileDao.getCopyFilesForDir(dir.getId(), CODING_EXTENSIONS);
                filesToCopy.addAll(copyFilesOfDir);
            }
        }

        return filesToCopy;
    }

    private boolean canEncodeDirectory(File dir) {
        if (dir == null) {
            return false;
        }

        List<File> filesToCopy = fileDao.getCopyFilesForDir(dir.getId(), CODING_EXTENSIONS);
        if (!filesToCopy.isEmpty()) {
            return false;
        }

        return fileDao.getFilesToCopy(CODING_EXTENSIONS).stream()
                .noneMatch(file -> file.getFullPath().startsWith(dir.getFullPath()));
    }

    private boolean canCopyDirectory(File dir) {
        if (dir == null || !fileDao.getEncodeFilesForDir(dir.getId(), CODING_EXTENSIONS).isEmpty()) {
            return false;
        }

        return fileDao.getFilesToEncode(CODING_EXTENSIONS).stream()
                .noneMatch(file -> file.getFullPath().startsWith(dir.getFullPath()));
    }

    private File findFarthestDitToEncode(List<File> alreadyEncoded, File dir) {
        File parentDir = fileDao.getFileById(dir.getParentId());

        if (canEncodeDirectory(parentDir) && alreadyEncoded.stream().noneMatch(file -> file.getFullPath().startsWith(parentDir.getFullPath()))) {
            return findFarthestDitToEncode(alreadyEncoded, parentDir);
        }

        return dir;
    }

    private File findFarthestDirToCopy(List<File> alreadyCopied, File dir) {
        File parentDir = fileDao.getFileById(dir.getParentId());

        if (canCopyDirectory(parentDir) && alreadyCopied.stream().noneMatch(file -> file.getFullPath().startsWith(parentDir.getFullPath()))) {
            return findFarthestDirToCopy(alreadyCopied, parentDir);
        }

        return dir;
    }

    private void addFilesToEncode(List<File> filesToEncode, File dirToEncode) {
        List<File> encodeFilesOfDir = fileDao.getEncodeFilesForDir(dirToEncode.getId(), CODING_EXTENSIONS);

        if (encodeFilesOfDir.size() > 1) {
            filesToEncode.add(dirToEncode);
        } else {
            filesToEncode.addAll(encodeFilesOfDir);
        }
    }

    private void addFilesToCopy(List<File> filesToCopy, File foundDirectory) {
        List<File> copyFilesOfDir = fileDao.getCopyFilesForDir(foundDirectory.getId(), CODING_EXTENSIONS);

        if (copyFilesOfDir.size() != 1) {
            filesToCopy.add(foundDirectory);
        } else {
            filesToCopy.addAll(copyFilesOfDir);
        }
    }
}

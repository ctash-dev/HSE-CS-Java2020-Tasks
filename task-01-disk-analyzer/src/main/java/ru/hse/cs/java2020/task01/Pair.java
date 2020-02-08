package ru.hse.cs.java2020.task01;

import java.nio.file.Path;

public class Pair {
    private final Long size;
    private final Path path;
    private Path rootPath;

    Pair(Long fileSize, Path filePath) {
        size = fileSize;
        path = filePath;
    }

    int compare(Pair other) {
        return (size > other.size ? -1 : (size.equals(other.size) ? 0 : 1));
    }

    public void setRootPath(Path rootFilePath) {
        rootPath = rootFilePath;
    }

    @Override
    public String toString() {
        return rootPath.relativize(path).toString();
    }
}

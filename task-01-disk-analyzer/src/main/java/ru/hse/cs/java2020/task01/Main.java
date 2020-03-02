package ru.hse.cs.java2020.task01;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;


public class Main {
    static final int COUNT_MAX = 10;
    static final int PERCENTAGE = 100;
    static final int TO_KB = 1024;
    static final long TO_S = 1000000000;

    static class Output {
        private File nameFile;
        private long countElements, fullSize;
        private List<File> topFiles;
        Output(File file) {
            nameFile = file;
            countElements = 0;
            fullSize = 0;
            topFiles = null;
            if (file.isDirectory()) {
                topFiles = new ArrayList<>();
            } else {
                fullSize = file.length();
                countElements = -1;
            }
        }
    }

    static class FileSizeComparator implements Comparator<File> {
        public int compare(File a, File b) {
            long aSize = a.length();
            long bSize = b.length();
            return Long.compare(bSize, aSize);
        }
    }

    private static Output getInfo(File toCheck) {
        Queue<File> inside = new PriorityQueue<>();
        Output ret = new Output(toCheck);
        Collections.addAll(inside, Objects.requireNonNull(toCheck.listFiles()));
        while (!inside.isEmpty()) {
            File curFile = inside.remove();
            if (curFile.isDirectory() && curFile.listFiles() != null) {
                    Collections.addAll(inside, Objects.requireNonNull(curFile.listFiles()));
            } else {
                if (ret.topFiles.size() < COUNT_MAX) {
                    ret.topFiles.add(curFile);
                } else if (ret.topFiles.get(COUNT_MAX - 1).length() < curFile.length()) {
                    ret.topFiles.set(COUNT_MAX - 1, curFile);
                    ret.topFiles.sort(new FileSizeComparator());
                }
                ret.fullSize += curFile.length();
                ret.countElements++;
            }
        }
        return ret;
    }

    static class OutputSizeComparator implements Comparator<Output> {
        public int compare(Output a, Output b) {
            long aSize = a.fullSize;
            long bSize = b.fullSize;
            return Long.compare(bSize, aSize);
        }
    }

    public static void main(String[] args) {
        File rootLib;
        if (args.length == 1) {
            rootLib = new File(args[0]);
            if (!rootLib.isDirectory() || !rootLib.exists()) {
                System.err.println("Incorrect path");
                return;
            }
        } else if (args.length == 0) {
            System.err.println("No input arguments");
            return;
        } else {
            System.err.println("Incorrect input");
            return;
        }
        Queue<File> inside = new PriorityQueue<>();
        List<Output> resultList = new ArrayList<>();
        final long startTime = System.nanoTime();
        Collections.addAll(inside, Objects.requireNonNull(rootLib.listFiles()));
        Output rootFolder = getInfo(rootLib);
        long fullSize = rootFolder.fullSize;
        List<File> topFiles = rootFolder.topFiles;
        topFiles.sort(new FileSizeComparator());
        while (!inside.isEmpty()) {
            File curFile = inside.remove();
            if (curFile.isDirectory()) {
                File[] insideFiles = curFile.listFiles();
                if (insideFiles != null) {
                    resultList.add(getInfo(curFile));
                }
            } else {
                resultList.add(new Output(curFile));
            }
        }
        resultList.sort(new OutputSizeComparator());
        System.out.printf(
                "%-10s %-100s %-22s %-15s %-15s"
                + " %n",
                "Number",
                "File or folder",
                "Size (KB)",
                "Size (%)",
                "Elements"
        );
        int number = 0;
        for (Output ex : resultList) {
            number++;
            String countElements = ex.nameFile.isDirectory() ? Long.toString(ex.countElements) : "";
            System.out.printf(
                    "%-10d %-100s %-22d %-15f %-15s %n",
                    number,
                    ex.nameFile.getPath().substring(args[0].length()),
                    ex.fullSize / TO_KB, (double) ex.fullSize / fullSize * PERCENTAGE, countElements
            );
        }
        System.out.println("\n\n Top of biggest files:");
        System.out.printf(
                "%-10s %-100s %-22s %n",
                "Number",
                "Full path",
                "Size (KB)"
        );
        number = 0;
        for (File file : topFiles) {
            number++;
            System.out.printf(
                    "%-10d %-100s %-15d %n",
                    number,
                    file.getPath(),
                    file.length() / TO_KB
            );
        }
        long endTime = System.nanoTime();
        System.out.println("Time: " + (float)(endTime - startTime) / TO_S + "s\n");
    }
}

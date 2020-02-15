package ru.hse.cs.java2020.task01;

import java.text.SimpleDateFormat;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Collections;

public class Main {
    private static File rootLib;
    private static int countMax;

    static class Returning {
        private File nameFile;
        private long countFiles, countFolders, totalSize;
        private List<File> biggestFiles;

        Returning() {
            nameFile = null;
            countFiles = 0;
            countFolders = 0;
            totalSize = 0;
            biggestFiles = new ArrayList<>();
        }
        Returning(File path) {
            nameFile = path;
            countFiles = 0;
            countFolders = 0;
            totalSize = 0;
            biggestFiles = new ArrayList<>();
        }
        Returning(long size, File path) {
            nameFile = path;
            countFiles = -1;
            countFolders = -1;
            totalSize = size;
            biggestFiles = null;
        }
    }

    static class FileSizeComparator implements Comparator<File> {
        public int compare(File a, File b) {
            long aSize = a.length();
            long bSize = b.length();
            if (aSize == bSize) {
                return 0;
            } else {
                return Long.compare(aSize, bSize) * -1;
            }
        }
    }
    static class FinalSizeComparator implements Comparator<Returning> {
        public int compare(Returning a, Returning b) {
            long aSize = a.totalSize;
            long bSize = b.totalSize;
            if (aSize == bSize) {
                return 0;
            } else {
                return Long.compare(aSize, bSize) * -1;
            }
        }
    }


    private static int setPathAndNumMax(String[] args) {
        if (args.length == 0) {
            System.err.println("You have not passed pass");
            return 1;
        } else if (args.length == 1) {
            rootLib = new File(args[0]);
        } else {
            rootLib = new File(args[0]);
            try {
                countMax = Integer.parseInt(args[1]);
                if (countMax < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                System.err.println("Incorrect number of biggest files");
                return 1;
            }
        }

        if (!rootLib.isDirectory() || !rootLib.exists()) {
            System.err.println("Incorrect path");
            return 1;
        }
        return 0;
    }

    private static Returning getContaining(File toCheck) {
        Queue<File> containing = new PriorityQueue<>();
        Returning ret = new Returning(toCheck);

        Collections.addAll(containing, toCheck.listFiles());

        while (!containing.isEmpty()) {
            File currentFile = containing.remove();
            if (currentFile.isDirectory()) {
                if (currentFile.listFiles() != null) {
                    ret.countFolders++;
                    Collections.addAll(containing, currentFile.listFiles());
                }
            } else {
                // Updating list of big files
                if (countMax > 0) {
                    if (ret.biggestFiles.size() < countMax) {
                        ret.biggestFiles.add(currentFile);
                    } else if (ret.biggestFiles.get(countMax - 1).length() < currentFile.length()) {
                        ret.biggestFiles.set(countMax - 1, currentFile);
                        ret.biggestFiles.sort(new FileSizeComparator());
                    }
                }
                // Updating count of files and size of folder
                ret.totalSize += currentFile.length();
                ret.countFiles++;
            }
        }

        return ret;
    }

    public static void main(String[] args) {
        Queue<File> containing = new PriorityQueue<>();
        List<Returning> resultList = new ArrayList<>();
        List<File> biggestFiles = new ArrayList<>();
        final long startTime = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss z");

        if (setPathAndNumMax(args) != 0) {
            return;
        }

        Collections.addAll(containing, rootLib.listFiles());

        long totalSize = getContaining(rootLib).totalSize;

        while (!containing.isEmpty()) {
            File currentFile = containing.remove();

            if (currentFile.isDirectory()) {
                File[] tmp = currentFile.listFiles();
                if (tmp != null) {
                    resultList.add(getContaining(currentFile));
                }
            } else {
                if (countMax > 0) {
                    if (biggestFiles.size() < countMax) {
                        biggestFiles.add(currentFile);
                    } else if (biggestFiles.get(countMax - 1).length() < currentFile.length()) {
                        biggestFiles.set(countMax - 1, currentFile);
                        biggestFiles.sort(new FileSizeComparator());
                    }
                }
                resultList.add(new Returning(currentFile.length(), currentFile));
            }

        }

        resultList.sort(new FinalSizeComparator());
        final int kbdiv = 1024;
        final int hundred = 100;
        final int thouthand = 1000;
        System.out.printf("%-10s | %-60s | %-22s | %-15s | %-15s | %-15s | %n",
                "Number", "Path", "Total size", "Percent size", "Count files", "Count folders");
        for (int i = 0; i < resultList.size(); i++) {
            Returning ex = resultList.get(i);

            if (ex.nameFile.isDirectory()) {
                System.out.printf("%-10d | %-60s | %-15d kbytes | %-15f | %-15d | %-15d | %n", i + 1,
                        ex.nameFile.getPath().substring(args[0].length() - 1, ex.nameFile.getPath().length() - 1),
                        ex.totalSize / kbdiv, (double) ex.totalSize / totalSize * hundred, ex.countFiles, ex.countFolders);
                if (ex.biggestFiles != null) {
                    biggestFiles.addAll(ex.biggestFiles);
                }
            } else {
                System.out.printf("%-10d | %-60s | %-15d kbytes | %-15f | %-15s | %-15s | %n", i + 1,
                        ex.nameFile.getPath().substring(args[0].length() - 1, ex.nameFile.getPath().length() - 1),
                        ex.totalSize / kbdiv, (double) ex.totalSize / totalSize * hundred, "that's file", "that's file");
            }
        }
        biggestFiles.sort(new FileSizeComparator());

        if (countMax > 0) {
            System.out.println("\n\nBiggest files:");
            System.out.printf("%-10s | %-160s | %-22s | %n", "Number", "Path", "Size");
        }
        for (int i = 0; i < countMax; i++) {
            System.out.printf("%-10d | %-160s | %-15d kbytes | %n",
                    i + 1, biggestFiles.get(i).getPath(), biggestFiles.get(i).length() / kbdiv);
        }

        System.out.println("Total folder size: " + totalSize / kbdiv + " kbytes\nWork time: "
                + ((System.currentTimeMillis() - startTime) / thouthand)  + "c "
                + ((System.currentTimeMillis() - startTime) % thouthand) + "ms");
    }
}

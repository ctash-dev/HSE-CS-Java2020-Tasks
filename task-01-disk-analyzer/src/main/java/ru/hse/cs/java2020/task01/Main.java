package ru.hse.cs.java2020.task01;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Collections;

public class Main {
    private static File rootLib;
    private static int countMax = 5;

    static class Returning {
        private File nameFile;
        private long Filescnt, Folderscnt, totalSize;
        private List<File> TopSize;

        Returning() {
            nameFile = null;
            Filescnt = 0;
            Folderscnt = 0;
            totalSize = 0;
            TopSize = new ArrayList<>();
        }

        Returning(File f) {
            nameFile = f;
            Filescnt = 0;
            Folderscnt = 0;
            totalSize = 0;
            TopSize = new ArrayList<>();
        }

        Returning(long s, File f) {
            nameFile = f;
            Filescnt = -1;
            Folderscnt = -1;
            totalSize = s;
            TopSize = null;
        }
    }

    static class FileSizeComparator implements Comparator<File> {
        public int compare(File f1, File f2) {
            if (f1.length() != f2.length()) {
                return Long.compare(f1.length(), f2.length()) * -1;
            }
            return 0;
        }
    }
    static class FinalSizeComparator implements Comparator<Returning> {
        public int compare(Returning r1, Returning r2) {
            if (r1.totalSize != r2.totalSize) {
                return Long.compare(r1.totalSize, r2.totalSize) * -1;
            }
            return 0;
        }
    }

    private static int setPathAndNumMax(String[] args) {
        if (args.length == 0) {
            System.err.println("Too little amount of args");
            return 1;
        }
        rootLib = new File(args[0]);
        if (args.length > 1) {
            try {
                countMax = Integer.parseInt(args[1]);
                if (Integer.parseInt(args[1]) < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                System.err.println("Number Format Exception caught");
                return 1;
            }
        }

        if (rootLib.isDirectory() && rootLib.exists()) {
            return 0;
        }
        System.err.println("The path is incorrect");
        return 1;
    }

    private static Returning getContaining(File f) {
        Queue<File> allFiles = new PriorityQueue<>();
        Returning r = new Returning(f);

        Collections.addAll(allFiles, f.listFiles());

        while (!allFiles.isEmpty()) {
            File fCur = allFiles.remove();

            if (!fCur.isDirectory()) {
                if (countMax > 0) {
                    if (r.TopSize.size() < countMax) {
                        r.TopSize.add(fCur);
                    } else if (r.TopSize.get(countMax - 1).length() < fCur.length()) {
                        r.TopSize.set(countMax - 1, fCur);
                        r.TopSize.sort(new FileSizeComparator());
                    }
                }
                r.totalSize += fCur.length();
                r.Filescnt++;
            } else {
                if (fCur.listFiles() != null) {
                    r.Folderscnt++;
                    Collections.addAll(allFiles, fCur.listFiles());
                }
            }
        }
        return r;
    }



    public static void main(String[] args) {
        final long startTime = System.currentTimeMillis();
        Queue<File> allFiles = new PriorityQueue<>();
        List<File> biggestFiles = new ArrayList<>();
        List<Returning> resFiles = new ArrayList<>();

        if (setPathAndNumMax(args) != 0) {
            return;
        }

        Collections.addAll(allFiles, rootLib.listFiles());

        long totalSize = getContaining(rootLib).totalSize;

        while (!allFiles.isEmpty()) {
            File currentFile = allFiles.remove();

            if (currentFile.isDirectory()) {
                File[] tmp = currentFile.listFiles();
                if (tmp != null) {
                    resFiles.add(getContaining(currentFile));
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
                resFiles.add(new Returning(currentFile.length(), currentFile));
            }

        }

        resFiles.sort(new FinalSizeComparator());
        final int thousand24 = 1024;
        final int hundred = 100;
        final int thousand = 1000;


        System.out.printf("%7s | %-50s | %14s | %15s | %12s | %14s | %12s %n",
                "Number", "Path", "Size (kbytes)", "Size (percents)", "Files count", "Folders count", "All count");
        for (int i = 0; i < resFiles.size(); i++) {
            Returning r = resFiles.get(i);

            if (r.nameFile.isDirectory()) {
                System.out.printf("%7d | %-50s | %7d kbytes | %13f %% | %12d | %14d | %12d %n", i + 1,
                        r.nameFile.getPath().substring(args[0].length(), r.nameFile.getPath().length()),
                        r.totalSize / thousand24, (double) r.totalSize / totalSize * hundred,
                        r.Filescnt, r.Folderscnt, r.Filescnt + r.Folderscnt);
                if (r.TopSize != null) {
                    biggestFiles.addAll(r.TopSize);
                }
            } else {
                System.out.printf("%7d | %-50s | %7d kbytes | %13f %% | %12s | %14s | %12s %n", i + 1,
                        r.nameFile.getPath().substring(args[0].length(), r.nameFile.getPath().length()),
                        r.totalSize / thousand24, (double) r.totalSize / totalSize * hundred, "-", "-", "-");
            }
        }
        biggestFiles.sort(new FileSizeComparator());

        if (resFiles.size() < countMax) {
            countMax = resFiles.size();
        }
        if (countMax > 0) {
            System.out.println("\n");
            System.out.printf("%7s | %14s | %-120s %n", "Number", "Size", "Path");
            for (int i = 0; i < countMax; i++) {
                System.out.printf("%7d | %7d kbytes | %-120s %n",
                        i + 1, biggestFiles.get(i).length() / thousand24, biggestFiles.get(i).getPath());
            }
        }

        System.out.println("\n");
        System.out.println("Total folder size: " + totalSize / thousand24 + " kbytes");
        System.out.println("Total time: " + ((System.currentTimeMillis() - startTime) / thousand)  + "c "
                + ((System.currentTimeMillis() - startTime) % thousand) + "ms");
    }
}
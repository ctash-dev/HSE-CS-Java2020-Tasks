package ru.hse.cs.java2020.task01;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        final long startTime = System.currentTimeMillis();
        if (args.length == 0) {
            System.out.println("Expected 1 argument, 0 provided.\n");
            return;
        }

        File file = new File(args[0]);
        if (!file.exists()) {
            return;
        }
        System.out.println(file);
        Folder folder = new Folder(file);
        folder.printSubFolders();
        final Integer countTop = 7;
        folder.printTop(countTop);

        final long endTime = System.currentTimeMillis();
        System.out.printf("Total execution time: %d ms", endTime - startTime);
    }
}

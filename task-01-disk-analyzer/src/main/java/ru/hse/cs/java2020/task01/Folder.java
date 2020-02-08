package ru.hse.cs.java2020.task01;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

public class Folder {
    private final String name;
    private final Path fullPath;
    private Long size;
    private Long itemsCount;
    private String percent;
    private ArrayList<Folder> subFolders;
    private final Boolean isDirectory;
    private final Integer bytes = 1000;

    Folder(File file) {
        name = file.getName();
        fullPath = file.toPath();
        size = file.length();
        itemsCount = 0L;
        isDirectory = file.isDirectory();
        subFolders = new ArrayList<>();
        if (!isDirectory || file.listFiles() == null) {
            return;
        }

        for (File item : Objects.requireNonNull(file.listFiles())) {
            if (Files.isSymbolicLink(Paths.get(item.getPath()))) {
                continue;
            }
            Folder elem = new Folder(item);
            size += elem.size;
            itemsCount += (elem.itemsCount != 0L) ? elem.itemsCount : 1;
            subFolders.add(elem);
        }
        DecimalFormat formatter = new DecimalFormat("##0.00%");
        for (Folder elem : subFolders) {
            elem.percent = formatter.format((double) elem.size / size);
        }
        subFolders.sort(Folder::compare);
    }

    private ArrayList<Pair> getTop() {
        ArrayList<Folder> allElements = getAllElements();
        ArrayList<Pair> top = new ArrayList<>();
        for (Folder elem : allElements) {
            top.add(new Pair(elem.getFolderSize(), elem.getFullPath()));
        }
        top.sort(Pair::compare);
        return top;
    }

    private ArrayList<Folder> getAllElements() {
        ArrayList<Folder> elements = new ArrayList<>();
        for (Folder item : subFolders) {
            if (item.isDirectory) {
                elements.addAll(item.getAllElements());
            } else {
                elements.add(item);
            }
        }
        return elements;
    }

    private int compare(Folder other) {
        return (size > other.size ? -1 : (size.equals(other.size) ? 0 : 1));
    }

    private Long[] getMaxLengths() {
        final int fieldsCount = 5;
        Long[] lengths = {0L, 0L, 0L, 0L, 0L};
        lengths[0] = (long) Integer.toString(subFolders.size()).length();
        for (Folder child : subFolders) {
            lengths[1] = Math.max(lengths[1], child.name.length());
            lengths[2] = Math.max(lengths[2], Long.toString((child.size / bytes)).length());
            lengths[fieldsCount - 2] = Math.max(lengths[fieldsCount - 2], child.percent.length());
            lengths[fieldsCount - 1] = Math.max(lengths[fieldsCount - 1], child.itemsCount.toString().length());
        }
        ++lengths[1];
        lengths[2] = Math.max(lengths[2], Long.toString((size / bytes)).length());
        return lengths;
    }

    private void print(Long[] maxLengths, Long index) {
        String[] fields = {index.toString(), name, Long.toString(size / bytes), percent, itemsCount.toString()};
        String[] addedStrings = {". ", (isDirectory ? "/| " : "| "), " Kb| ", "| ", " items"};
        for (int i = 0; i < (isDirectory ? fields.length : fields.length - 1); ++i) {
            System.out.print(String.format("%1$" + (maxLengths[i] - ((i == 1 && isDirectory) ? 1 : 0)) + "s", fields[i]) + addedStrings[i]);
        }
        System.out.println();
    }

    public Path getFullPath() {
        return fullPath;
    }

    public Long getFolderSize() {
        return size;
    }

    public void printSubFolders() {
        Long index = 1L;
        Long[] lengths = getMaxLengths();
        for (Folder f : subFolders) {
            f.print(lengths, index);
            ++index;
        }
        System.out.print(String.format("%1$" + (lengths[0] + 2 + lengths[1] + 2) + "s", "TOTAL| "));
        System.out.println(String.format("%1$" + lengths[2] + "s", size / bytes) + " Kb");
    }

    public void printTop(Integer count) {
        System.out.println("LARGEST FILES:");
        ArrayList<Pair> top = getTop();
        int index = 1;
        for (Pair elem : top) {
            elem.setRootPath(fullPath);
            System.out.printf("%3d. ", index);
            System.out.println(elem);
            if (count.equals(index)) {
                break;
            }
            ++index;
        }
    }
}

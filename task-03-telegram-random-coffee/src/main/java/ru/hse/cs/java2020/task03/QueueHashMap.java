package ru.hse.cs.java2020.task03;

public class QueueHashMap {

    public QueueHashMap(String newKey, long newId) {
        this.key = newKey;
        this.id = newId;
    }

    public String getKey() {
        return key;
    }

    public long getId() {
        return id;
    }

    private String key;

    private long id;
}

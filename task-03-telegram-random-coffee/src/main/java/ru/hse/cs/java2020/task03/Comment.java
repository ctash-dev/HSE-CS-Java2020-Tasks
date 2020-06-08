package ru.hse.cs.java2020.task03;

public class Comment {
    public Comment(String givenAuthor, String givenText) {
        this.author = givenAuthor;
        this.text = givenText;
    }

    private final String author;
    private final String text;

    public String getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }
}

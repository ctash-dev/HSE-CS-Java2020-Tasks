package ru.hse.cs.java2020.task03;

public class Comment {
    public Comment(String givenAuthor, String givenText) {
        this.author = givenAuthor;
        this.text = givenText;
    }

    public String Author() {
        return author;
    }

    public String Text() {
        return text;
    }

    private String author;
    private String text;
}

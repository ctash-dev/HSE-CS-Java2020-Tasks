package ru.hse.cs.java2020.task03;

import java.util.ArrayList;
import java.util.Optional;

public class Task {
    private final ArrayList<String> followers;
    private final ArrayList<Comment> comments;
    private Optional<String> assignedTo;
    private String name;
    private String description;
    private String author;

    Task() {
        followers = new ArrayList<>();
        comments = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String newDescription) {
        this.description = newDescription;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String newAuthor) {
        this.author = newAuthor;
    }

    void addComment(Comment info) {
        comments.add(info);
    }

    void addFollower(String follower) {
        followers.add(follower);
    }

    public ArrayList<String> getFollowers() {
        return followers;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public Optional<String> getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String newAssignedTo) {
        this.assignedTo = Optional.ofNullable(newAssignedTo);
    }
}

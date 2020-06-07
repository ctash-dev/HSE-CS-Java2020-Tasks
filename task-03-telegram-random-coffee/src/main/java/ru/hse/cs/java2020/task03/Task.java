package ru.hse.cs.java2020.task03;

import java.util.ArrayList;
import java.util.Optional;

public class Task {
    Task() {
        followers = new ArrayList<>();
        comments = new ArrayList<>();
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public void setDescription(String newDescription) {
        this.description = newDescription;
    }

    public void setAuthor(String newAuthor) {
        this.author = newAuthor;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
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

    private Optional<String> assignedTo;
    private String name;
    private String description;
    private String author;
    private ArrayList<String> followers;
    private ArrayList<Comment> comments;
}

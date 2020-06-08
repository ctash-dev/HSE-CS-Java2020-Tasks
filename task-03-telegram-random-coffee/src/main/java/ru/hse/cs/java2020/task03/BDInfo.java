package ru.hse.cs.java2020.task03;

public class BDInfo {
    private final String token;
    private final String org;
    private final String username;

    public BDInfo(String tkn, String orgId, String name) {
        this.token = tkn;
        this.org = orgId;
        this.username = name;
    }

    public String getToken() {
        return token;
    }

    public String getOrg() {
        return org;
    }

    public String getUsername() {
        return username;
    }
}

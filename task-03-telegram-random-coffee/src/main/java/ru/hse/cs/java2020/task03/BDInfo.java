package ru.hse.cs.java2020.task03;

import java.util.Objects;

public class BDInfo {
    public BDInfo(String tkn, String orgId, String name) {
        this.token = tkn;
        this.org = orgId;
        this.username = name;
    }

    public String Token() {
        return token;
    }
    public String Org() {
        return org;
    }

    public String Username() {
        return username;
    }

    private String token;
    private String org;
    private String username;
}

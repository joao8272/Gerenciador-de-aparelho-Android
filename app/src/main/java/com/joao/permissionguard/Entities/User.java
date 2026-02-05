package com.joao.permissionguard.Entities;

public class User {

    private String username;
    private String profile;

    public User(String username, String profile) {
        this.username = username;
        this.profile = profile;
    }

    public String getUsername() {
        return username;
    }

    public String getProfile() {
        return profile;
    }
}


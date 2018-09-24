package com.google.firebase.udacity.friendlychat.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jesús López Corominas
 */
public class User {

    private String name;
    private String email;

    private Map<String, Boolean> devices;

    public User() {
        this(null, null, new HashMap<>());
    }

    public User(String name, String email, Map<String, Boolean> devices) {
        this.name = name;
        this.email = email;

        this.devices = devices;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, Boolean> getDevices() {
        return devices;
    }

    public void setDevices(Map<String, Boolean> devices) {
        this.devices = devices;
    }
}

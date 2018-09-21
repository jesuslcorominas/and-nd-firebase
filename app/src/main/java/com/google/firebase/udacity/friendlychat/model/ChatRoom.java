package com.google.firebase.udacity.friendlychat.model;

/**
 * @author Jesús López Corominas
 */
public class ChatRoom {

    private String name;

    public ChatRoom() {
    }

    public ChatRoom(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

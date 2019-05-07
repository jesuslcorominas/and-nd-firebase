package com.google.firebase.udacity.friendlychat.model;

/**
 * @author Jesús López Corominas
 */
public class ChatRoom {

    private String creatorUserUid;
    private String name;

    public ChatRoom() {
    }

    public ChatRoom(String creatorUserUid, String name) {
        this.creatorUserUid = creatorUserUid;
        this.name = name;
    }

    public String getCreatorUserUid() {
        return creatorUserUid;
    }

    public void setCreatorUserUid(String creatorUserUid) {
        this.creatorUserUid = creatorUserUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

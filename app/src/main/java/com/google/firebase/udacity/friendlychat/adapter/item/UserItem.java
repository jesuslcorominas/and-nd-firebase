package com.google.firebase.udacity.friendlychat.adapter.item;

import com.google.firebase.udacity.friendlychat.model.User;

/**
 * @author Jesús López Corominas
 */
public class UserItem {

    private String uid;
    private User user;

    public UserItem() {
    }

    public UserItem(String uid, User user) {
        this.uid = uid;
        this.user = user;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

package com.google.firebase.udacity.friendlychat.adapter.item;

import com.google.firebase.udacity.friendlychat.model.ChatRoom;

/**
 * @author Jesús López Corominas
 */
public class ChatRoomItem {

    private String uid;
    private ChatRoom chatRoom;

    public ChatRoomItem() {
    }

    public ChatRoomItem(String uid, ChatRoom chatRoom) {
        this.uid = uid;
        this.chatRoom = chatRoom;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
}

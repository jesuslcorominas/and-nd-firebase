package com.google.firebase.udacity.friendlychat.adapter.item;

import com.google.firebase.udacity.friendlychat.model.FriendlyMessage;

/**
 * @author Jesús López Corominas
 */
public class FriendlyMessageItem {

    private FriendlyMessage friendlyMessage;
    private boolean incoming;

    public FriendlyMessageItem() {
    }

    public FriendlyMessageItem(FriendlyMessage friendlyMessage, boolean incoming) {
        this.friendlyMessage = friendlyMessage;
        this.incoming = incoming;
    }

    public FriendlyMessage getFriendlyMessage() {
        return friendlyMessage;
    }

    public void setFriendlyMessage(FriendlyMessage friendlyMessage) {
        this.friendlyMessage = friendlyMessage;
    }

    public boolean getIncoming() {
        return incoming;
    }

    public void setIncoming(boolean incoming) {
        this.incoming = incoming;
    }
}

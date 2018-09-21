package com.google.firebase.udacity.friendlychat.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.udacity.friendlychat.R;
import com.google.firebase.udacity.friendlychat.adapter.item.ChatRoomItem;
import com.google.firebase.udacity.friendlychat.model.ChatRoom;

import java.util.List;

public class ChatsAdapter extends ArrayAdapter<ChatRoomItem> {
    public ChatsAdapter(Context context, int resource, List<ChatRoomItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_chat, parent, false);
        }

        TextView textViewName = convertView.findViewById(R.id.item_chat_textView_name);

        ChatRoomItem item = getItem(position);
        ChatRoom chatRoom = item.getChatRoom();

        textViewName.setText(chatRoom.getName());

        return convertView;
    }
}

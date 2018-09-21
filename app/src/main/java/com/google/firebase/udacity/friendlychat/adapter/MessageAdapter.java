package com.google.firebase.udacity.friendlychat.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.udacity.friendlychat.adapter.item.FriendlyMessageItem;
import com.google.firebase.udacity.friendlychat.R;
import com.google.firebase.udacity.friendlychat.model.FriendlyMessage;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<FriendlyMessageItem> {
    public MessageAdapter(Context context, int resource, List<FriendlyMessageItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message, parent, false);
        }

        LinearLayout linearLayoutIncoming = convertView.findViewById(R.id.item_message_incoming);
        TextView messageTextViewIncoming = convertView.findViewById(R.id.item_message_textView_incoming_text);
        TextView authorTextViewIncoming = convertView.findViewById(R.id.item_message_textView_name);

        LinearLayout linearLayoutOutgoing = convertView.findViewById(R.id.item_message_outgoing);
        TextView messageTextViewOutgoing = convertView.findViewById(R.id.item_message_textView_outgoing_text);
        TextView authorTextViewOutgoing = convertView.findViewById(R.id.item_message_outgoing_name);

        FriendlyMessageItem item = getItem(position);
        FriendlyMessage message = item.getFriendlyMessage();

        boolean isIncoming = item.getIncoming();
        if (isIncoming) {
            messageTextViewIncoming.setText(message.getText());

            authorTextViewIncoming.setText(message.getName());

            linearLayoutIncoming.setVisibility(View.VISIBLE);
            linearLayoutOutgoing.setVisibility(View.GONE);
        } else {
            messageTextViewOutgoing.setText(message.getText());
            authorTextViewOutgoing.setText(message.getName());

            linearLayoutOutgoing.setVisibility(View.VISIBLE);
            linearLayoutIncoming.setVisibility(View.GONE);
        }

        return convertView;
    }
}

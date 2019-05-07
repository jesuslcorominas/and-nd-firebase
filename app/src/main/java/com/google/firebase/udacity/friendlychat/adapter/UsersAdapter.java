package com.google.firebase.udacity.friendlychat.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.udacity.friendlychat.R;
import com.google.firebase.udacity.friendlychat.adapter.item.UserItem;
import com.google.firebase.udacity.friendlychat.model.User;

import java.util.List;

public class UsersAdapter extends ArrayAdapter<UserItem> {
    public UsersAdapter(Context context, int resource, List<UserItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_user, parent, false);
        }

        TextView textViewName = convertView.findViewById(R.id.item_user_textView_name);
        TextView textViewEmail = convertView.findViewById(R.id.item_user_textView_email);

        UserItem item = getItem(position);
        User user = item.getUser();

        textViewName.setText(user.getName());
        textViewEmail.setText(user.getEmail());

        return convertView;
    }
}

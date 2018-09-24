package com.google.firebase.udacity.friendlychat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.udacity.friendlychat.adapter.ChatsAdapter;
import com.google.firebase.udacity.friendlychat.adapter.item.ChatRoomItem;
import com.google.firebase.udacity.friendlychat.model.ChatRoom;
import com.google.firebase.udacity.friendlychat.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import utils.Keys;

public class ChatsActivity extends AppCompatActivity {

    private static final String TAG = "ChatsActivity";

    public static final String ANONYMOUS = "anonymous";

    public static final int RC_SIGN_IN = 1;

    private ChatsAdapter mChatsAdapter;

    private ListView mChatsListView;
    private ProgressBar mProgressBar;

    private FloatingActionButton mFloatingActionButton;

    private String mUsername;
    private String mUserUid;

    // Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mChatsDatabaseReference;

    private ChildEventListener mChatsEventListener;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        mUsername = ANONYMOUS;

        // Initialize Firebase components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        mChatsDatabaseReference = mFirebaseDatabase.getReference().child("chats");

        // Initialize references to views
        mProgressBar = findViewById(R.id.progressBar);
        mChatsListView = findViewById(R.id.chatsListView);
        mFloatingActionButton = findViewById(R.id.fab);

        // Initialize message ListView and its adapter
        List<ChatRoomItem> items = new ArrayList<>();
        mChatsAdapter = new ChatsAdapter(this, R.layout.item_chat, items);
        mChatsListView.setAdapter(mChatsAdapter);
        mChatsListView.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent = new Intent(ChatsActivity.this, MainActivity.class);

            ChatRoomItem item = mChatsAdapter.getItem(i);
            intent.putExtra(Keys.EXTRA_CHAT_ROOM_UID, item.getUid());
            intent.putExtra(Keys.EXTRA_CHAT_ROOM_NAME, item.getChatRoom().getName());
            startActivity(intent);
        });

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mFloatingActionButton.setOnClickListener(view -> {
            showInputDialog();
        });

        mAuthStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                onSignedInInitialize(user);
            } else {
                // User is signed out
                onSignedOutCleanup();
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false)
                                .setAvailableProviders(Arrays.asList(
                                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                                        new AuthUI.IdpConfig.EmailBuilder().build()))
                                .build(),
                        RC_SIGN_IN);
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }

        mChatsAdapter.clear();
        detachDatabaseReadListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onSignedInInitialize(FirebaseUser firebaseUser) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
            String deviceToken = instanceIdResult.getToken();

            User user = new User(firebaseUser.getDisplayName(), firebaseUser.getEmail(), new HashMap<>());
            user.getDevices().put(deviceToken, true);

            mFirebaseDatabase.getReference("users").child(firebaseUser.getUid()).setValue(user);
        });

        mUserUid = firebaseUser.getUid();
        mUsername = firebaseUser.getDisplayName();

        attachDatabaseReadListener();
    }

    private void onSignedOutCleanup() {
        mUserUid = ANONYMOUS;
        mUsername = ANONYMOUS;

        mChatsAdapter.clear();
        detachDatabaseReadListener();
    }


    private void attachDatabaseReadListener() {
        if (mChatsEventListener == null) {
            mChatsEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                    ChatRoomItem item = new ChatRoomItem(dataSnapshot.getKey(), chatRoom);
                    mChatsAdapter.add(item);
                }

                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                }

                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }

                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                }

                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };

            mChatsDatabaseReference.addChildEventListener(mChatsEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChatsEventListener != null) {
            mChatsDatabaseReference.removeEventListener(mChatsEventListener);

            mChatsEventListener = null;
        }
    }

    private void showInputDialog() {
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_input,
                findViewById(R.id.activity_chats_content), false);

        final EditText input = viewInflated.findViewById(R.id.input);

        new AlertDialog.Builder(this)
                .setTitle("Title")
                .setView(viewInflated)
                .setPositiveButton(android.R.string.ok, (dialog, wich) -> {
                    dialog.dismiss();

                    ChatRoom chatRoom = new ChatRoom(input.getText().toString());
                    mChatsDatabaseReference.push().setValue(chatRoom);
                })
                .setNegativeButton(android.R.string.cancel, (dialog, wich) -> dialog.cancel())
                .show();
    }


}

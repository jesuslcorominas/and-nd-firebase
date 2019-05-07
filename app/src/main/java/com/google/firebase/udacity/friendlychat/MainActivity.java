/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.udacity.friendlychat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.udacity.friendlychat.adapter.MessageAdapter;
import com.google.firebase.udacity.friendlychat.adapter.item.FriendlyMessageItem;
import com.google.firebase.udacity.friendlychat.model.ChatRoom;
import com.google.firebase.udacity.friendlychat.model.FriendlyMessage;
import com.google.firebase.udacity.friendlychat.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.Keys;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

    public static final int RC_SIGN_IN = 1;

    private TextView textViewMembers;
    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private Button mSendButton;

    Map<String, User> members;

    private String mUsername;
    private String mUserUid;

    private String mChatRoomUid;

    // Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;

    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mMessagesEventListener;

    private DatabaseReference mMembersDatabaseReference;
    private ChildEventListener mMembersEventListener;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    String dataTitle;
    String dataMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChatRoomUid = getIntent().getExtras().getString(Keys.EXTRA_CHAT_ROOM_UID);

        // Initialize Firebase components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("messages").child(mChatRoomUid);
        mMembersDatabaseReference = mFirebaseDatabase.getReference().child("members").child(mChatRoomUid);

        mUsername = ANONYMOUS;

        setTitleFromChatUid(mChatRoomUid);

        // Initialize references to views
        initView();

        // Initialize message ListView and its adapter
        List<FriendlyMessageItem> items = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_message, items);
        mMessageListView.setAdapter(mMessageAdapter);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(view -> {
            FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), mUsername, mUserUid);
            mMessagesDatabaseReference.push().setValue(friendlyMessage);

            // Clear input box
            mMessageEditText.setText("");
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

        mMessageAdapter.clear();
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

            mMembersDatabaseReference.child(firebaseUser.getUid()).setValue(true);
        });

        mUserUid = firebaseUser.getUid();
        mUsername = firebaseUser.getDisplayName();

        attachDatabaseReadListener();
    }

    private void onSignedOutCleanup() {
        mUserUid = ANONYMOUS;
        mUsername = ANONYMOUS;

        mMessageAdapter.clear();
        detachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if (mMessagesEventListener == null) {
            mMessagesEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                    FriendlyMessageItem item = new FriendlyMessageItem(friendlyMessage, !friendlyMessage.getUserUid().equals(mUserUid));
                    mMessageAdapter.add(item);
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

            mMessagesDatabaseReference.addChildEventListener(mMessagesEventListener);
        }

        if (mMembersEventListener == null) {
            mMembersEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if (TextUtils.isEmpty(dataSnapshot.getKey())) {
                        return;
                    }

                    mFirebaseDatabase.getReference("users").child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            if (members == null) {
                                members = new HashMap<>();
                            }

                            members.put(dataSnapshot.getKey(), user);

                            String emails = "";
                            for (Map.Entry<String, User> mapEntry : members.entrySet()) {
                                emails = emails + mapEntry.getValue().getEmail() + ", ";
                            }

                            if (emails.length() > 2) {
                                emails = emails.substring(0, emails.length() - 2);
                            }

                            textViewMembers.setText(emails);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            mMembersDatabaseReference.addChildEventListener(mMembersEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mMessagesEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mMessagesEventListener);

            mMessagesEventListener = null;
        }

        if (mMembersEventListener != null) {
            mMembersDatabaseReference.removeEventListener(mMembersEventListener);

            mMembersEventListener = null;
        }
    }

    private void setTitleFromChatUid(String chatUid) {
        mFirebaseDatabase
                .getReference("chats")
                .child(chatUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                        if (chatRoom != null) {
                            setTitle(chatRoom.getName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
    }

    private void initView() {
        mProgressBar = findViewById(R.id.progressBar);
        mMessageListView = findViewById(R.id.messageListView);

        mMessageEditText = findViewById(R.id.messageEditText);
        mSendButton = findViewById(R.id.sendButton);

        textViewMembers = findViewById(R.id.membersTextView);
    }
}

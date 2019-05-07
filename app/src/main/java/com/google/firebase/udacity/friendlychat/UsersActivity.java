package com.google.firebase.udacity.friendlychat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.google.firebase.udacity.friendlychat.adapter.UsersAdapter;
import com.google.firebase.udacity.friendlychat.adapter.item.UserItem;
import com.google.firebase.udacity.friendlychat.model.ChatRoom;
import com.google.firebase.udacity.friendlychat.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.Keys;

public class UsersActivity extends AppCompatActivity {

    private static final String TAG = "ChatsActivity";

    public static final String ANONYMOUS = "anonymous";

    public static final int RC_SIGN_IN = 1;

    private UsersAdapter mUsersAdapter;

    private ListView mUsersListView;
    private ProgressBar mProgressBar;

    private FloatingActionButton mFloatingActionButton;

    private String mUsername;
    private String mUserUid;

    // Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;

    private ChildEventListener mUsersEventListener;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mUsername = ANONYMOUS;

        // Initialize Firebase components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");

        // Initialize references to views
        mProgressBar = findViewById(R.id.progressBar);
        mUsersListView = findViewById(R.id.usersListView);

        // Initialize message ListView and its adapter
        List<UserItem> items = new ArrayList<>();
        mUsersAdapter = new UsersAdapter(this, R.layout.item_user, items);
        mUsersListView.setAdapter(mUsersAdapter);
        mUsersListView.setOnItemClickListener((adapterView, view, i, l) -> {
            final UserItem item = (UserItem) mUsersListView.getAdapter().getItem(i);
            ChatRoom chatRoom = new ChatRoom(mUserUid, item.getUser().getName());
            mFirebaseDatabase.getReference().child("chats").push().setValue(chatRoom, ((databaseError, databaseReference) -> {
                String uniqueKey = databaseReference.getKey();
                if (TextUtils.isEmpty(uniqueKey)) {
                    return;
                }

                Map<String, Boolean> members = new HashMap<>();
                members.put(mUserUid, true);
                members.put(item.getUid(), true);

                mFirebaseDatabase.getReference().child("members").child(uniqueKey).setValue(members);

                Intent intent = new Intent(UsersActivity.this, MainActivity.class);
                intent.putExtra(Keys.EXTRA_CHAT_ROOM_UID, uniqueKey);
                startActivity(intent);
            }));
        });

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

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
                                        new AuthUI.IdpConfig.EmailBuilder().build(),
                                        new AuthUI.IdpConfig.FacebookBuilder().build()))
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

        mUsersAdapter.clear();
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

        mUsersAdapter.clear();
        detachDatabaseReadListener();
    }


    private void attachDatabaseReadListener() {
        if (mUsersEventListener == null) {
            mUsersEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    if (!dataSnapshot.getKey().equals(mUserUid)) {
                        User user = dataSnapshot.getValue(User.class);
                        UserItem item = new UserItem(dataSnapshot.getKey(), user);
                        mUsersAdapter.add(item);
                    }
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

            mUsersDatabaseReference.addChildEventListener(mUsersEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mUsersEventListener != null) {
            mUsersDatabaseReference.removeEventListener(mUsersEventListener);

            mUsersEventListener = null;
        }
    }
}

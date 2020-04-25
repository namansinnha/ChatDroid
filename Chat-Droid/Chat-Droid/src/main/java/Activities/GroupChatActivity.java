package com.developer.coreandroidx.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.developer.coreandroidx.whatsappclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton sendMessage_button;
    private EditText userMessageInput;
    private ScrollView scrollView;
    private TextView displayMessage;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference, groupNameReference, groupMessageKeyReference;
    private String groupName, currentUserId, currentUserName, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        groupName = getIntent().getExtras().getString("GroupName");

        mFirebaseAuth = FirebaseAuth.getInstance();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupName);

        currentUserId = mFirebaseAuth.getCurrentUser().getUid();

        initializeVariable ();
        settingUpToolbar ();
        getUserInfo ();

        sendMessage_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveMessageInfoToDatabase ();
                userMessageInput.setText("");
                scrollView.fullScroll(View.FOCUS_DOWN);

            }
        });

    }

    private void initializeVariable () {

        toolbar = findViewById(R.id.group_chat_bar_layout);
        sendMessage_button = findViewById(R.id.send_message);
        userMessageInput = findViewById(R.id.input_group_message);
        scrollView = findViewById(R.id.scrollView);
        displayMessage = findViewById(R.id.group_chat_text_display);

    }

    private void settingUpToolbar () {

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(groupName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    private void getUserInfo () {

        mDatabaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    currentUserName = dataSnapshot.child("name").getValue().toString();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void saveMessageInfoToDatabase () {

        String userMessage = userMessageInput.getText().toString();
        String messageKey = groupNameReference.push().getKey();

        if (TextUtils.isEmpty(userMessage)) {


        } else {

            Calendar calendarCurrentDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd/MMM,YYYY");
            currentDate = currentDateFormat.format(calendarCurrentDate.getTime());

            Calendar calendarCurrentTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calendarCurrentTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupNameReference.updateChildren(groupMessageKey);
            groupMessageKeyReference = groupNameReference.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", userMessage);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);

            groupMessageKeyReference.updateChildren(messageInfoMap);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupNameReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists()) {

                    displayMessageOnScreen(dataSnapshot);

                }

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
        });
    }

    private void displayMessageOnScreen (DataSnapshot dataSnapshot) {

        Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()) {

            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayMessage.append(chatName + ":\n"+ chatMessage + "\n" + chatTime + "    " + chatDate + " \n\n\n");
            scrollView.fullScroll(View.FOCUS_DOWN);
        }

    }

}

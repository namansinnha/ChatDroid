package com.developer.coreandroidx.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.developer.coreandroidx.whatsappclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId, senderUserId,currentState;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton, declineMessageRequestButton;

    private DatabaseReference userRef, chatRequestRef, contactsRef, notificationRef;
    private FirebaseAuth mAuth;

    private String REQUEST_TYPE;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getApplicationContext().getResources().getColor(R.color.white));
        setContentView(R.layout.activity_profile);

        initializeVariable ();
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        senderUserId = mAuth.getCurrentUser().getUid();

        retrieveUserInfo ();
        currentState = "new";

    }

    private void initializeVariable () {

        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_profile_username);
        userProfileStatus = findViewById(R.id.visit_user_profile_status);
        sendMessageRequestButton = findViewById(R.id.send_request_message_btn);
        declineMessageRequestButton = findViewById(R.id.decline_request_message_btn);

    }

    private void retrieveUserInfo () {
        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))) {

                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.ic_profile).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequest ();

                } else {

                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequest ();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private  void manageChatRequest () {

        chatRequestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(receiverUserId)){

                    REQUEST_TYPE = Objects.requireNonNull(dataSnapshot.child(receiverUserId).child("request_type").getValue()).toString();

                    if(REQUEST_TYPE.equals("sent")) {

                        currentState = "request_sent";

                        sendMessageRequestButton.setTextColor(getResources().getColor(R.color.aachaDarkRedColor));
                        sendMessageRequestButton.setBackgroundResource(R.drawable.cancel_request_background);
                        sendMessageRequestButton.setText("Cancel");

                    } else if (REQUEST_TYPE.equals("received")) {

                        currentState = "request_received";

                        sendMessageRequestButton.setVisibility(View.VISIBLE);
                        sendMessageRequestButton.setText("Accept");

                        declineMessageRequestButton.setVisibility(View.VISIBLE);
                        declineMessageRequestButton.setEnabled(true);
                        declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                cancelChatRequest();

                            }
                        });


                    }
                } else {

                    contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild(receiverUserId)) {

                                currentState = "friends";
                                sendMessageRequestButton.setText("Remove");
                                sendMessageRequestButton.setBackgroundResource(R.drawable.cancel_request_background);
                                sendMessageRequestButton.setTextColor(getResources().getColor(R.color.aachaDarkRedColor));

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (!(senderUserId.equals(receiverUserId))){

            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    sendMessageRequestButton.setEnabled(false);
                    if (currentState.equals("new")){

                        sendChatRequest ();

                    } else if (currentState.equals("request_sent")) {

                        cancelChatRequest ();

                    } else if (currentState.equals("request_received")) {

                        acceptChatRequest ();

                    } else if (currentState.equals("friends")) {

                        removeSpecificContact ();

                    }

                }
            });

        } else {

            sendMessageRequestButton.setVisibility(View.INVISIBLE);

        }

    }

    private void sendChatRequest () {

        chatRequestRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    chatRequestRef.child(receiverUserId).child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            HashMap<String, String> chatNotificationMap = new HashMap<>();
                            chatNotificationMap.put("from", senderUserId);
                            chatNotificationMap.put("type", "request");

                            notificationRef.child(receiverUserId).push().setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        sendMessageRequestButton.setEnabled(true);
                                        currentState = "request_sent";
                                        sendMessageRequestButton.setTextColor(getResources().getColor(R.color.aachaDarkRedColor));
                                        sendMessageRequestButton.setBackgroundResource(R.drawable.cancel_request_background);
                                        sendMessageRequestButton.setText("Cancel");

                                    }

                                }
                            });

//                            sendMessageRequestButton.setEnabled(true);
//                            currentState = "request_sent";
//                            sendMessageRequestButton.setTextColor(getResources().getColor(R.color.aachaDarkRedColor));
//                            sendMessageRequestButton.setBackgroundResource(R.drawable.cancel_request_background);
//                            sendMessageRequestButton.setText("Cancel");
                        }
                    });
                }
            }
        });

    }

    private void cancelChatRequest () {

        chatRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()) {

                    chatRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                sendMessageRequestButton.setEnabled(true);
                                currentState = "new" ;
                                sendMessageRequestButton.setTextColor(getResources().getColor(R.color.aacahaDarkGreenColor));
                                sendMessageRequestButton.setBackgroundResource(R.drawable.send_request_background);
                                sendMessageRequestButton.setText("Message");
                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                declineMessageRequestButton.setEnabled(false);
                            }

                        }
                    });

                }

            }
        });

    }

    private void acceptChatRequest () {

        contactsRef.child(senderUserId).child(receiverUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    contactsRef.child(receiverUserId).child(senderUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                chatRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            chatRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()) {

                                                        sendMessageRequestButton.setEnabled(true);
                                                        currentState = "friends";
                                                        sendMessageRequestButton.setText("Remove");
                                                        sendMessageRequestButton.setBackgroundResource(R.drawable.cancel_request_background);
                                                        sendMessageRequestButton.setTextColor(getResources().getColor(R.color.aachaDarkRedColor));
                                                        declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                        declineMessageRequestButton.setEnabled(false);

                                                    }

                                                }
                                            });

                                        }

                                    }
                                });

                            }
                        }
                    });

                }

            }
        });

    }

    private void removeSpecificContact () {

        contactsRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()) {

                    contactsRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                sendMessageRequestButton.setEnabled(true);
                                currentState = "new" ;
                                sendMessageRequestButton.setTextColor(getResources().getColor(R.color.aacahaDarkGreenColor));
                                sendMessageRequestButton.setBackgroundResource(R.drawable.send_request_background);
                                sendMessageRequestButton.setText("Message");
                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                declineMessageRequestButton.setEnabled(false);
                            }

                        }
                    });

                }

            }
        });

    }

}

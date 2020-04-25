package com.developer.coreandroidx.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.developer.coreandroidx.whatsappclone.Adapters.MessageAdapter;
import com.developer.coreandroidx.whatsappclone.Models.Messages;
import com.developer.coreandroidx.whatsappclone.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class ChatActivity extends AppCompatActivity {

    private String message_receiver_id, message_receiver_name, message_receiver_image;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar chatToolbar;
    private ImageButton sendMessageButton, sendFilesButton;
    private EditText messageInputText;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private String senderId;
    private String currentUserId;

    private ProgressDialog mProgressDialog;


    private String checker = "", myURL = "";
    private StorageTask uploadTask;
    private Uri fileUri;
    private String saveCurrentTime, saveCurrentDate;

    private final List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        senderId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        mProgressDialog = new ProgressDialog(this);

        message_receiver_name = Objects.requireNonNull(getIntent().getExtras().get("visit_user_name")).toString();
        message_receiver_id = Objects.requireNonNull(getIntent().getExtras().get("visit_user_id")).toString();
        message_receiver_image = Objects.requireNonNull(getIntent().getExtras().get("visit_user_image")).toString();

        initializeVariable();
        settingUpToolbar();


        userName.setText(message_receiver_name);
        Picasso.get().load(message_receiver_image).into(userImage);

        if (message_receiver_image.equals("default_image")) {

            Glide.with(getApplicationContext()).load(R.drawable.ic_profile).into(userImage);
            Picasso.get().load(R.drawable.ic_profile).into(userImage);
        }

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessageForPrivateText();

            }
        });

        displayLastSeen();

        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence options[] = new CharSequence[]{

                        "Image Files",
                        "PDF Files",
                        "Docs Files"

                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Choose file type");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (which == 0) {

                            checker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Images"), 123);


                        }
                        if (which == 1) {

                            checker = "pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF Files"), 123);


                        }
                        if (which == 2) {

                            checker = "docs";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/docs");
                            startActivityForResult(intent.createChooser(intent, "Select Docs Files"), 123);
                        }

                    }
                });

                builder.show();

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            mProgressDialog.setTitle("Getting Files" );
            mProgressDialog.setMessage ("Please wait,we are sending the file");
            mProgressDialog.show ();
            mProgressDialog.setCanceledOnTouchOutside(false);

            fileUri = data.getData();

            if (!checker.equals("image")) {

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Documents");
                final String messageSenderRef = "Messages /" + senderId + "/" + message_receiver_id;
                final String messageReceiverRef = "Messages /" + message_receiver_id + "/" + senderId;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(senderId).child(message_receiver_id).push();

                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId + "." + checker);
                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", task.getResult().getMetadata().getReference().getDownloadUrl().toString());
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", senderId);
                            messageTextBody.put("to", message_receiver_id);
                            messageTextBody.put("messageId", messagePushId);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map messageBodyDetail = new HashMap();
                            messageBodyDetail.put(messageSenderRef + "/" + messagePushId, messageTextBody);
                            messageBodyDetail.put(messageReceiverRef + "/" + messagePushId, messageTextBody);
                            rootRef.updateChildren(messageBodyDetail);
                            mProgressDialog.dismiss();

                        } else {

                            Toasty.error(getApplicationContext(), "Error : " + task.getException().getMessage(),Toast.LENGTH_SHORT).show();

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        mProgressDialog.dismiss();
                        Toasty.error(getApplicationContext(), "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                        double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        mProgressDialog.setMessage((int) p + "% Uploading....");

                    }
                });


            } else if (checker.equals("image")) {

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image-Files");
                final String messageSenderRef = "Messages /" + senderId + "/" + message_receiver_id;
                final String messageReceiverRef = "Messages /" + message_receiver_id + "/" + senderId;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(senderId).child(message_receiver_id).push();

                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId + "." + "jpg");
                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful()) {

                            mProgressDialog.dismiss();
                            throw task.getException();

                        }

                        mProgressDialog.dismiss();
                        return filePath.getDownloadUrl();


                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()) {

                            mProgressDialog.dismiss();
                            Uri downloadUri = task.getResult();
                            myURL = downloadUri.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myURL);
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", senderId);
                            messageTextBody.put("to", message_receiver_id);
                            messageTextBody.put("messageId", messagePushId);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map messageBodyDetail = new HashMap();
                            messageBodyDetail.put(messageSenderRef + "/" + messagePushId, messageTextBody);
                            messageBodyDetail.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

                            rootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if (task.isSuccessful()) {

                                    } else {

                                        Toasty.error(getApplicationContext(), "failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                    messageInputText.setText("");

                                }
                            });

                        }

                    }
                });


            } else {

                Toasty.error(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }

        }

    }

    private void initializeVariable() {

        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);
        userImage = findViewById(R.id.custom_profile_image);
        chatToolbar = findViewById(R.id.custom_chat_toolbar);
        sendMessageButton = findViewById(R.id.send_message);
        sendFilesButton = findViewById(R.id.send_files);
        messageInputText = findViewById(R.id.input_group_message);
        userMessageList = findViewById(R.id.private_message_of_users);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd / MMM, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());


        messageAdapter = new MessageAdapter(messageList, this);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);

    }

    private void displayLastSeen() {

        rootRef.child("Users").child(message_receiver_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("userState").hasChild("state")) {

                    String state = dataSnapshot.child("userState").child("state").getValue().toString();
                    String date = dataSnapshot.child("userState").child("date").getValue().toString();
                    String time = dataSnapshot.child("userState").child("time").getValue().toString();

                    if (state.equals("online")) {

                        userLastSeen.setText("online");

                    } else if (state.equals("offline")) {

                        userLastSeen.setText("Last Seen : " + "\t" + date + "\t" + "\t" + time);

                    }

                } else {

                    userLastSeen.setText("offline");


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void settingUpToolbar() {

        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(view);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            updateUserStatus("offline");

        } else {

            updateUserStatus("online");

        }

        rootRef.child("Messages ").child(senderId).child(message_receiver_id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);
                messageList.add(messages);
                messageAdapter.notifyDataSetChanged();

                userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());

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

                Toasty.error(getApplicationContext(), "Chat Message --> " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void sendMessageForPrivateText() {

        String messageText = messageInputText.getText().toString();
        if (TextUtils.isEmpty(messageText)) {

        } else {

            String messageSenderRef = "Messages /" + senderId + "/" + message_receiver_id;
            String messageReceiverRef = "Messages /" + message_receiver_id + "/" + senderId;

            DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(senderId).child(message_receiver_id).push();

            String messagePushId = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", senderId);
            messageTextBody.put("to", message_receiver_id);
            messageTextBody.put("messageId", messagePushId);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            Map messageBodyDetail = new HashMap();
            messageBodyDetail.put(messageSenderRef + "/" + messagePushId, messageTextBody);
            messageBodyDetail.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

            rootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful()) {

                    } else {

                        Toasty.error(getApplicationContext(), "failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");

                }
            });
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            updateUserStatus("offline");

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            updateUserStatus("offline");

        }
    }


    private void updateUserStatus(String state) {

        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd / MMM, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        currentUserId = mAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentUserId).child("userState").updateChildren(onlineStateMap);

    }

}




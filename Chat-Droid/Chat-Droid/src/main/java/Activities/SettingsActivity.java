package com.developer.coreandroidx.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.developer.coreandroidx.whatsappclone.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class SettingsActivity extends AppCompatActivity {

    private EditText set_username, set_profile_status;
    private Button update_btn;
    private CircleImageView profile_image;
    private String currentUserId;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;
    private int GalleryPick = 123;

    private StorageReference  storageReference;

    private ProgressDialog mProgressDialog;

    private ImageButton back;

    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getApplicationContext().getResources().getColor(R.color.white));
        setContentView(R.layout.activity_settings);

        initializeVariable();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mProgressDialog = new ProgressDialog(this);


        currentUserId = mFirebaseAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");
//        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateSettings();

            }
        });

        retrieveUserInfo();

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(galleryIntent, IMAGE_REQUEST);

            }
        });

    }

    private void initializeVariable() {

        set_username = findViewById(R.id.set_username);
        set_profile_status = findViewById(R.id.set_profile_status);
        update_btn = findViewById(R.id.setting_btn);
        profile_image = findViewById(R.id.set_profile_image);
        back = findViewById(R.id.back);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
//
//            Uri imageUri = data.getData();
//
//            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
//                    .setAspectRatio(1, 1)
//                    .start(SettingsActivity.this);
//        }
//
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//
//            if (resultCode == RESULT_OK) {
//                mProgressDialog.setTitle("Setting Profile");
//                mProgressDialog.setMessage ("Please wait, your profile image is getting updated");
//                mProgressDialog.show ();
//                mProgressDialog.setCanceledOnTouchOutside(false);
//
//                Uri resultUri = result.getUri();
//
//                StorageReference filepath = userProfileImageRef.child (currentUserId + ".jpg");
//                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//
//                        if (task.isSuccessful()) {
//
//                            mProgressDialog.dismiss ();
//                            Toasty.success(getApplicationContext(), "Upload successful", Toast.LENGTH_SHORT).show();
//                            String downloadUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();
//
//                            mDatabaseReference.child("Users").child(currentUserId).child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//
//                                    if (task.isSuccessful()) {
//                                        mProgressDialog.dismiss ();
//                                        Toasty.success(getApplicationContext(), "Image saved", Toast.LENGTH_SHORT).show();
//
//                                    } else  {
//
//                                        mProgressDialog.dismiss ();
//                                        Toasty.error(getApplicationContext(), "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//
//                                    }
//
//                                }
//                            });
//
//                        } else {
//
//                            mProgressDialog.dismiss ();
//                            Toasty.error(getApplicationContext(), "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//
//                        }
//
//                    }
//                });
//
//            }
//
//        }
//    }

    private void updateSettings() {

        String setUsername = set_username.getText().toString();
        String set_profileStatus = set_profile_status.getText().toString();

        if (TextUtils.isEmpty(setUsername)) {

            set_username.setError("Required");

        }
        if (TextUtils.isEmpty(set_profileStatus)) {

            set_profile_status.setError("Required");

        } else {

            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserId);
            profileMap.put("name", setUsername);
            profileMap.put("status", set_profileStatus);
            mDatabaseReference.child("Users").child(currentUserId).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {

                        Toasty.success(getApplicationContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        sendUserToMainActivity();


                    } else {

                        Toasty.error(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }

                }
            });


            //uploadImage ();

        }

    }

    private void sendUserToMainActivity() {

        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(mainIntent);

    }

    private void retrieveUserInfo() {

        mDatabaseReference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && ((dataSnapshot.hasChild("image")))) {

                    String retrieveUsername = dataSnapshot.child("name").getValue().toString();
                    String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();
                    String retrieveProfile = dataSnapshot.child("image").getValue().toString();

                    set_username.setText(retrieveUsername);
                    set_profile_status.setText(retrieveUserStatus);

                    //Picasso.get().load(retrieveProfile).into(profile_image);
                    Glide.with(SettingsActivity.this).load(retrieveProfile).into(profile_image);


                } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {


                    String retrieveUsername = dataSnapshot.child("name").getValue().toString();
                    String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();

                    set_username.setText(retrieveUsername);
                    set_profile_status.setText(retrieveUserStatus);


                } else {

                    Toasty.info(getApplicationContext(), "Please set and update your profile", Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private String getFileExtension (Uri uri) {

        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage () {

        if (imageUri != null ) {

            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    if (!task.isSuccessful()) {

                        throw task.getException();

                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if (task.isSuccessful()) {

                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("image", mUri);
                        mDatabaseReference.updateChildren(map);
                        Toasty.success(getApplicationContext(), "Uploaded image", Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss ();

                    } else {

                        Toasty.error(getApplicationContext(), "Failed : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss ();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toasty.error(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });

        } else {

            Toasty.info(getApplicationContext(), "No image selected", Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null ) {
            mProgressDialog.setTitle("Setting Profile");
                mProgressDialog.setMessage ("Please wait, your profile image is getting updated");
                mProgressDialog.show ();
                mProgressDialog.setCanceledOnTouchOutside(false);
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()){

                Toasty.info(getApplicationContext(), "Upload is in progress", Toast.LENGTH_SHORT).show();

            } else {

                uploadImage();

            }

        }

    }
}

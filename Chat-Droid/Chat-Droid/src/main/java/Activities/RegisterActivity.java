package com.developer.coreandroidx.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.developer.coreandroidx.whatsappclone.R;
import com.developer.coreandroidx.whatsappclone.Utilities.AsteriskPasswordTransformationMethod;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {

    private EditText register_email, register_password;
    private Button signIn, register_btn;

    private String emailPattern = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
    private String email, password;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;

    private ProgressDialog mProgressDialog;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getApplicationContext().getResources().getColor(R.color.white));
        setContentView(R.layout.activity_register);

        initializeVariable ();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mProgressDialog = new ProgressDialog (this);

        register_password.setTransformationMethod (new AsteriskPasswordTransformationMethod());

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loginActivity ();

            }
        });

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validateEntries()) {
                    createAccount();
                } else {
                    Toasty.warning(getApplicationContext(), "Please fill the required details", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void initializeVariable () {

        register_email = findViewById(R.id.register_email);
        register_password = findViewById(R.id.register_password);
        register_btn = findViewById(R.id.register_btn);
        signIn = findViewById(R.id.signIn);

    }

    private void loginActivity () {

        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));

    }

    private void createAccount () {

        email = register_email.getText().toString();
        password = register_password.getText().toString();

        mProgressDialog.setMessage ("Please wait you are getting registered");
        mProgressDialog.show ();
        mProgressDialog.setCanceledOnTouchOutside(false);

        mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    String currentUserID = mFirebaseAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    mDatabaseReference.child("Users").child(currentUserID).setValue("");
                    mDatabaseReference.child("Users").child(currentUserID).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            Toasty.success(getApplicationContext(), "Account created successfully", Toast.LENGTH_SHORT).show();
                            mainActivity();
                            mProgressDialog.dismiss ();


                        }
                    });

                } else {

                    mProgressDialog.dismiss ();
                    Toasty.error (getApplicationContext (), task.getException ().getMessage (), Toast.LENGTH_SHORT).show ();

                }


            }
        });

    }

    private boolean validateEntries() {

        boolean result;

        email = register_email.getText ().toString ().trim ();
        password = register_password.getText ().toString ().trim ();

        if ((!validateEmail ()) || (!validatePassword ())) {

            result = false;

        } else {

            result = true;

        }


        return result;
    }

    private boolean validateEmail() {

        boolean result;

        email = register_email.getText ().toString ().trim ();

        if (email.matches (emailPattern) && email.length () > 0) {

            result = true;

        } else {
            register_email.setError ("Invalid email");
            result = false;
        }

        return result;
    }

    private boolean validatePassword() {

        boolean result;

        password = register_password.getText().toString();

        if (register_password.length () <= 5) {

            register_password.setFocusable (true);
            register_password.setError ("Atleast 6 characters expected");


            if (register_password.length () == 0) {

                register_password.setError ("Password is mandatory");

            }

            result = false;

        } else {

            result = true;
        }

        return result;

    }

    private void mainActivity () {

        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(mainIntent);
        finish();

    }


}

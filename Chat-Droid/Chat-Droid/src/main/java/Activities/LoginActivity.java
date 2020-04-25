package com.developer.coreandroidx.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {

    private EditText login_email, login_password;
    private Button forgot_password, signUp, login_btn;

    private String emailPattern = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
    private String email, password;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference usersRef;

    private ProgressDialog mProgressDialog;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getApplicationContext().getResources().getColor(R.color.white));
        setContentView(R.layout.activity_login);

        initializeVariable();
        mFirebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mProgressDialog = new ProgressDialog(this);

        login_password.setTransformationMethod(new AsteriskPasswordTransformationMethod());

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                registerActivity();

            }
        });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

//        login_via_phone.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                phoneActivity ();
//
//            }
//        });

    }

    private void initializeVariable() {

        login_email = findViewById(R.id.login_email);
        login_password = findViewById(R.id.login_password);
        forgot_password = findViewById(R.id.login_forgot_password);
        login_btn = findViewById(R.id.login_btn);
        signUp = findViewById(R.id.signUp);
       // login_via_phone = findViewById(R.id.login_via_phone);
    }

    private void registerActivity() {

        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));

    }

    private void sendUserToMainActivity() {

        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(mainIntent);
        finish();

    }

    private void signIn() {

        email = login_email.getText().toString();
        password = login_password.getText().toString();

        if (TextUtils.isEmpty(email)) {

            login_email.setError("Required");

        }
        if (TextUtils.isEmpty(password)) {

            login_password.setError("Required");

        } else {
            mProgressDialog.setTitle("Signing-you In");
            mProgressDialog.setMessage ("Please wait");
            mProgressDialog.show ();
            mProgressDialog.setCanceledOnTouchOutside(false);

            mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {

                        mProgressDialog.dismiss ();

                        String currentUserId = mFirebaseAuth.getCurrentUser().getUid();
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        usersRef.child(currentUserId).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()){

                                    sendUserToMainActivity();
                                    Toasty.success(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();

                                }

                            }
                        });


                    } else {

                        mProgressDialog.dismiss ();
                        Toasty.error(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            });

        }

    }

    private void phoneActivity () {

        Intent phoneIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
        startActivity(phoneIntent);
        finish();

    }

}

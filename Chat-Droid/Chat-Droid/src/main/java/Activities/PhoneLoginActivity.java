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
import com.github.florent37.materialtextfield.MaterialTextField;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerificationCodeButton, verifyButton;
    private EditText inputPhoneNumber, inputVerificationCode;
    private MaterialTextField login_phone_otp_layout, login_phone_layout;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getApplicationContext().getResources().getColor(R.color.white));
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();
        mProgressDialog = new ProgressDialog(this);

        initializeVariable();

        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                inputPhoneNumber.setVisibility(View.GONE);
                login_phone_layout.setVisibility(View.GONE);

                verifyButton.setVisibility(View.VISIBLE);
                login_phone_otp_layout.setVisibility(View.VISIBLE);

                final String phoneNumber = inputPhoneNumber.getText().toString();
                if (TextUtils.isEmpty(phoneNumber)) {

                    inputPhoneNumber.setError("Required");

                } else {
                    mProgressDialog.setTitle("Phone Authentication");
                    mProgressDialog.setMessage ("Please wait, while we authenticate your phone");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show ();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,
                            60,
                            TimeUnit.SECONDS,
                            PhoneLoginActivity.this,
                            mCallbacks);

                }

            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                mProgressDialog.dismiss ();
                Toasty.error(getApplicationContext(), "Invalid Code :" + e.getMessage(), Toast.LENGTH_SHORT).show();

                inputVerificationCode.setVisibility(View.VISIBLE);
                login_phone_layout.setVisibility(View.VISIBLE);

                verifyButton.setVisibility(View.GONE);
                login_phone_otp_layout.setVisibility(View.GONE);

            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {

                mVerificationId = verificationId;
                mResendToken = token;

                mProgressDialog.dismiss ();
                Toasty.success(getApplicationContext(), "Code has been sent successfully", Toast.LENGTH_SHORT).show();

                inputVerificationCode.setVisibility(View.GONE);
                login_phone_layout.setVisibility(View.GONE);

                verifyButton.setVisibility(View.VISIBLE);
                login_phone_otp_layout.setVisibility(View.VISIBLE);

            }
        };

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                login_phone_layout.setVisibility(View.GONE);
                login_phone_layout.setVisibility(View.GONE);

                String verificationCode = inputVerificationCode.getText().toString();
                if (TextUtils.isEmpty(verificationCode)) {

                    inputVerificationCode.setError("Required");

                } else {

                    mProgressDialog.setTitle("Code verification");
                    mProgressDialog.setMessage ("Please wait, while we verify your code");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show ();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }


            }
        });

    }

    private void initializeVariable() {

        sendVerificationCodeButton = findViewById(R.id.login_phone_send_otp);
        verifyButton = findViewById(R.id.login_phone_btn);
        inputPhoneNumber = findViewById(R.id.login_phone);
        inputVerificationCode = findViewById(R.id.login_otp);
        login_phone_otp_layout = findViewById(R.id.login_otp_layout);
        login_phone_layout = findViewById(R.id.login_phone_layout);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    mProgressDialog.dismiss ();
                    Toasty.success(getApplicationContext(), "Logged in successfully", Toast.LENGTH_SHORT).show();
                    sendUserToMainActivity();

                } else {

                    mProgressDialog.dismiss ();
                    Toasty.error(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();


                }
            }
        });
    }

    private void sendUserToMainActivity () {

        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        mainIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(mainIntent);
        finish();


    }

}

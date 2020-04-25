package com.developer.coreandroidx.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.developer.coreandroidx.whatsappclone.Adapters.TabsAccessorAdapter;
import com.developer.coreandroidx.whatsappclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeVariable();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        settingUpToolbar();

        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout.setupWithViewPager(myViewPager);

    }

    private void initializeVariable() {

        mToolbar = findViewById(R.id.main_page_toolbar);
        myViewPager = findViewById(R.id.main_tab_pager);
        myTabLayout = findViewById(R.id.main_tabs);
    }

    private void settingUpToolbar() {

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("WhatsApp");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();

        if (currentUser == null) {

            sendUserToLoginActivity();

        } else {

            updateUserStatus ("online");
            verifyUserExistance();

        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if (currentUser != null) {

            updateUserStatus ("offline");

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if (currentUser != null) {

            updateUserStatus ("offline");

        }
    }

    private void sendUserToLoginActivity() {

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.find_friends) {

            findFriendsActivity();

        }
        if (item.getItemId() == R.id.settings) {

            sendUserToSettingsActivity();

        }
        if (item.getItemId() == R.id.logout) {

            updateUserStatus("offline");
            mFirebaseAuth.signOut();
            sendUserToLoginActivity();

        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)

    private void sendUserToSettingsActivity() {

        Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingIntent);
    }

    private void verifyUserExistance() {

        String currentUserID = mFirebaseAuth.getCurrentUser().getUid();
        mDatabaseReference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if ((dataSnapshot.child("name").exists())) {

                    Toasty.success(getApplicationContext(), "Welcome", Toast.LENGTH_SHORT).show();

                } else {

                    sendUserToSettingsActivity();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void findFriendsActivity() {

        Intent settingIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(settingIntent);

    }

    private void updateUserStatus (String state) {

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

        currentUserId = mFirebaseAuth.getCurrentUser().getUid();
        mDatabaseReference.child("Users").child(currentUserId).child("userState").updateChildren(onlineStateMap);

    }

}

package com.developer.coreandroidx.whatsappclone.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.developer.coreandroidx.whatsappclone.Activities.GroupChatActivity;
import com.developer.coreandroidx.whatsappclone.Activities.MainActivity;
import com.developer.coreandroidx.whatsappclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import es.dmoral.toasty.Toasty;

public class GroupsFragment extends Fragment {

    private View groupFragmentView;
    private ListView listView;
    private FloatingActionButton create_group_fab;

    private ArrayList<String> list_of_group = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private DatabaseReference groupReference;
    private DatabaseReference mDatabaseReference;


    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);
        groupReference = FirebaseDatabase.getInstance().getReference().child("Groups");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        initializeVariable ();
        retrieveAndDisplayGroup ();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String groupName = parent.getItemAtPosition(position).toString();

                Intent groupChatActivity = new Intent(getContext(), GroupChatActivity.class);
                groupChatActivity.putExtra("GroupName", groupName);
                startActivity(groupChatActivity);

            }
        });

        listView.setAdapter(arrayAdapter);

        create_group_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestNewGroup();

            }
        });

        return groupFragmentView;

    }

    private void initializeVariable () {

        listView = groupFragmentView.findViewById(R.id.list_view);
        create_group_fab = groupFragmentView.findViewById(R.id.create_group_fab);
        arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, list_of_group);
    }

    private void retrieveAndDisplayGroup () {

        groupReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {

                    set.add(((DataSnapshot)iterator.next()).getKey());

                }

                list_of_group.clear();
                list_of_group.addAll(set);
                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void requestNewGroup() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Set a title for alert dialog
        builder.setTitle("Create Group");

        final EditText groupNameField = new EditText(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(16, 0, 16, 0);
        groupNameField.setLayoutParams(lp);
        groupNameField.setHint("eg: School Friends");
        Typeface typeface = getResources().getFont(R.font.ubuntu_regular);
        groupNameField.setTypeface(typeface);

        builder.setView(groupNameField);

        // Set the positive button
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName)) {

                    groupNameField.setError("Required");

                } else {

                    //database
                    createNewGroup(groupName);

                }
            }
        });

        // Set the negative button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

        // Get the alert dialog buttons reference
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        // Change the alert dialog buttons text and background color
        positiveButton.setTextColor(Color.parseColor("#FF0B8B42"));
        //positiveButton.setBackgroundColor(Color.parseColor("#FFE1FCEA"));

        negativeButton.setTextColor(Color.parseColor("#FFFF0400"));
        //negativeButton.setBackgroundColor(Color.parseColor("#FFFCB9B7"));

    }
    private void createNewGroup(final String groupName) {

        mDatabaseReference.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Toasty.success(getContext(), groupName + " was created successfully", Toast.LENGTH_SHORT).show();

                } else {

                    Toasty.error(getContext(), "Failed to create group : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

}

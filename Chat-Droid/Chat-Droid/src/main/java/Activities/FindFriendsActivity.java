package com.developer.coreandroidx.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.developer.coreandroidx.whatsappclone.Models.ContactsModel;
import com.developer.coreandroidx.whatsappclone.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView findFriendsRecyclerList;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        initializeVariable ();
        settingUpToolbar ();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        findFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));


    }

    private void initializeVariable () {

        mToolbar = findViewById(R.id.find_friends_toolbar);
        findFriendsRecyclerList = findViewById(R.id.find_friends_recycler_list);

    }

    private void settingUpToolbar () {

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ContactsModel> options = new FirebaseRecyclerOptions.Builder<ContactsModel>()
                .setQuery(userRef, ContactsModel.class)
                .build();

        FirebaseRecyclerAdapter<ContactsModel, FindFriendsViewHolder> adapter = new FirebaseRecyclerAdapter<ContactsModel, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull ContactsModel model) {

                holder.username.setText(model.getName());
                holder.userStatus.setText(model.getStatus());

                //Glide.with(getApplicationContext()).load(model.getImage()).placeholder(R.drawable.ic_profile).into(holder.profileImage);
                Picasso.get().load(model.getImage()).placeholder(R.drawable.ic_profile).into(holder.profileImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String visit_user_id = getRef(position).getKey();
                        Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("visit_user_id", visit_user_id);
                        startActivity(profileIntent);


                    }
                });

            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);

                return viewHolder;

            }
        };

        findFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {

        private TextView username, userStatus;
        private CircleImageView profileImage;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);

        }
    }

}

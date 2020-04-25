package com.developer.coreandroidx.whatsappclone.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.developer.coreandroidx.whatsappclone.Activities.ChatActivity;
import com.developer.coreandroidx.whatsappclone.Models.ContactsModel;
import com.developer.coreandroidx.whatsappclone.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class ChatsFragment extends Fragment {

    private View privateChatsView;
    private RecyclerView chatsList;

    private DatabaseReference chatsRef, usersRef;
    private FirebaseAuth mAuth;

    private String currentUserId;



    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        chatsList = privateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ContactsModel> options = new FirebaseRecyclerOptions.Builder<ContactsModel>()
                .setQuery(chatsRef, ContactsModel.class)
                .build();

        FirebaseRecyclerAdapter<ContactsModel, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<ContactsModel, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull ContactsModel model) {
                holder.userName.setVisibility(View.VISIBLE);
                holder.userStatus.setVisibility(View.VISIBLE);
                holder.profileImage.setVisibility(View.VISIBLE);

                final String usersId = getRef(position).getKey();
                final String[] retImage = {"default_image"};

                usersRef.child(usersId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            if (dataSnapshot.hasChild("image")) {

                                retImage[0] = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                                final String retName = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                                String retStatus = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();

                                Picasso.get().load(retImage[0]).placeholder(R.drawable.ic_profile).into(holder.profileImage);
                                holder.userName.setText(retName);

                                if (dataSnapshot.child("userState").hasChild("state")){

                                    String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                    String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                    String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                    if (state.equals("online")) {

                                        holder.userStatus.setText("online");

                                    } else if (state.equals("offline")){

                                        holder.userStatus.setText("Last Seen : " + "\t" + date +  "\t" + "\t" + time);

                                    }

                                } else {

                                    holder.userStatus.setText("offline");


                                }

                            }

                            if (dataSnapshot.child("userState").hasChild("state")){

                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("online")) {

                                    holder.userStatus.setText("online");

                                } else if (state.equals("offline")){

                                    holder.userStatus.setText("Last Seen : " + "\t" + date +  "\t" + "\t" + time);

                                }

                            } else {

                                holder.userStatus.setText("offline");


                            }



                            final String retName = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                            String retStatus = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();

                            Glide.with(getContext()).load(R.drawable.ic_profile).into(holder.profileImage);

                            holder.userName.setText(retName);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id", usersId);
                                    chatIntent.putExtra("visit_user_name", retName);
                                    chatIntent.putExtra("visit_user_image", retImage[0]);
                                    startActivity(chatIntent);

                                }
                            });


                        } else {


                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        Toasty.error(getContext(), "Error : " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });


            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                return new ChatsViewHolder(view);
            }
        };

        chatsList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView profileImage;
        private TextView userStatus, userName;


        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.user_profile_image);
            userStatus = itemView.findViewById(R.id.user_status);
            userName = itemView.findViewById(R.id.user_profile_name);


        }


    }

}

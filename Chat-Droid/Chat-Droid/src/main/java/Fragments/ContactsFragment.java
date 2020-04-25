package com.developer.coreandroidx.whatsappclone.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

public class ContactsFragment extends Fragment {

    private View contactsView;
    private RecyclerView myContactsList;

    private DatabaseReference contactsReference, usersReference;
    private FirebaseAuth mAuth;

    private String currentUserId;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        initializeVariable();
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);

        return contactsView;

    }

    private void initializeVariable() {

        myContactsList = contactsView.findViewById(R.id.contacts_list);

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<ContactsModel>()
                .setQuery(contactsReference, ContactsModel.class)
                .build();

        FirebaseRecyclerAdapter<ContactsModel, ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<ContactsModel, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull ContactsModel model) {

                String userID = getRef(position).getKey();
                usersReference.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            if (dataSnapshot.hasChild("image")) {

                                String userImage = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                                String profileName = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                                String profileStatus = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                                Picasso.get().load(userImage).placeholder(R.drawable.ic_profile).into(holder.profileImage);

                                if (dataSnapshot.child("userState").hasChild("state")){

                                    String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                    String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                    String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                    if (state.equals("online")) {

                                        holder.onlineIcon.setVisibility(View.VISIBLE);

                                    } else if (state.equals("offline")){

                                        holder.onlineIcon.setVisibility(View.INVISIBLE);

                                    }

                                } else {

                                    holder.onlineIcon.setVisibility(View.INVISIBLE);


                                }


                            } else {

                                String profileName = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                                String profileStatus = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();

                                Glide.with(getContext()).load(R.drawable.ic_profile).into(holder.profileImage);
                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);

                                if (dataSnapshot.child("userState").hasChild("state")){

                                    String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                    String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                    String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                    if (state.equals("online")) {

                                        holder.onlineIcon.setVisibility(View.VISIBLE);

                                    } else if (state.equals("offline")){

                                        holder.userStatus.setVisibility(View.INVISIBLE);

                                    }

                                } else {

                                    holder.userStatus.setVisibility(View.INVISIBLE);


                                }


                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        Toasty.error(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from((parent).getContext()).inflate(R.layout.user_display_layout, parent, false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;

            }
        };

        myContactsList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder{

        private TextView userName, userStatus;
        private CircleImageView profileImage;
        private ImageView onlineIcon;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            onlineIcon = itemView.findViewById(R.id.user_online_status);

        }
    }

}

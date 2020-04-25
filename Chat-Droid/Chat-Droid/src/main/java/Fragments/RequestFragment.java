package com.developer.coreandroidx.whatsappclone.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.coreandroidx.whatsappclone.Models.ContactsModel;
import com.developer.coreandroidx.whatsappclone.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class RequestFragment extends Fragment {

    private View requestFragmentView;
    private RecyclerView myRequestList;

    private FirebaseAuth mAuth;
    private DatabaseReference chatRequestRef, usersRef, contactsRef;

    private String currentUserId;

    public RequestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        requestFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        initializeVariable();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));
        return requestFragmentView;
    }

    private void initializeVariable() {

        myRequestList = requestFragmentView.findViewById(R.id.chat_request_list);

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ContactsModel> options = new FirebaseRecyclerOptions.Builder<ContactsModel>()
                .setQuery(chatRequestRef.child(currentUserId), ContactsModel.class)
                .build();

        FirebaseRecyclerAdapter<ContactsModel, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<ContactsModel, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull ContactsModel model) {

                holder.itemView.findViewById(R.id.accept_request_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.decline_request_btn).setVisibility(View.VISIBLE);

                final String list_user_id = getRef(position).getKey();

                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();
                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            String type = dataSnapshot.getValue().toString();

                            if (type.equals("received")) {

                                usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.hasChild("image")) {


                                            String request_profileImage = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();


                                            Picasso.get().load(request_profileImage).into(holder.userImage);

                                        }

                                        String request_status = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                                        final String request_username = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();

                                        holder.userName.setText(request_username);
                                        holder.userStatus.setText("Wants to connect with you....");

                                        holder.accept_btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                contactsRef.child(currentUserId).child(list_user_id).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {

                                                            contactsRef.child(list_user_id).child(currentUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()) {

                                                                        chatRequestRef.child(currentUserId).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful()) {

                                                                                    chatRequestRef.child(list_user_id).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                                            if (task.isSuccessful()) {

                                                                                                Toasty.success(getContext(), "Contacts saved", Toast.LENGTH_SHORT).show();

                                                                                            } else {

                                                                                                Toasty.error(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();


                                                                                            }

                                                                                        }
                                                                                    });

                                                                                } else {

                                                                                    Toasty.error(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                                                                }

                                                                            }
                                                                        });

                                                                    } else {

                                                                        Toasty.error(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                                                    }

                                                                }
                                                            });

                                                        } else {

                                                            Toasty.error(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                                        }

                                                    }
                                                });

                                            }
                                        });

                                        holder.cancel_btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                chatRequestRef.child(currentUserId).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {

                                                            chatRequestRef.child(list_user_id).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()) {

                                                                        Toasty.success(getContext(), "Contact deleted", Toast.LENGTH_SHORT).show();

                                                                    }

                                                                }
                                                            });

                                                        }

                                                    }
                                                });

                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                        Toasty.error(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });

                            } else if (type.equals("sent")) {

                                Button request_sent_btn = holder.itemView.findViewById(R.id.accept_request_btn);
                                request_sent_btn.setText("Cancel Request");
                                request_sent_btn.setBackgroundResource(R.drawable.cancel_request_background);
                                request_sent_btn.setTextColor(getResources().getColor(R.color.aachaDarkRedColor));

                                holder.itemView.findViewById(R.id.decline_request_btn).setVisibility(View.GONE);

                                usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.hasChild("image")) {


                                            String request_profileImage = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();


                                            Picasso.get().load(request_profileImage).into(holder.userImage);

                                        }

                                        String request_status = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                                        final String request_username = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();

                                        holder.userName.setText(request_username);
                                        holder.userStatus.setText("You have sent request to" + "  " + request_username);

                                        holder.accept_btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                chatRequestRef.child(currentUserId).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {

                                                            chatRequestRef.child(list_user_id).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()) {

                                                                        Toasty.success(getContext(), "Request removed", Toast.LENGTH_SHORT).show();

                                                                    }

                                                                }
                                                            });

                                                        }

                                                    }
                                                });

                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                        Toasty.error(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });


                            }


                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        Toasty.error(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                RequestViewHolder requestViewHolder = new RequestViewHolder(view);
                return requestViewHolder;

            }
        };

        myRequestList.setAdapter(adapter);
        adapter.startListening();


    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        private TextView userName, userStatus;
        private CircleImageView userImage;
        private Button accept_btn, cancel_btn;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            userImage = itemView.findViewById(R.id.user_profile_image);
            accept_btn = itemView.findViewById(R.id.accept_request_btn);
            cancel_btn = itemView.findViewById(R.id.decline_request_btn);


        }
    }

}

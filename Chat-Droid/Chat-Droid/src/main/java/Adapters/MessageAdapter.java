package com.developer.coreandroidx.whatsappclone.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developer.coreandroidx.whatsappclone.Activities.ImageViewerActivity;
import com.developer.coreandroidx.whatsappclone.Activities.MainActivity;
import com.developer.coreandroidx.whatsappclone.Models.Messages;
import com.developer.coreandroidx.whatsappclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private Context context;

    public MessageAdapter(List<Messages> userMessageList, Context context) {
        this.userMessageList = userMessageList;
        this.context = context;

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_message_layout, parent, false);
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference();
        return new MessageViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(position);

        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("image")) {

                    String receiverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.ic_profile).into(holder.receiverProfileImage);

                } else {

                    Picasso.get().load(R.drawable.ic_profile).into(holder.receiverProfileImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(context, "Adapter Error : " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);

        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);

        if (fromMessageType.equals("text")) {

            if (fromUserId.equals(messageSenderID)) {

                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());

            } else {

                //holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());

            }

        } else if (fromMessageType.equals("image")) {

            if (fromUserId.equals(messageSenderID)) {

                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);

            } else {

                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);

            }

        } else if (fromMessageType.equals("pdf") || fromMessageType.equals("docs")) {

            if (fromUserId.equals(messageSenderID)) {

                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                holder.messageSenderPicture.setBackgroundResource(R.drawable.ic_document);

                //  Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whatsaapp-clone.appspot.com/o/Image-Files%2Fic_document.xml?alt=media&token=58c66e38-f7c8-460e-8cd8-482357beb38f").into(holder.messageSenderPicture);

            } else {

                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);
                holder.messageReceiverPicture.setBackgroundResource(R.drawable.ic_document);
                // Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whatsaapp-clone.appspot.com/o/Image-Files%2Fic_document.xml?alt=media&token=58c66e38-f7c8-460e-8cd8-482357beb38f").into(holder.messageSenderPicture);

            }
        }


        if (fromUserId.equals(messageSenderID)) {

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docs")) {

                        CharSequence options[] = new CharSequence[]{

                                "Delete for me",
                                "Download and view document",
                                "Cancel",
                                "Delete for everyone"

                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {

                                    deleteSentMessage(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if (which == 1) {

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                if (which == 3) {

                                    deleteMessageForEveryone(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    } else if (userMessageList.get(position).getType().equals("text")) {

                        CharSequence options[] = new CharSequence[]{

                                "Delete for me",
                                "Delete for everyone",
                                "Cancel"

                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {

                                    deleteSentMessage(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if (which == 2) {

                                    deleteMessageForEveryone(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();

                    } else if (userMessageList.get(position).getType().equals("image")) {

                        CharSequence options[] = new CharSequence[]{

                                "Delete for me",
                                "View this image",
                                "Cancel",
                                "Delete for everyone"

                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {

                                    deleteSentMessage(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if (which == 1) {

                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                }if (which == 3) {

                                    deleteMessageForEveryone(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();

                    }

                }
            });

        } else {

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docs")) {

                        CharSequence options[] = new CharSequence[]{

                                "Delete for me",
                                "Download and view document",
                                "Cancel",

                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {

                                    deleteReceiveMessage(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if (which == 1) {

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                if (which == 2) {


                                }
                            }
                        });
                        builder.show();
                    } else if (userMessageList.get(position).getType().equals("text")) {

                        CharSequence options[] = new CharSequence[]{

                                "Delete for me",
                                "Cancel"

                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {

                                    deleteReceiveMessage(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();

                    } else if (userMessageList.get(position).getType().equals("image")) {

                        CharSequence options[] = new CharSequence[]{

                                "Delete for me",
                                "View this image",
                                "Cancel",

                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {

                                    deleteReceiveMessage(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if (which == 1) {

                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();

                    }

                }
            });


        }

    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    private void deleteReceiveMessage (int position, final MessageViewHolder holder) {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message ")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Toasty.success(holder.itemView.getContext(), "Deleted", Toast.LENGTH_SHORT).show();

                } else {

                    Toasty.error(holder.itemView.getContext(), "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }

            }
        });

    }


    private void deleteSentMessage (int position, final MessageViewHolder holder) {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message ")
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Toasty.success(holder.itemView.getContext(), "Deleted", Toast.LENGTH_SHORT).show();

                } else {

                    Toasty.error(holder.itemView.getContext(), "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

    private void deleteMessageForEveryone (final int position, final MessageViewHolder holder) {

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message ")
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    rootRef.child("Message ")
                            .child(userMessageList.get(position).getTo())
                            .child(userMessageList.get(position).getFrom())
                            .child(userMessageList.get(position).getMessageId())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                Toasty.success(holder.itemView.getContext(), "Deleted", Toast.LENGTH_SHORT).show();

                            } else {

                                Toasty.error(holder.itemView.getContext(), "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                            }


                        }
                    });


                } else {

                    Toasty.error(holder.itemView.getContext(), "Error : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }

            }
        });

    }


}

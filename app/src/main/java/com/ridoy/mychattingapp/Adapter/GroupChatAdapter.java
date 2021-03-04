package com.ridoy.mychattingapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ridoy.mychattingapp.Model.Messagemodel;
import com.ridoy.mychattingapp.Model.Userinformation;
import com.ridoy.mychattingapp.R;
import com.ridoy.mychattingapp.databinding.DeleteDialogBinding;
import com.ridoy.mychattingapp.databinding.GroupReceiverLayoutBinding;
import com.ridoy.mychattingapp.databinding.GroupSenderLayoutBinding;
import com.ridoy.mychattingapp.databinding.ReceiverLayoutBinding;
import com.ridoy.mychattingapp.databinding.SenderLayoutBinding;

import java.util.ArrayList;

public class GroupChatAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Messagemodel> messagemodels;

    final int senderView=1;
    final int ReceiverView=2;

    public GroupChatAdapter(Context context, ArrayList<Messagemodel> messagemodels) {
        this.context = context;
        this.messagemodels = messagemodels;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==senderView){
            View view= LayoutInflater.from(context).inflate(R.layout.group_sender_layout,parent,false);
            return new SenderViewholder(view);
        }else {
            View view=LayoutInflater.from(context).inflate(R.layout.group_receiver_layout,parent,false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Messagemodel message=messagemodels.get(position);

        int reactionsarray[]=new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactionsarray)
                .build();
        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if (holder.getClass()==SenderViewholder.class){
                SenderViewholder senderViewholder= (SenderViewholder) holder;
                senderViewholder.senderLayoutBinding.sentfeelings.setImageResource(reactionsarray[pos]);
                senderViewholder.senderLayoutBinding.sentfeelings.setVisibility(View.VISIBLE);
            }else {
                ReceiverViewHolder receiverViewHolder= (ReceiverViewHolder) holder;
                receiverViewHolder.receiverLayoutBinding.receiverfeelings.setImageResource(reactionsarray[pos]);
                receiverViewHolder.receiverLayoutBinding.receiverfeelings.setVisibility(View.VISIBLE);
            }
            message.setFeeling(pos);
            FirebaseDatabase.getInstance().getReference()
                    .child("GroupChats")
                    .child(message.getMsgid())
                    .setValue(message);

            return true; // true is closing popup, false is requesting a new selection
        });


        if (holder.getClass()==SenderViewholder.class){

            SenderViewholder senderViewholder= (SenderViewholder) holder;

            if (message.getMessage().equals("Photo")){
                senderViewholder.senderLayoutBinding.sentimageView.setVisibility(View.VISIBLE);
                senderViewholder.senderLayoutBinding.sentmsg.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(senderViewholder.senderLayoutBinding.sentimageView);

            }
            senderViewholder.senderLayoutBinding.sentmsg.setText(message.getMessage());
            FirebaseDatabase.getInstance()
                    .getReference().child("Users")
                    .child(message.getSenderid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                Userinformation user = snapshot.getValue(Userinformation.class);
                                senderViewholder.senderLayoutBinding.gsenderusername.setText("@" + user.getName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            if (message.getFeeling()>=0){
                senderViewholder.senderLayoutBinding.sentfeelings.setImageResource(reactionsarray[message.getFeeling()]);
                senderViewholder.senderLayoutBinding.sentfeelings.setVisibility(View.VISIBLE);
            }else if (message.getFeeling()==-2){
                senderViewholder.senderLayoutBinding.sentfeelings.setVisibility(View.GONE);
            }
            senderViewholder.senderLayoutBinding.sentfeelings.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
            senderViewholder.senderLayoutBinding.sentimageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });

            senderViewholder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
                    DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle("Delete Message")
                            .setView(binding.getRoot())
                            .create();
                    dialog.show();
                    binding.everyone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            message.setMessage("This message is removed...");
                            message.setFeeling(-2);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("GroupChats")
                                    .child(message.getMsgid()).setValue(message);
                            dialog.dismiss();
                        }
                    });
                    binding.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("GroupChats")
                                    .child(message.getMsgid()).setValue(null);
                            dialog.dismiss();
                        }
                    });
                    binding.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    return false;
                }
            });

        }else {
            ReceiverViewHolder receiverViewHolder= (ReceiverViewHolder) holder;
            if (message.getMessage().equals("Photo")){
                receiverViewHolder.receiverLayoutBinding.receiverimageView.setVisibility(View.VISIBLE);
                receiverViewHolder.receiverLayoutBinding.receivermsg.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(receiverViewHolder.receiverLayoutBinding.receiverimageView);
            }

            FirebaseDatabase.getInstance()
                    .getReference().child("users")
                    .child(message.getSenderid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                Userinformation user = snapshot.getValue(Userinformation.class);
                                receiverViewHolder.receiverLayoutBinding.grevusername.setText("@" + user.getName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            receiverViewHolder.receiverLayoutBinding.receivermsg.setText(message.getMessage());
            if (message.getFeeling()>=0){
                receiverViewHolder.receiverLayoutBinding.receiverfeelings.setImageResource(reactionsarray[message.getFeeling()]);
                receiverViewHolder.receiverLayoutBinding.receiverfeelings.setVisibility(View.VISIBLE);
            }else if (message.getFeeling()==2){
                receiverViewHolder.receiverLayoutBinding.receiverfeelings.setVisibility(View.GONE);
            }
            receiverViewHolder.receiverLayoutBinding.receiverfeelings.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
            receiverViewHolder.receiverLayoutBinding.receiverimageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return messagemodels.size();
    }

    @Override
    public int getItemViewType(int position) {
        Messagemodel model=messagemodels.get(position);
        if (FirebaseAuth.getInstance().getUid().equals(model.getSenderid())){
            return senderView;
        }else {
            return ReceiverView;
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        GroupReceiverLayoutBinding receiverLayoutBinding;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverLayoutBinding=GroupReceiverLayoutBinding.bind(itemView);
        }
    }

    public class SenderViewholder extends RecyclerView.ViewHolder {

        GroupSenderLayoutBinding senderLayoutBinding;
        public SenderViewholder(@NonNull View itemView) {
            super(itemView);
            senderLayoutBinding=GroupSenderLayoutBinding.bind(itemView);
        }
    }

}

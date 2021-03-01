package com.ridoy.mychattingapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.ridoy.mychattingapp.Model.Messagemodel;
import com.ridoy.mychattingapp.R;
import com.ridoy.mychattingapp.databinding.ReceiverLayoutBinding;
import com.ridoy.mychattingapp.databinding.SenderLayoutBinding;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Messagemodel> messagemodels;
    String senderRoom,receiverRoom;

    final int senderView=1;
    final int ReceiverView=2;

    public ChatAdapter(Context context, ArrayList<Messagemodel> messagemodels, String senderRoom, String receiverRoom) {
        this.context = context;
        this.messagemodels = messagemodels;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==senderView){
            View view= LayoutInflater.from(context).inflate(R.layout.sender_layout,parent,false);
            return new SenderViewholder(view);
        }else {
            View view=LayoutInflater.from(context).inflate(R.layout.receiver_layout,parent,false);
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
                    .child("Chats")
                    .child(senderRoom)
                    .child("Messages")
                    .child(message.getMsgid())
                    .setValue(message);
            FirebaseDatabase.getInstance().getReference()
                    .child("Chats")
                    .child(receiverRoom)
                    .child("Messages")
                    .child(message.getMsgid())
                    .setValue(message);

            return true; // true is closing popup, false is requesting a new selection
        });


        if (holder.getClass()==SenderViewholder.class){
            SenderViewholder senderViewholder= (SenderViewholder) holder;
            senderViewholder.senderLayoutBinding.sentmsg.setText(message.getMessage());
            if (message.getFeeling()>=0){
                senderViewholder.senderLayoutBinding.sentfeelings.setImageResource(reactionsarray[message.getFeeling()]);
                senderViewholder.senderLayoutBinding.sentfeelings.setVisibility(View.VISIBLE);
            }else {
                senderViewholder.senderLayoutBinding.sentfeelings.setVisibility(View.GONE);
            }
            senderViewholder.senderLayoutBinding.sentmsg.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
        }else {
            ReceiverViewHolder receiverViewHolder= (ReceiverViewHolder) holder;
            receiverViewHolder.receiverLayoutBinding.receivermsg.setText(message.getMessage());
            if (message.getFeeling()>=0){
                receiverViewHolder.receiverLayoutBinding.receiverfeelings.setImageResource(reactionsarray[message.getFeeling()]);
                receiverViewHolder.receiverLayoutBinding.receiverfeelings.setVisibility(View.VISIBLE);
            }else {
                receiverViewHolder.receiverLayoutBinding.receiverfeelings.setVisibility(View.GONE);
            }
            receiverViewHolder.receiverLayoutBinding.receivermsg.setOnTouchListener(new View.OnTouchListener() {
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
        ReceiverLayoutBinding receiverLayoutBinding;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverLayoutBinding=ReceiverLayoutBinding.bind(itemView);
        }
    }

    public class SenderViewholder extends RecyclerView.ViewHolder {

        SenderLayoutBinding senderLayoutBinding;
        public SenderViewholder(@NonNull View itemView) {
            super(itemView);
            senderLayoutBinding=SenderLayoutBinding.bind(itemView);
        }
    }

}

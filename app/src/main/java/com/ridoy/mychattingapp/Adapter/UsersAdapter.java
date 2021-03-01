package com.ridoy.mychattingapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ridoy.mychattingapp.ChatActivity;
import com.ridoy.mychattingapp.Model.Userinformation;
import com.ridoy.mychattingapp.R;
import com.ridoy.mychattingapp.databinding.ConversationLayoutBinding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    Context context;
    ArrayList<Userinformation> userinformations;

    public UsersAdapter() {
    }

    public UsersAdapter(Context context, ArrayList<Userinformation> userinformations) {
        this.context = context;
        this.userinformations = userinformations;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.conversation_layout,parent,false);
        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {

        Userinformation userinformation=userinformations.get(position);

        String senderid= FirebaseAuth.getInstance().getUid();
        String senderoom=senderid+userinformation.getuId();
        String receiverroom=userinformation.getuId()+senderid;

        FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(senderoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        SimpleDateFormat dateFormat=new SimpleDateFormat("hh:mm a");
                        if (snapshot.exists()){
                            String lastmsg=snapshot.child("lastmsg").getValue(String.class);
                            long lastmsgtime=snapshot.child("lastmsgtime").getValue(Long.class);
                            holder.conversationLayoutBinding.conversationLastmsgTV.setText(lastmsg);
                            holder.conversationLayoutBinding.conversationLastmsgtimeTV.setText(dateFormat.format(new Date(lastmsgtime)));
                        }else {

                            holder.conversationLayoutBinding.conversationLastmsgTV.setText("Tap to Chat");
                            holder.conversationLayoutBinding.conversationLastmsgtimeTV.setText(dateFormat.format(new Date()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.conversationLayoutBinding.conversationUsernameTV.setText(userinformation.getName());
        Glide.with(context).load(userinformation.getImageUrl()).placeholder(R.drawable.avatar).into(holder.conversationLayoutBinding.conversationUserimage);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context,ChatActivity.class);
                intent.putExtra("name",userinformation.getName());
                intent.putExtra("uid",userinformation.getuId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userinformations.size();
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {
        ConversationLayoutBinding conversationLayoutBinding;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            conversationLayoutBinding=ConversationLayoutBinding.bind(itemView);

        }
    }
}

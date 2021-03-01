package com.ridoy.mychattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ridoy.mychattingapp.Adapter.ChatAdapter;
import com.ridoy.mychattingapp.Model.Messagemodel;
import com.ridoy.mychattingapp.databinding.ActivityChatBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding activityChatBinding;
    ChatAdapter chatAdapter;
    ArrayList<Messagemodel> msgs;
    FirebaseDatabase database;
    String senderRoom, receiverRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityChatBinding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(activityChatBinding.getRoot());

        database = FirebaseDatabase.getInstance();

        String name = getIntent().getStringExtra("name");
        String chatuseruid = getIntent().getStringExtra("uid");

        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String currentuid = FirebaseAuth.getInstance().getUid();
        senderRoom = currentuid + chatuseruid;
        receiverRoom = chatuseruid + currentuid;
        msgs = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, msgs, senderRoom, receiverRoom);

        activityChatBinding.sentbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = activityChatBinding.msgbox.getText().toString();
                Date date = new Date();
                Messagemodel messagemodel = new Messagemodel(message, currentuid, date.getTime());
                activityChatBinding.msgbox.setText("");
                String randomkey = database.getReference().push().getKey();
                HashMap<String, Object> map=new HashMap<>();
                map.put("lastmsg",messagemodel.getMessage());
                map.put("lastmsgtime",date.getTime());

                database.getReference().child("Chats").child(senderRoom).updateChildren(map);
                database.getReference().child("Chats").child(receiverRoom).updateChildren(map);
                database.getReference().child("Chats").child(senderRoom).child("Messages").child(randomkey)
                        .setValue(messagemodel).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        database.getReference().child("Chats").child(receiverRoom).child("Messages").child(randomkey)
                                .setValue(messagemodel).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });
                    }
                });
            }
        });
        database.getReference().child("Chats").child(senderRoom).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                msgs.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Messagemodel mssages = dataSnapshot.getValue(Messagemodel.class);
                    mssages.setMsgid(dataSnapshot.getKey());
                    msgs.add(mssages);
                }
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        activityChatBinding.chatrv.setAdapter(chatAdapter);

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
package com.ridoy.mychattingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ridoy.mychattingapp.Adapter.ChatAdapter;
import com.ridoy.mychattingapp.Model.Messagemodel;
import com.ridoy.mychattingapp.databinding.ActivityChatBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding activityChatBinding;
    ChatAdapter chatAdapter;
    ArrayList<Messagemodel> msgs;

    FirebaseDatabase database;
    FirebaseStorage storage;

    String senderRoom, receiverRoom,currentuid;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityChatBinding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(activityChatBinding.getRoot());

        setSupportActionBar(activityChatBinding.chattoolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        dialog=new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Image Sending...");

        database = FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();

        String name = getIntent().getStringExtra("name");
        String chatuseruid = getIntent().getStringExtra("uid");
        String image = getIntent().getStringExtra("image");

        Glide.with(this).load(image).placeholder(R.drawable.avatar).into(activityChatBinding.profileImage);
        activityChatBinding.usernameTV.setText(name);

        database.getReference().child("OnlineStatus").child(chatuseruid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()){
                            String status=snapshot.getValue(String.class);
                            if (!status.isEmpty()){
                                if (status.equals("offline")){
                                    activityChatBinding.statusindicator.setVisibility(View.GONE);
                                }else {
                                    activityChatBinding.statusindicator.setText(status);
                                    activityChatBinding.statusindicator.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        activityChatBinding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        currentuid = FirebaseAuth.getInstance().getUid();
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

        activityChatBinding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,69);
            }
        });

        Handler handler=new Handler();

        activityChatBinding.msgbox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("OnlineStatus")
                        .child(FirebaseAuth.getInstance().getUid())
                        .setValue("Typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userstoptyping,1000);

            }
            Runnable userstoptyping=new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("OnlineStatus")
                            .child(FirebaseAuth.getInstance().getUid())
                            .setValue("Online");
                }
            };
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data!=null){
            if (data.getData()!=null){
                Uri selectedImagepath=data.getData();
                Calendar calendar=Calendar.getInstance();
                dialog.show();
                StorageReference reference=storage.getReference().child("ChatsImages").child(calendar.getTimeInMillis()+"");

                reference.putFile(selectedImagepath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()){
                            dialog.dismiss();
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String filepath=uri.toString();

                                    String message = activityChatBinding.msgbox.getText().toString();
                                    Date date = new Date();
                                    Messagemodel messagemodel = new Messagemodel(message, currentuid, date.getTime());
                                    messagemodel.setMessage("Photo");
                                    messagemodel.setImageUrl(filepath);

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
                        }

                    }
                });

            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("OnlineStatus")
                .child(currentId)
                .setValue("Online");

    }
    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("OnlineStatus")
                .child(currentId)
                .setValue("offline");
    }
}
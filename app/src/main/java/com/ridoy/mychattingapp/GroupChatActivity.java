package com.ridoy.mychattingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

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
import com.ridoy.mychattingapp.Adapter.GroupChatAdapter;
import com.ridoy.mychattingapp.Model.Messagemodel;
import com.ridoy.mychattingapp.databinding.ActivityChatBinding;
import com.ridoy.mychattingapp.databinding.ActivityGroupChatBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {
    ActivityGroupChatBinding activityGroupChatBinding;

    GroupChatAdapter groupChatAdapter;
    ArrayList<Messagemodel> msgs;

    FirebaseDatabase database;
    FirebaseStorage storage;

    String senderUid;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityGroupChatBinding=ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(activityGroupChatBinding.getRoot());

        getSupportActionBar().setTitle("Group Chats");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();

        senderUid= FirebaseAuth.getInstance().getUid();

        dialog=new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Image Sending...");

        msgs = new ArrayList<>();
        groupChatAdapter = new GroupChatAdapter(this, msgs);

        activityGroupChatBinding.sentbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = activityGroupChatBinding.msgbox.getText().toString();

                Date date = new Date();
                Messagemodel messagemodel = new Messagemodel(message, senderUid, date.getTime());
                activityGroupChatBinding.msgbox.setText("");

                database.getReference().child("GroupChats").push()
                        .setValue(messagemodel).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });
            }
        });

        database.getReference().child("GroupChats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                msgs.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Messagemodel mssages = dataSnapshot.getValue(Messagemodel.class);
                    mssages.setMsgid(dataSnapshot.getKey());
                    msgs.add(mssages);
                }
                groupChatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        activityGroupChatBinding.chatrv.setAdapter(groupChatAdapter);
        activityGroupChatBinding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,69);
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data!=null){
            if (data.getData()!=null){
                Uri selectedImagepath=data.getData();
                Calendar calendar=Calendar.getInstance();
                dialog.show();
                StorageReference reference=storage.getReference().child("GroupChatsImages").child(calendar.getTimeInMillis()+"");

                reference.putFile(selectedImagepath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()){
                            dialog.dismiss();
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String filepath=uri.toString();

                                    String message = activityGroupChatBinding.msgbox.getText().toString();
                                    Date date = new Date();

                                    Messagemodel messagemodel = new Messagemodel(message, senderUid, date.getTime());
                                    messagemodel.setMessage("Photo");
                                    messagemodel.setImageUrl(filepath);

                                    activityGroupChatBinding.msgbox.setText("");

                                    database.getReference().child("GroupChats").push()
                                            .setValue(messagemodel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
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
}
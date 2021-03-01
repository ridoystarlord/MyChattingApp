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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ridoy.mychattingapp.Model.Userinformation;
import com.ridoy.mychattingapp.databinding.ActivitySetupProfileBinding;

public class SetupProfileActivity extends AppCompatActivity {

    ActivitySetupProfileBinding activitySetupProfileBinding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySetupProfileBinding=ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(activitySetupProfileBinding.getRoot());
        getSupportActionBar().hide();
        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();

        dialog=new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Profile Updating...");

        activitySetupProfileBinding.setupprofileProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,45);
            }
        });
        activitySetupProfileBinding.goToDashboardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=activitySetupProfileBinding.nameboxET.getText().toString();
                if (name.isEmpty()){
                    activitySetupProfileBinding.nameboxET.setError("Please, Enter Your Name");
                    return;
                }
                dialog.show();
                if (selectedImage!=null){
                    StorageReference reference=storage.getReference().child("ProfilePhotos").child(auth.getUid());
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageURL=uri.toString();
                                        String uid=auth.getUid();
                                        String phonenumber=auth.getCurrentUser().getPhoneNumber();
                                        Userinformation userinformation=new Userinformation(name,imageURL,uid,phonenumber);
                                        database.getReference().child("Users").child(uid).setValue(userinformation).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                dialog.dismiss();
                                                startActivity(new Intent(SetupProfileActivity.this,MainActivity.class));
                                                finishAffinity();
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }else {
                    String imageURL="No Images Found";
                    String uid=auth.getUid();
                    String phonenumber=auth.getCurrentUser().getPhoneNumber();
                    Userinformation userinformation=new Userinformation(name,imageURL,uid,phonenumber);
                    database.getReference().child("Users").child(uid).setValue(userinformation).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dialog.dismiss();
                            startActivity(new Intent(SetupProfileActivity.this,MainActivity.class));
                            finishAffinity();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        selectedImage=data.getData();
        if (data!=null){
            if (selectedImage!=null){
                activitySetupProfileBinding.setupprofileProfileImage.setImageURI(selectedImage);
            }
        }
    }
}
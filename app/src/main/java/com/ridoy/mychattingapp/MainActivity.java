
package com.ridoy.mychattingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ridoy.mychattingapp.Adapter.TopstatusAdapter;
import com.ridoy.mychattingapp.Adapter.UsersAdapter;
import com.ridoy.mychattingapp.Model.StatusModel;
import com.ridoy.mychattingapp.Model.UserStatusModel;
import com.ridoy.mychattingapp.Model.Userinformation;
import com.ridoy.mychattingapp.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding activityMainBinding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ProgressDialog dialog;
    Userinformation userinfo;

    ArrayList<Userinformation> users;
    ArrayList<UserStatusModel> usersstatus;

    UsersAdapter usersAdapter;
    TopstatusAdapter topstatusAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();

        dialog=new ProgressDialog(this);
        dialog.setMessage("Status Updating...");
        dialog.setCancelable(false);

        users=new ArrayList<>();
        usersstatus=new ArrayList<>();

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userinfo=snapshot.getValue(Userinformation.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        topstatusAdapter=new TopstatusAdapter(this,usersstatus);
        usersAdapter=new UsersAdapter(this,users);

        activityMainBinding.rv.setAdapter(usersAdapter);
        activityMainBinding.statusrv.setAdapter(topstatusAdapter);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        activityMainBinding.statusrv.setLayoutManager(linearLayoutManager);

        activityMainBinding.rv.showShimmerAdapter();
        activityMainBinding.statusrv.showShimmerAdapter();

        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Userinformation userinformation=dataSnapshot.getValue(Userinformation.class);
                    if (userinformation.getuId().equals(auth.getUid())){
                        continue;
                    }else {
                        users.add(userinformation);
                    }
                }
                activityMainBinding.rv.hideShimmerAdapter();
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("Stories")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists())
                        {
                            usersstatus.clear();
                            for (DataSnapshot storySnapshot:snapshot.getChildren())
                            {
                                UserStatusModel statusModel=new UserStatusModel();

                                statusModel.setName(storySnapshot.child("name").getValue(String.class));
                                statusModel.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                                statusModel.setLastupdated(storySnapshot.child("lastupdated").getValue(long.class));

                                ArrayList<StatusModel> statusModelArrayList=new ArrayList<>();

                                for (DataSnapshot statussnapshot:storySnapshot.child("statusModels").getChildren())
                                {
                                    StatusModel samplestatus=statussnapshot.getValue(StatusModel.class);
                                    statusModelArrayList.add(samplestatus);
                                }
                                statusModel.setStatusModels(statusModelArrayList);
                                usersstatus.add(statusModel);
                            }
                            activityMainBinding.statusrv.hideShimmerAdapter();
                            topstatusAdapter.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        activityMainBinding.bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.bottom_menu_status:
                        Intent intent=new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent,55);
                        break;
                }
                return false;
            }
        });



    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.top_menu_search:
                Toast.makeText(this, "Search Click", Toast.LENGTH_SHORT).show();
                break;
            case R.id.top_menu_groups:
                Toast.makeText(this, "Group Click", Toast.LENGTH_SHORT).show();
                break;
            case R.id.top_menu_logout:
                auth.signOut();
                startActivity(new Intent(MainActivity.this,PhoneNumberActivity.class));
                finishAffinity();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data!=null){
            if (data.getData()!=null){
                dialog.show();
                FirebaseStorage storage=FirebaseStorage.getInstance();
                Date date=new Date();
                StorageReference reference=storage.getReference().child("StatusImages")
                        .child(date.getTime()+"");
                reference.putFile(data.getData())
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()){
                                    reference.getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {

                                                    UserStatusModel userStatusModel=new UserStatusModel();
                                                    userStatusModel.setName(userinfo.getName());
                                                    userStatusModel.setProfileImage(userinfo.getImageUrl());
                                                    userStatusModel.setLastupdated(date.getTime());

                                                    HashMap<String , Object> map=new HashMap<>();
                                                    map.put("name",userStatusModel.getName());
                                                    map.put("profileImage",userStatusModel.getProfileImage());
                                                    map.put("lastupdated",userStatusModel.getLastupdated());

                                                    StatusModel statusModel=new StatusModel(uri.toString(),userStatusModel.getLastupdated());

                                                    database.getReference().child("Stories")
                                                            .child(FirebaseAuth.getInstance().getUid())
                                                            .updateChildren(map);

                                                    database.getReference().child("Stories").child(FirebaseAuth.getInstance().getUid())
                                                            .child("statusModels")
                                                            .push()
                                                            .setValue(statusModel);

                                                    dialog.dismiss();
                                                }
                                            });
                                }
                            }
                        });

            }
        }
    }
}
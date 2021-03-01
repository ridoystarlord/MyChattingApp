package com.ridoy.mychattingapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ridoy.mychattingapp.MainActivity;
import com.ridoy.mychattingapp.Model.StatusModel;
import com.ridoy.mychattingapp.Model.UserStatusModel;
import com.ridoy.mychattingapp.R;
import com.ridoy.mychattingapp.databinding.StatusLayoutBinding;

import java.util.ArrayList;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class TopstatusAdapter extends RecyclerView.Adapter<TopstatusAdapter.TopStatusViewHolder> {

    Context context;
    ArrayList<UserStatusModel> userStatusModels;

    public TopstatusAdapter(Context context, ArrayList<UserStatusModel> userStatusModels) {
        this.context = context;
        this.userStatusModels = userStatusModels;
    }

    @NonNull
    @Override
    public TopStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.status_layout,parent,false);

        return new TopStatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopStatusViewHolder holder, int position) {

        UserStatusModel statusModel=userStatusModels.get(position);

        StatusModel model=statusModel.getStatusModels().get(statusModel.getStatusModels().size()-1);

        Glide.with(context).load(model.getImageUrl()).into(holder.statusLayoutBinding.circleimage);

        holder.statusLayoutBinding.circularStatusView.setPortionsCount(statusModel.getStatusModels().size());

        holder.statusLayoutBinding.circularStatusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<MyStory> myStories = new ArrayList<>();

                for(StatusModel story: statusModel.getStatusModels()){
                    myStories.add(new MyStory(
                            story.getImageUrl()
                    ));
                }
                new StoryView.Builder(((MainActivity)context).getSupportFragmentManager())
                        .setStoriesList(myStories) // Required
                        .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                        .setTitleText(statusModel.getName()) // Default is Hidden
                        .setSubtitleText("") // Default is Hidden
                        .setTitleLogoUrl(statusModel.getProfileImage()) // Default is Hidden
                        .setStoryClickListeners(new StoryClickListeners() {
                            @Override
                            public void onDescriptionClickListener(int position) {
                                //your action
                            }

                            @Override
                            public void onTitleIconClickListener(int position) {
                                //your action
                            }
                        }) // Optional Listeners
                        .build() // Must be called before calling show method
                        .show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return userStatusModels.size();
    }

    public class TopStatusViewHolder extends RecyclerView.ViewHolder {

        StatusLayoutBinding statusLayoutBinding;

        public TopStatusViewHolder(@NonNull View itemView) {
            super(itemView);
            statusLayoutBinding=StatusLayoutBinding.bind(itemView);
        }
    }

}

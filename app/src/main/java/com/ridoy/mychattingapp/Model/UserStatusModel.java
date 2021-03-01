package com.ridoy.mychattingapp.Model;

import java.util.ArrayList;

public class UserStatusModel {

    String name,profileImage;
    long lastupdated;
    ArrayList<StatusModel> statusModels;

    public UserStatusModel() {
    }

    public UserStatusModel(String name, String profileImage, long lastupdated, ArrayList<StatusModel> statusModels) {
        this.name = name;
        this.profileImage = profileImage;
        this.lastupdated = lastupdated;
        this.statusModels = statusModels;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public long getLastupdated() {
        return lastupdated;
    }

    public void setLastupdated(long lastupdated) {
        this.lastupdated = lastupdated;
    }

    public ArrayList<StatusModel> getStatusModels() {
        return statusModels;
    }

    public void setStatusModels(ArrayList<StatusModel> statusModels) {
        this.statusModels = statusModels;
    }
}

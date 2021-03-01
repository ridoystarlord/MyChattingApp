package com.ridoy.mychattingapp.Model;

public class Userinformation {

    private String name,imageUrl,uId,phoneNumber;

    public Userinformation(String name, String imageUrl, String uId, String phoneNumber) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.uId = uId;
        this.phoneNumber = phoneNumber;
    }

    public Userinformation() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}

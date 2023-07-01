package com.section34.journalApp.model;

//import com.google.cloud.firestore.Timestamp;


import com.google.firebase.Timestamp;

public class Journal {

private  String title ;
private  String description  ;
private  String imageUri  ;
private  String userId  ;
private Timestamp timeAdded  ;
private  String userName  ;


    public Journal() {
    }

    public Journal(String title, String description, String imageUri, String userId, Timestamp timeAdded, String userName) {
        this.title = title;
        this.description = description;
        this.imageUri = imageUri;
        this.userId = userId;
        this.timeAdded = timeAdded;
        this.userName = userName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}

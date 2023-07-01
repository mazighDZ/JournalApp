package com.section34.journalApp.util;

import android.app.Application;

//this an sure that only one single user at run time found in app
public class JournalUser extends Application {
    public String userName;
    private String userId;


    private  static JournalUser instance;


    // Singleton Class
    public static JournalUser getInstance(){

        if(instance == null){
            instance =  new JournalUser();
        }
        return instance;
    }

    public JournalUser() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}

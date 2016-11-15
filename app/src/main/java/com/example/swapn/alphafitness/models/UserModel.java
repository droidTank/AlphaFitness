package com.example.swapn.alphafitness.models;

import android.net.Uri;

/**
 * Created by swapn on 10/16/2016.
 */

public class UserModel {
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    private String uid;
    private String name;
    private String gender;
    private String email;
    private String height;
    private String weight;

    public boolean isAccount_setuped() {
        return account_setuped;
    }

    public void setAccount_setuped(boolean account_setuped) {
        this.account_setuped = account_setuped;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    private boolean account_setuped;

    public String getProf_pic() {
        return prof_pic;
    }

    public void setProf_pic(String prof_pic) {
        this.prof_pic = prof_pic;
    }

    private String prof_pic;

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public boolean insert_user() {
        //new FirebaseDb().getmDatabase().getReference("message");
       // mDatabase.child("users").
        return true;
    }
}

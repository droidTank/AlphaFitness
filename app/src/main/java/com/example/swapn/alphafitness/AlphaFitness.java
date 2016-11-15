package com.example.swapn.alphafitness;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by swapn on 10/16/2016.
 */

public class AlphaFitness extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}

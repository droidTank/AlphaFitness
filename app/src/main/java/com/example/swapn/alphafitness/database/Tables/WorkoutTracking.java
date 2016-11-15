package com.example.swapn.alphafitness.database.Tables;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.swapn.alphafitness.common.Constant;

/**
 * Created by swapn on 11/11/2016.
 */

public class WorkoutTracking {
    public static final String KEY_WORKOUTID = "id";
    public static final String KEY_WORKOUT_START = "workout_start";
    public static final String KEY_WORKOUT_END = "workout_end";
    public static final String KEY_STEPS = "steps";
    public static final String KEY_USERNAME = "username";

    private static final String LOG_TAG = "UserWorkouts";
    public static final String USER_WORKOUT_TABLE = "UserWorkouts";

    private int workout_id;
    private String workout_start;
    private String workout_end;

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    private String user_name;

    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + USER_WORKOUT_TABLE + " (" +
                    KEY_WORKOUTID + " INT PRIMARY KEY," +
                    KEY_WORKOUT_START + " DATETIME," +
                    KEY_WORKOUT_END + " DATETIME," +
                    KEY_USERNAME + " TEXT," +
                    KEY_STEPS +
                    " INT);";

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public String getWorkout_end() {
        return workout_end;
    }

    public void setWorkout_end(String workout_end) {
        this.workout_end = workout_end;
    }

    public String getWorkout_start() {
        return workout_start;
    }

    public void setWorkout_start(String workout_start) {
        this.workout_start = workout_start;
    }

    public int getWorkout_id() {
        return workout_id;
    }

    public void setWorkout_id(int workout_id) {
        this.workout_id = workout_id;
    }

    private int steps;


    public static void onCreate(SQLiteDatabase db) {
        Log.w(LOG_TAG, DATABASE_CREATE);
        db.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + USER_WORKOUT_TABLE);
        onCreate(db);
    }

    public static void setCurrentWorkoutid(Context context ,int id) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt(Constant.CURRENT_WORKOU_ID, id);
        editor.apply();
        editor.commit();
    }


    public static int getCurrentWorkoutid (Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return pref.getInt(Constant.CURRENT_WORKOU_ID, 0);
    }


}

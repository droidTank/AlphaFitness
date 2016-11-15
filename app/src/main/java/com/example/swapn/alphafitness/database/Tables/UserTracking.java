package com.example.swapn.alphafitness.database.Tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.swapn.alphafitness.common.Util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by swapn on 10/25/2016.
 */

public class UserTracking {
    public static final String KEY_USERID = "_id";
    public static final String KEY_WORKOUTID = "workout_id";
    public static final String KEY_STEPS = "steps";
    public static final String KEY_DAY = "date";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";


    private static final String LOG_TAG = "UserSteps";
    public static final String USER_TRACKING_TABLE = "UserSteps";

    public String user_id;
    public Double latitude;

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double longitude;

    public int getWorkout_id() {
        return workout_id;
    }

    public void setWorkout_id(int workout_id) {
        this.workout_id = workout_id;
    }

    public int workout_id;

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDate() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        String dateTemp = df.format(date).toString().trim();
        return dateTemp;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String steps;
    public Date date;

    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + USER_TRACKING_TABLE + " (" +
                    KEY_USERID + " TEXT," +
                    KEY_STEPS + " TEXT," +
                    KEY_WORKOUTID + " INT," +
                    KEY_LATITUDE + " REAL," +
                    KEY_LONGITUDE + " REAL," +
                    KEY_DAY +
                    " TEXT);";

    /**
     * No need to do anything, fields are already set to default values above
     */
    public UserTracking() {
    }



    /**
     * Convert information from the database into a Person object.
     */
    public UserTracking(final Cursor cursor) {
        // Indices expected to match order in FIELDS!
        this.user_id = cursor.getString(0);
        this.steps = cursor.getString(1);
        this.date = Util.stringToDate(cursor.getString(2));
    }

    public UserTracking(String user_id, String steps, Date date) {
        this.user_id = user_id;
        this.steps = steps;
        this.date = date;
    }



    /**
     * Return the fields in a ContentValues object, suitable for insertion
     * into the database.
     */
    public ContentValues getContent() {
        final ContentValues values = new ContentValues();
        // Note that ID is NOT included here
        values.put(KEY_USERID, user_id);
        values.put(KEY_STEPS, steps);
        values.put(KEY_WORKOUTID, workout_id);
        values.put(KEY_DAY, getDate());
        values.put(KEY_LATITUDE, latitude);
        values.put(KEY_LONGITUDE, longitude);
        return values;
    }

    public static void onCreate(SQLiteDatabase db) {
        Log.w(LOG_TAG, DATABASE_CREATE);
        db.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + USER_TRACKING_TABLE);
        onCreate(db);
    }
}

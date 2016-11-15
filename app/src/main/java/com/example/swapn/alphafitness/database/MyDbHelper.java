package com.example.swapn.alphafitness.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.swapn.alphafitness.common.Util;
import com.example.swapn.alphafitness.database.Tables.UserTracking;
import com.example.swapn.alphafitness.database.Tables.WorkoutTracking;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by swapn on 10/25/2016.
 */

public class MyDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "AlphaFitness";
    private static final int DATABASE_VERSION = 4;
    private static MyDbHelper sInstance;
    Util u = new Util();

    public static synchronized MyDbHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new MyDbHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public MyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        UserTracking.onCreate(db);
        WorkoutTracking.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        UserTracking.onUpgrade(db, oldVersion, newVersion);
        WorkoutTracking.onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Inserts a new entry in the database, if there is no entry for the given
     * date yet. Steps should be the current number of steps and it's negative
     * value will be used as offset for the new date. Also adds 'steps' steps to
     * the previous day, if there is an entry for that date.
     * <p/>
     * This method does nothing if there is already an entry for 'date' - use
     * {@link #updateSteps} in this case.
     * <p/>
     * To restore data from a backup, use {@link #insertDayFromBackup}
     *
     * @param date  the date in ms since 1970
     * @param steps the current step value to be used as negative offset for the
     *              new day; must be >= 0
     */
    public void insertNewDay(UserTracking user) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = user.getContent();
            long rowInserted = db.insert(UserTracking.USER_TRACKING_TABLE, null, values);
            if(rowInserted != -1)
                Log.d("Inserted", "I");
            else
                Log.d("Inserted", "NOT");
         //   getAllSteps(u.getStringDate(new Date()));
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        } finally {
            db.close();
        }
    }

    public void updateWorkoutSteps(int workout_id, int count, String username) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(WorkoutTracking.KEY_STEPS, count);
            int rows = db.update(WorkoutTracking.USER_WORKOUT_TABLE, values, WorkoutTracking.KEY_WORKOUTID + " = " + workout_id + " and " + WorkoutTracking.KEY_STEPS + "<>" + count + " and "
                                 + WorkoutTracking.KEY_USERNAME + " = '" + username + "'" , null);
            db.close();
        } catch (Exception e) {
            Log.e("Database", "Error Updating steps");
        }
    }

    public void updateWorkoutEndTime(int workout_id, String username) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(WorkoutTracking.KEY_WORKOUT_END, u.getStringDate(new Date()));
            db.update(WorkoutTracking.USER_WORKOUT_TABLE, values, WorkoutTracking.KEY_WORKOUTID + " = " + workout_id + " and " + WorkoutTracking.KEY_USERNAME + " = '" + username + "'", null);
            db.close();
        } catch (Exception e ) {
            Log.e("Database", "Error Updating End time");
        }
    }

    public int insertNewWorkout(String username) {

        try {
            SQLiteDatabase db = getWritableDatabase();
            int workoutid = this.getWorkoutCount() + 1;
            final ContentValues values = new ContentValues();
            // Note that ID is NOT included here
            values.put(WorkoutTracking.KEY_WORKOUTID, workoutid);
            values.put(WorkoutTracking.KEY_WORKOUT_START, u.getStringDate(new Date()));
            values.put(WorkoutTracking.KEY_USERNAME, username);
          //  values.put(WorkoutTracking.KEY_WORKOUT_END, u.getStringDate(new Date()));
            values.put(WorkoutTracking.KEY_STEPS, 0);
            long rowInserted = db.insertOrThrow(WorkoutTracking.USER_WORKOUT_TABLE, null, values);
            db.close();
            if(rowInserted != -1) {
                Log.d("Inserted", "I");
                return workoutid;
            }
            else {
                Log.d("Inserted", "NOT");
                return 0;
            }

        } catch (Exception e) {
            Log.e("Error", "Inserting new workout" + e.getMessage());
        } finally {
        }

        return 0;
    }

    public int getWorkoutCount() {
        try {
            String selectQuery = "SELECT  * FROM " + WorkoutTracking.USER_WORKOUT_TABLE;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor != null) {
                return cursor.getCount();
            }
        } catch (Exception e) {
            return 0;
        }

        return 0;
    }

    public long getTimeDifferenceforCurrentWorkout (int workout_id, String username) throws ParseException {
        String selectQuery = "SELECT  * FROM " + WorkoutTracking.USER_WORKOUT_TABLE + " where " + WorkoutTracking.KEY_WORKOUTID + "=" + workout_id
                + " AND " + WorkoutTracking.KEY_USERNAME + " = '" + username + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToNext()) {

                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date startDate = outputFormat.parse(cursor.getString((cursor.getColumnIndex(WorkoutTracking.KEY_WORKOUT_START))));
                Date endDate = outputFormat.parse(u.getStringDate(new Date()));
                return endDate.getTime() - startDate.getTime();
            }
        } catch (Exception e) {
            Log.e("Exception", "Exception in getTimeDifferenceforCurrentWorkout" + e.getMessage());
        }

        return 0;
    }

    public int getStepsforCurrentWorkout (int workout_id, String username ) {
        String selectQuery = "SELECT  * FROM " + WorkoutTracking.USER_WORKOUT_TABLE + " where " + WorkoutTracking.KEY_WORKOUTID + "=" + workout_id
                                + " AND " + WorkoutTracking.KEY_USERNAME + " = '" + username + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToNext()) {
                return Integer.parseInt(cursor.getString(cursor.getColumnIndex(UserTracking.KEY_STEPS)));
            }
        } catch(Exception e) {
            Log.e("Excpetion", "Exception in getStepsforCurrentWorkout");
        }
        return 0;
    }

    public ArrayList<WorkoutTracking> getWeekWorkouts(String username) {
        ArrayList<WorkoutTracking> array = new ArrayList<WorkoutTracking>();
        Date d = u.firstDayOfWeek(new Date());
        String daystart = u.editTime(u.getStringDate(d), "00:00:00");
        String dayend = u.editTime(u.getStringDate(new Date()), "23:59:59");
        String selectQuery = "SELECT  * FROM " + WorkoutTracking.USER_WORKOUT_TABLE + " where " + WorkoutTracking.KEY_WORKOUT_START + ">='" + daystart + "' AND " + WorkoutTracking.KEY_WORKOUT_START + "<='" + dayend
                + "' AND " +  WorkoutTracking.KEY_USERNAME + " = '" + username + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        while(cursor.moveToNext()) {
            WorkoutTracking w = new WorkoutTracking();
            w.setWorkout_id(cursor.getInt(cursor.getColumnIndex(WorkoutTracking.KEY_WORKOUTID)));
            w.setWorkout_start(cursor.getString(cursor.getColumnIndex(WorkoutTracking.KEY_WORKOUT_START)));
            w.setWorkout_end(cursor.getString(cursor.getColumnIndex(WorkoutTracking.KEY_WORKOUT_END)));
            w.setSteps(cursor.getInt(cursor.getColumnIndex(WorkoutTracking.KEY_STEPS)));
            array.add(w);
        }

        if(array.size() != 0)
            return array;
        else {
            return null;
        }
    }

    public ArrayList<UserTracking> getAllStep(int workout_id, String user) {
        ArrayList<UserTracking> array = new ArrayList<UserTracking>();
        String selectQuery = "SELECT  * FROM " + UserTracking.USER_TRACKING_TABLE + " where " + UserTracking.KEY_WORKOUTID + " = " + workout_id + " and " +
                                                                                                UserTracking.KEY_USERID + " = '" + user + "' ORDER BY " + UserTracking.KEY_STEPS;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);
            while (cursor.moveToNext()) {
                UserTracking w = new UserTracking();
                w.setWorkout_id(workout_id);
                w.setLatitude(cursor.getDouble(cursor.getColumnIndex(UserTracking.KEY_LATITUDE)));
                w.setLongitude(cursor.getDouble(cursor.getColumnIndex(UserTracking.KEY_LONGITUDE)));
                w.setSteps(cursor.getString(cursor.getColumnIndex(UserTracking.KEY_STEPS)));
                w.setDate(Util.stringToDate(cursor.getString(cursor.getColumnIndex(UserTracking.KEY_DAY))));
                w.setUser_id(user);

                array.add(w);
            }
        } catch (Exception e) {
            Log.e("Exception", "Exception in getAllStep" + e.getMessage());
        }

        if(array.size() != 0)
            return array;
        else {
            return null;
        }
    }

    public ArrayList<WorkoutTracking> getAllWorkout (String username) {
        ArrayList<WorkoutTracking> array = new ArrayList<WorkoutTracking>();
            String selectQuery = "SELECT  * FROM " + WorkoutTracking.USER_WORKOUT_TABLE + " where " + WorkoutTracking.KEY_USERNAME + " = '" + username + "'";
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
        while(cursor.moveToNext()) {
            WorkoutTracking w = new WorkoutTracking();
            w.setWorkout_id(cursor.getInt(cursor.getColumnIndex(WorkoutTracking.KEY_WORKOUTID)));
            w.setWorkout_start(cursor.getString(cursor.getColumnIndex(WorkoutTracking.KEY_WORKOUT_START)));
            w.setWorkout_end(cursor.getString(cursor.getColumnIndex(WorkoutTracking.KEY_WORKOUT_END)));
            w.setSteps(cursor.getInt(cursor.getColumnIndex(WorkoutTracking.KEY_STEPS)));
            array.add(w);
        }

        if(array.size() != 0)
            return array;
        else {
            return null;
        }
    }


    public boolean insertOrUpdateDayCount(UserTracking user) {
        int counter = ExistsDayTracking(user.getDate());
        Log.d("Count", Integer.toString(counter));
        if(counter == 0) {
            user.setSteps(user.getSteps());
            insertNewDay(user);
            return true;
        } else {
            updateUserDaySteps(user.getDate(), user.getSteps());
            return false;
        }
    }

    public void updateUserDaySteps(String day, String counter) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues newValues = new ContentValues();
            newValues.put(UserTracking.KEY_STEPS, counter);
            db.update(UserTracking.USER_TRACKING_TABLE, newValues, UserTracking.KEY_DAY +"='" + day + "'" , null);
        } finally {
            db.close();
        }
    }

    public Cursor getDataBetweenDates(String daystart, String dayend) {
        String selectQuery = "SELECT  * FROM " + UserTracking.USER_TRACKING_TABLE + " where " + UserTracking.KEY_DAY + ">='" + daystart + "' AND " + UserTracking.KEY_DAY + "<='" + dayend + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        return cursor;
    }


    public void getAllSteps(String day) {
        String dayend =  u.editTime(day, "23:59:59");// "Thursday 10 Nov 2016 00:00:00";"thursday 10 nov 2016 23:59:59";
        String daystart = u.editTime(day, "00:00:00");// "Thursday 10 Nov 2016 00:00:00";
        Cursor cursor = this.getDataBetweenDates(daystart, dayend);
        while (cursor.moveToNext()) {
        }
    }

    public int ExistsDayTracking(String day) {
        String dayend =  u.editTime(day, "23:59:59:000");// "Thursday 10 Nov 2016 00:00:00";"thursday 10 nov 2016 23:59:59";
        String dayStart = u.editTime(day, "00:00:00:000");// "Thursday 10 Nov 2016 00:00:00";
        String selectQuery = "SELECT  * FROM " + UserTracking.USER_TRACKING_TABLE + " where " + UserTracking.KEY_DAY + ">='" + dayStart + "' AND " + UserTracking.KEY_DAY + "<='" + dayend + "'";
        SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.getCount() > 0) {
                return cursor.getCount();
            }
        return 0;
    }

}

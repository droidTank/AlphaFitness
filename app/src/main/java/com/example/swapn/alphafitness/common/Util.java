package com.example.swapn.alphafitness.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.swapn.alphafitness.database.Tables.WorkoutTracking;
import com.example.swapn.alphafitness.models.UserModel;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by swapn on 10/26/2016.
 */

public class Util {
    private static double step_to_distance = 0.000466028; // Considering 75 cm per second, converting second to miles
   // private static int step_to_time = 2;
    //private float step_to_calory;
    private static DecimalFormat df = new DecimalFormat("#.0000");
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static Context context;
    private static int step_to_calory = 38; // Per 1000 steps

    public static void setContext(Context cont) {
        context = cont;
    }

    public static Context getContext() {
        return context;
    }

    public static Date stringToDate (String dt ) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        Date date = new Date();
        try {
            date = df.parse(dt);
        } catch (ParseException e) {
            Log.e("errorParseDate", "Error in parsing date");
        }

        return date;
    }

    public static boolean isRunning(Context ctx) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
        return pref.getBoolean(Constant.PREF_IS_RUNNING, false);
    }

    public static void setRunning(boolean running) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(Util.getContext());
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean(Constant.PREF_IS_RUNNING, running);
        editor.apply();
        editor.commit();
    }

    public static String returnDistance (int counter) {
        return df.format(counter * step_to_distance);
    }

    public static String returnTruncDouble (Double value) {
        return df.format(value);
    }

    public static String getCaloriesBurnt (int steps) {
        return df.format(step_to_calory * (((double) steps)/1000));
    }

  /*  public static String timeConversion(int counter) {
        int totalSeconds = counter * step_to_time;
        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        int seconds = totalSeconds % SECONDS_IN_A_MINUTE;
        int totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
        int minutes = totalMinutes % MINUTES_IN_AN_HOUR;
        int hours = totalMinutes / MINUTES_IN_AN_HOUR;

        return hours + ":" + minutes + ":" + seconds;
    } */

    public void setSharedPreferences (Context context, UserModel user) {
        SharedPreferences mPrefs = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        prefsEditor.putString("user", json);
        prefsEditor.commit();
    }


    public UserModel getUserFromSHaredPreference (Context context) {
        SharedPreferences mPrefs = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("user", "");
        UserModel obj = gson.fromJson(json, UserModel.class);
        return obj;
    }

    public String editTime (String date, String setime) {
        String[] time = date.split(" ");
        return time[0]  + " " + setime;
    }


    public String getStringDate(Date date) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
            String dateTemp = df.format(date).toString().trim();
            return dateTemp;
    }

    public  Date firstDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, 1);
        return calendar.getTime();
    }

    public Date[] getDaysOfWeek(Date refDate, int firstDayOfWeek) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(refDate);
        calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
        Date[] daysOfWeek = new Date[7];
        for (int i = 0; i < 7; i++) {
            daysOfWeek[i] = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return daysOfWeek;
    }

    public static String millisecToHours (long millisec) {
        int secs = (int) (millisec / 1000);
        int mins = secs / 60;
        int hrs = mins /60;
        mins = mins % 60;
        secs = secs % 60;
        int milliseconds = (int) (millisec % 1000);
        return Integer.toString(hrs) + " hrs " + mins + " min " + secs +" secs ";
    }

    public static int getStepCountFromWorkout(ArrayList<WorkoutTracking> array) {
        int stepcount = 0;
        if(array == null) {
            return 0;
        }
        for(WorkoutTracking w : array) {
            stepcount += w.getSteps();
        }
        return stepcount;
    }

    public static String getTotalTimeFromWorkouts(ArrayList<WorkoutTracking> array) {
        return "00 hr 00 min 00 sec";
    }

    public static double getSpeed(int count, int minutes) {
        Double distance = Double.parseDouble(returnDistance(count));
        Double speed = distance / minutes;
        return speed;
    }

}

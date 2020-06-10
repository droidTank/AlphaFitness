package com.example.swapn.alphafitness.services;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.swapn.alphafitness.IStepCounterInterface;
import com.example.swapn.alphafitness.R;
import com.example.swapn.alphafitness.common.Constant;
import com.example.swapn.alphafitness.common.Util;
import com.example.swapn.alphafitness.database.MyDbHelper;
import com.example.swapn.alphafitness.database.Tables.UserTracking;
import com.example.swapn.alphafitness.database.Tables.WorkoutTracking;
import com.example.swapn.alphafitness.fragments.RecordWorkFragment;
import com.example.swapn.alphafitness.fragments.WorkOutDetailFragment;
import com.example.swapn.alphafitness.models.UserModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class StepCounterService extends Service implements SensorEventListener {
    public StepCounterService() {
    }

    IStepCounterInterface.Stub mBinder;
    private SensorManager sensorManager;
    private MyDbHelper db;
    private static boolean mRunning;
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    UserModel userData;
    Util u;
    boolean oncreateflag = false;
    public static int MY_PERMISSION_REQUEST_READ_FINE_LOCATION = 5;

    public boolean isRecording() {
        return isRunning(getApplicationContext());
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    private boolean isRecording;
    Intent indentService;
    private static final int numberOfStepsToUpdateMap = 10;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;
    Sensor stepSensor;

    Intent intent;
    int counter = 0;

    final Handler handler = new Handler();
    Timer timer = new Timer();
    TimerTask backtask = new TimerTask() {
        @Override
        public void run() {
            handler.post(new Runnable() {
                public void run() {
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(WorkOutDetailFragment.mBroadcastCounterAction);
                    broadcastIntent.putExtra("count", counter);
                    //  if(counter % numberOfStepsToUpdateMap == 0) {
                    broadcastIntent.putExtra("workoutid", WorkoutTracking.getCurrentWorkoutid(Util.getContext()));
                    //  }
                    sendBroadcast(broadcastIntent);
                }
            });
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        //getApplicationContext().deleteDatabase("AlphaFitness");
        //WorkoutTracking.setCurrentWorkoutid(Util.getContext(),0);
        //Util.setRunning(false);
        db = MyDbHelper.getInstance(Util.getContext());
        // counter = Intent
        Log.d("Service", "Service ID" + WorkoutTracking.getCurrentWorkoutid(Util.getContext()));
        //counter = db.ExistsDayTracking(df.format(new Date()));
        u = new Util();

        indentService = new Intent(Util.getContext(), StepCounterService.class);
        oncreateflag = true;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        userData = u.getUserFromSHaredPreference(getApplicationContext());
        mBinder = new IStepCounterInterface.Stub() {

            @Override
            public int getCounter() throws RemoteException {
                return counter;
            }

            @Override
            public double getDistance() {
                return counter * 0.67;
            }

            public void setRecording(boolean value) {
                setRunning(value);
            }

            public boolean getRecording() {

                return isRunning(getApplicationContext());
            }
        };

        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Log.d("Sensor", "Step Sensor Not Found");
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        if (ActivityCompat.checkSelfPermission(Util.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
            return;
        }
        timer.schedule(backtask , 0, WorkOutDetailFragment.duration * 10000);

        Log.d("IOnCreate", " IN On Create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        counter = intent.getIntExtra("Count", 0);
        loadNotification = new LoadNotification("AlphaFitness", "App is Running....");
        loadNotification.notifyMessage();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        int workout_id = WorkoutTracking.getCurrentWorkoutid(Util.getContext());
        db.updateWorkoutEndTime(workout_id, userData.getName());
        Util.setRunning(false);
        stopForeground(true);
        sensorManager.unregisterListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(listener);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Log.d("Service","Service Destroyed");
    }

    public void setRunning(boolean running) {
        counter = 0;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(Util.getContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Constant.PREF_IS_RUNNING, running);
        editor.apply();
        editor.commit();
    }

    public static boolean isRunning(Context ctx) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
        return pref.getBoolean(Constant.PREF_IS_RUNNING, false);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Service","Service Bind");
        int workout_id = WorkoutTracking.getCurrentWorkoutid(Util.getContext());
        if(workout_id != 0) {
            counter = db.getStepsforCurrentWorkout(workout_id, userData.getName());
        } else {
            counter = 0;
        }
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("Service","Service Unbind");
        return super.onUnbind(intent);

    }



    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        try {
            // TODO Batch Step Counter Project for accurate rating.
            if(isRunning(getApplicationContext()) && !oncreateflag) {

                counter += 1;
              //  Log.d("Count", "Count From Servic - " + counter);
                UserTracking user = new UserTracking(userData.getName(), Integer.toString(counter), new Date());
                int workout_id = WorkoutTracking.getCurrentWorkoutid(Util.getContext());
                if(workout_id != 0) {
                    user.setSteps(Integer.toString(counter));
                    user.setWorkout_id(workout_id);
                    if(previousBestLocation != null) {
                        user.setLatitude(previousBestLocation.getLatitude());
                        user.setLongitude(previousBestLocation.getLongitude());
                    }
                    db.updateWorkoutSteps(workout_id, counter, userData.getName());
                    db.insertNewDay(user);
                }
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(RecordWorkFragment.mBroadcastStringAction);
                broadcastIntent.putExtra("count", counter);
              //  if(counter % numberOfStepsToUpdateMap == 0) {
                    broadcastIntent.putExtra("latitude", user.getLatitude());
                    broadcastIntent.putExtra("longitude", user.getLongitude());
              //  }
                sendBroadcast(broadcastIntent);
            } else {
                oncreateflag = false;
            }
        }catch(Exception e) {
            Log.e("error", e.getMessage());
        }
     }

    protected Integer NOTIFICATION_ID = 23213123; // Some random integer

    private LoadNotification loadNotification;

    class LoadNotification {

        private String titleMessage;
        private String textMessage;


        public LoadNotification(String titleMessage, String textMessage) {
            this.titleMessage = titleMessage;
            this.textMessage = textMessage;
        }

        public void notifyMessage() {
            NotificationCompat.Builder builder = getNotificationBuilder(StepCounterService.class);
            startForeground(NOTIFICATION_ID, builder.build());
        }

        protected NotificationCompat.Builder getNotificationBuilder(Class clazz) {
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

            builder.setSmallIcon(R.drawable.ic_directions_run_white_48dp);  // icon id of the image

            builder.setContentTitle(this.titleMessage)
                    .setContentText(this.textMessage)
                    .setContentInfo("AlphaFitness");

            Intent foregroundIntent = new Intent(getApplicationContext(), clazz);

            foregroundIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, foregroundIntent, 0);

            builder.setContentIntent(contentIntent);
            return builder;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else return isNewer && !isSignificantlyLessAccurate && isFromSameProvider;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public class MyLocationListener implements LocationListener
    {

        public void onLocationChanged(final Location loc)
        {
      //      Log.i("Lopcation", "Location changed");
            if(isBetterLocation(loc, previousBestLocation)) {
                previousBestLocation = loc;
            /*final Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            String Text = "";
            try {
                List<Address> addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                Text = "My current location is: "+addresses.get(0).getAddressLine(0);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Text = "My current location is: " +"Latitude = " + loc.getLatitude() + ", Longitude = " + loc.getLongitude();
            }
            */
                //Toast.makeText( getApplicationContext(), "Location polled to server", Toast.LENGTH_SHORT).show();
            }
        }

        public void onProviderDisabled(String provider)
        {
          //  Toast.makeText( getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();
        }


        public void onProviderEnabled(String provider)
        {
          //  Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }


        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

    }
}

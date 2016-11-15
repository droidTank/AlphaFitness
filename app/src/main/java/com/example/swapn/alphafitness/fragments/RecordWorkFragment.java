package com.example.swapn.alphafitness.fragments;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.swapn.alphafitness.IStepCounterInterface;
import com.example.swapn.alphafitness.R;
import com.example.swapn.alphafitness.RecordWorkOutActivity;
import com.example.swapn.alphafitness.common.Constant;
import com.example.swapn.alphafitness.common.FirebaseDb;
import com.example.swapn.alphafitness.common.Util;
import com.example.swapn.alphafitness.database.MyDbHelper;
import com.example.swapn.alphafitness.database.Tables.UserTracking;
import com.example.swapn.alphafitness.database.Tables.WorkoutTracking;
import com.example.swapn.alphafitness.models.UserModel;
import com.example.swapn.alphafitness.services.StepCounterService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecordWorkFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecordWorkFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordWorkFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    private Button fab;
    IStepCounterInterface mService;
    UserModel userData;
    Intent indentService;


    public static final String mBroadcastStringAction = "com.truiton.broadcast.string";
    public static final String mBroadcastIntegerAction = "com.truiton.broadcast.integer";
    public static final String mBroadcastArrayListAction = "com.truiton.broadcast.arraylist";
    private static final int numberOfStepsToUpdateMap = 10;
    private IntentFilter mIntentFilter;
    private TextView distance;
    private TextView time;
    private MyDbHelper db;


    long starttime = 0L;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedtime = 0L;
    int t = 1;
    int secs = 0;
    int mins = 0;
    int milliseconds = 0;
    Handler handler = new Handler();
    double previousLatitude = -1;
    double previousLongitude = -1;
    Location locationService;
    private int numberOfSteps = 0;
    private int prevCounter = 0;
    private boolean isServiceRunning = false;

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = IStepCounterInterface.Stub.asInterface(iBinder);
            int counter  = 0;
            try {
                counter = mService.getCounter();
                setRunning(mService.getRecording());
                prevCounter = counter;
                distance.setText(Util.returnDistance(counter) + "");

                drawPreviousMapAsyncTaskRunner runner = new drawPreviousMapAsyncTaskRunner();
                runner.execute(WorkoutTracking.getCurrentWorkoutid(Util.getContext()));

             //   time.setText(Util.timeConversion(counter));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public RecordWorkFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecordWorkFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecordWorkFragment newInstance(String param1, String param2) {
        RecordWorkFragment fragment = new RecordWorkFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(mBroadcastStringAction)) {
                        int counter = intent.getIntExtra("count", 0);
                        double latitude = intent.getDoubleExtra("latitude", -1);
                        double longitude = intent.getDoubleExtra("longitude", -1);
                            locationService = new Location("provider");
                            locationService.setLatitude(latitude);
                            locationService.setLongitude(longitude);
                            handleNewLocationService(locationService);
                            distance.setText(Util.returnDistance(counter) + "");
            }
        }
    };


    private class drawPreviousMapAsyncTaskRunner extends AsyncTask<Integer, String, ArrayList<LatLng>> {

        private String resp;

        @Override
        protected ArrayList<LatLng> doInBackground(Integer... params) {

            ArrayList<UserTracking> steps = new ArrayList<UserTracking>();
            ArrayList<LatLng> polyline = new ArrayList<LatLng>();
            steps = db.getAllStep(params[0], userData.getName());
            if(steps == null) {
                return null;
            }
            for(UserTracking step : steps) {
                if(step.getLatitude() != 0 && step.getLatitude() != null)
                    polyline.add(new LatLng(step.getLatitude(),step.getLongitude()));
            }
            return polyline;
        }

        @Override
        protected void onPreExecute() {

        }


        @Override
        protected void onPostExecute(ArrayList<LatLng> points) {
            if(points != null)
                drawPolylineOnMap(points);
        }

        @Override
        protected void onProgressUpdate(String... text) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record_work, container, false);
        //Util.setRunning(false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db =  new MyDbHelper(Util.getContext());
        fab = (Button) view.findViewById(R.id.fab);
        distance = (TextView) view.findViewById(R.id.text_distance);
        time = (TextView) view.findViewById(R.id.text_time);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(mBroadcastStringAction);
        mIntentFilter.addAction(mBroadcastIntegerAction);
        mIntentFilter.addAction(mBroadcastArrayListAction);
        getActivity().registerReceiver(mReceiver, mIntentFilter);
        userData = ((RecordWorkOutActivity) getActivity()).getUserData();
        indentService = new Intent(Util.getContext(), StepCounterService.class);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(checkLocationPermission()) {
                        if (checkServiceRunning()) {
                            //stopServices();
                            // Util.getContext().unbindService(mConnection);
                            Util.getContext().stopService(indentService);
                            stopServices();
                            setRunning(false);
                            stopTimer();
                            resetValues();
                            Toast.makeText(Util.getContext(), "All workout Data Saved.", Toast.LENGTH_SHORT);
                        } else {
                            Log.d("Record Workout", "Service Started");
                            startService(0);
                            Toast.makeText(Util.getContext(), "Service Started", Toast.LENGTH_SHORT);
                        }
                    } else {
                        Toast.makeText(getActivity(), "App needs location access to run ..", Toast.LENGTH_LONG);
                    }
                } catch (Exception e1) {
                    Log.d("Start click", "Something wrong");
                }
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        runOrBindService();
    }

    public void runOrBindService() {
        if(Util.isRunning(Util.getContext())) {
            if (check()) {
                bindService();
            } else {
                Log.d("Activity", "Service Started");
                startService(db.getStepsforCurrentWorkout(WorkoutTracking.getCurrentWorkoutid(Util.getContext()), userData.getName()));
            }
        }
        else {
            setRunning(false);
        }
    }


    public boolean checkServiceRunning () {
        if(Util.isRunning(Util.getContext())) {
            return true;
        } else {
            return false;
        }
    }

    public void startTimer (long timebuff) {
        timeSwapBuff += timebuff;
        starttime = SystemClock.uptimeMillis();
        handler.postDelayed(updateTimer, 0);
        t = 0;
    }

    public void stopTimer () {
        starttime = 0L;
        timeInMilliseconds = 0L;
        timeSwapBuff = 0L;
        updatedtime = 0L;
        secs = 0;
        mins = 0;
        milliseconds = 0;
        handler.removeCallbacks(updateTimer);
        time.setText("00:00:00");
    }

    public void resetValues() {
        time.setText("0:0:0");
        distance.setText(".0000");
    }

    public void setRunning (boolean flag) {
        if(flag) {
            fab.setText(R.string.stopworkout);
        } else {
            fab.setText(R.string.startworkout);
        }
    }

    public void startService(int i){
        Util.getContext().startService(indentService);
        Util.setRunning(true);
        setRunning(true);
        if(i == 0) {
            int id = db.insertNewWorkout(userData.getName());
            WorkoutTracking.setCurrentWorkoutid(Util.getContext(), id);
            startTimer(0);
        }
            bindService();
        //mService.setRecording(true);
    }

    public void bindService() {
        if (!Util.getContext().bindService(indentService, mConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND)) {
            Toast.makeText(Util.getContext(), "Failed to Bind the Service", Toast.LENGTH_SHORT);
        } else {
            try {
                startTimer(db.getTimeDifferenceforCurrentWorkout(WorkoutTracking.getCurrentWorkoutid(Util.getContext()), userData.getName()));
            } catch (ParseException e) {
                startTimer(0);
            }
            Log.d("service", "Service Binded");
        }
    }


    public boolean check() {
        ActivityManager manager = (ActivityManager) Util.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (com.example.swapn.alphafitness.services.StepCounterService.class.getName()
                    .equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void stopServices() {
        Log.d("Record Workout", "Service Stopped");
        Util.setRunning(false);
        int workout_id = WorkoutTracking.getCurrentWorkoutid(Util.getContext());
        db.updateWorkoutEndTime(workout_id, userData.getName());
        WorkoutTracking.setCurrentWorkoutid(Util.getContext(), 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getActivity().unregisterReceiver(mReceiver);
            if (mService != null)
                Util.getContext().unbindService(mConnection);
            handler.removeCallbacks(updateTimer);
            mConnection = null;
        } catch (Exception e) {

        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        UserModel getUserData();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } else {
                handleNewLocation(location);
            }
        }
    }


    private void drawPolylineOnMap(ArrayList<LatLng> points) {
        try {
            if (points != null) {
                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .addAll(points)
                        .width(5)
                        .color(Color.RED));
            }
        }
        catch(Exception e) {

        }
    }


    private void handleNewLocationService(Location location) {
            double currentLatitude = locationService.getLatitude();
            double currentLongitude = locationService.getLongitude();
       // double currentLatitude = p
       // double currentLongitude = location.getLongitude();
        if (previousLatitude == -1 || previousLongitude == -1) {
            previousLatitude = currentLatitude;
            previousLongitude = currentLongitude;
        } else {
            LatLng latLng = new LatLng(currentLatitude, currentLongitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
       //     mMap.animateCamera(CameraUpdateFactory.zoomTo(19));
            if ((previousLatitude != -1 || previousLongitude != -1) && (previousLatitude != currentLatitude || previousLongitude != currentLongitude)) {
                ArrayList<LatLng> points = new ArrayList<LatLng>();
                points.add(new LatLng(previousLatitude, previousLongitude));
                points.add(new LatLng(currentLatitude, currentLongitude));
                drawPolylineOnMap(points);
            /*    Polyline line = mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(previousLatitude, previousLongitude), new LatLng(currentLatitude, currentLongitude))
                        .width(5)
                        .color(Color.RED)); */
            }

            previousLatitude = currentLatitude;
            previousLongitude = currentLongitude;
        }
    }

    private void handleNewLocation(Location location) {
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        if (previousLatitude == -1 || previousLongitude == -1) {
            previousLatitude = currentLatitude;
            previousLongitude = currentLongitude;
        }

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(19));
        if ((previousLatitude != -1 || previousLongitude != -1) && (previousLatitude != currentLatitude || previousLongitude != currentLongitude)) {
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(previousLatitude, previousLongitude), new LatLng(currentLatitude, currentLongitude))
                    .width(5)
                    .color(Color.RED));
        }

        previousLatitude = currentLatitude;
        previousLongitude = currentLongitude;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
      //  if (location != null && mService != null && getLocationUpdateStatus())
            handleNewLocation(location);


        //stop location updates
       if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    public boolean getLocationUpdateStatus() {
        try {
            int counter = mService.getCounter();
            if((counter - prevCounter) > numberOfStepsToUpdateMap) {
                prevCounter = counter;
                return true;
            } else {
                return false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }


    public Runnable updateTimer = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - starttime;
            updatedtime = timeSwapBuff + timeInMilliseconds;
            secs = (int) (updatedtime / 1000);
            mins = secs / 60;
            secs = secs % 60;
            milliseconds = (int) (updatedtime % 1000);
            time.setText("" + mins + ":" + String.format("%02d", secs) + ":"
                    + String.format("%03d", milliseconds));
            handler.postDelayed(this, 0);
        }};

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                    mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }


            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }
}

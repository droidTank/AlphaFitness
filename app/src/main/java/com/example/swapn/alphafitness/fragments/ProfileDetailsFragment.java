package com.example.swapn.alphafitness.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.swapn.alphafitness.R;
import com.example.swapn.alphafitness.RecordWorkOutActivity;
import com.example.swapn.alphafitness.common.CircleTransform;
import com.example.swapn.alphafitness.common.Util;
import com.example.swapn.alphafitness.database.MyDbHelper;
import com.example.swapn.alphafitness.database.Tables.WorkoutTracking;
import com.example.swapn.alphafitness.models.UserModel;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileDetailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileDetailsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String mBroadcastStringAction = "com.truiton.broadcast.string";

    private IntentFilter mIntentFilter;

    private UserModel user;
    private MyDbHelper db;
    private Util u;

    private ImageView prof_pic;
    private TextView username;
    private TextView gender;
    private TextView weight;
    private TextView height;
    private ImageButton update;

    private TextView distance_week;
    private TextView time_week;
    private TextView workouts_week;
    private TextView calories_week;

    private TextView distance_all;
    private TextView time_all;
    private TextView workouts_all;
    private TextView calories_all;

    private int step_week = 0;
    private int step_all = 0;




    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ProfileDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileDetailsFragment newInstance(String param1, String param2) {
        ProfileDetailsFragment fragment = new ProfileDetailsFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_details, container, false);
        db = MyDbHelper.getInstance(getActivity().getApplicationContext());
        u = new Util();
        prof_pic = (ImageView) view.findViewById(R.id.prof_picture);
        username = (TextView) view.findViewById(R.id.prof_username);
        gender = (TextView) view.findViewById(R.id.prof_gender);
        weight = (TextView) view.findViewById(R.id.prof_weight);
        height = (TextView) view.findViewById(R.id.prof_height);
        long a = new Date().getTime();

       //int daily_count = db.ExistsDayTracking(u.editTime(u.getStringDate(new Date()), "00:00:00"));

        distance_week = (TextView) view.findViewById(R.id.distanceweek);
        time_week = (TextView) view.findViewById(R.id.timeweek);
        workouts_week = (TextView) view.findViewById(R.id.workoutweek);
        calories_week = (TextView) view.findViewById(R.id.caloriesweek);

        distance_all = (TextView) view.findViewById(R.id.distanceall);
        time_all = (TextView) view.findViewById(R.id.timeall);
        workouts_all = (TextView) view.findViewById(R.id.workoutall);
        calories_all = (TextView) view.findViewById(R.id.caloriesall);

        update = (ImageButton) view.findViewById(R.id.update_button);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RecordWorkOutActivity) getActivity()).openEditProfileFragment();
            }
        });

        user = ((RecordWorkOutActivity) getActivity()).getUserData();


        WeeklyWorkoutsAsyncTaskRunner runner = new WeeklyWorkoutsAsyncTaskRunner();
        runner.execute();

        AllWorkoutsAsyncTaskRunner allworkoutrunner = new AllWorkoutsAsyncTaskRunner();
        allworkoutrunner.execute();

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(mBroadcastStringAction);
        getActivity().registerReceiver(mReceiver, mIntentFilter);

     /*   Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open navigation drawer when click navigation back button
                Log.d("ProfileDetails", "BackClicked");
                ((RecordWorkOutActivity) getActivity()).homeClick();
            }
        }); */

        initialize(user);

        return view;
    }


    private class AllWorkoutsAsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()
            ArrayList<WorkoutTracking> array = null;
            try {

                array = db.getAllWorkout(user.getName());
                int stepCount = Util.getStepCountFromWorkout(array);
                step_all = stepCount;
                String workout_count = Integer.toString(array.size());
                String time = "";
                try {
                    time = Util.millisecToHours(calculateTotalTimeWorkouts(array));
                } catch (ParseException e) {
                    Log.e("ParseTotalTime", "Error parsing total time of workouts");
                    time = "00 hrs 00 min 00 sec";
                }
                //String time = Util.timeConversion(stepCount);
                String distance = Util.returnDistance(stepCount);
                String calories = Util.getCaloriesBurnt(stepCount);
                return distance + "#" + time + "#" + workout_count + "#" + calories;
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

        @Override
        protected void onPreExecute() {
            workouts_all.setText("calculating...");
            distance_all.setText("Calculating...");
            time_all.setText("Calculating...");
            calories_all.setText("Calculating...");
        }


        @Override
        protected void onPostExecute(String workouts) {
            // execution of result of Long time consuming operation
            if(!workouts.equals("")) {
                String[] workout_data = workouts.split("#");
                distance_all.setText(workout_data[0] + " miles");

                time_all.setText(workout_data[1]);
                workouts_all.setText(workout_data[2] + " times");
                calories_all.setText(workout_data[3] + " Cal");
            } else {
                distance_all.setText("0 miles");
                time_all.setText("00 hrs 00 min 00 sec");
                workouts_all.setText("0 times");
                calories_all.setText("0 Cal");
            }
        }

        @Override
        protected void onProgressUpdate(String... text) {
        }
    }


    private class WeeklyWorkoutsAsyncTaskRunner extends AsyncTask<String, String, ArrayList<WorkoutTracking>> {

        private String resp;

        @Override
        protected ArrayList<WorkoutTracking> doInBackground(String... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()
            ArrayList<WorkoutTracking> array = null;
            try {

                array = db.getWeekWorkouts(user.getName());
            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }
            return array;
        }

        @Override
        protected void onPreExecute() {
            workouts_week.setText("calculating...");
            distance_week.setText("Calculating...");
            time_week.setText("Calculating...");
        //    calories_week.setText("Calculating...");
        }


        @Override
        protected void onPostExecute(ArrayList<WorkoutTracking> workouts) {
            // execution of result of Long time consuming operation
            if(workouts != null) {
                int stepCount = Util.getStepCountFromWorkout(workouts);
                step_week = stepCount;
                distance_week.setText(Util.returnDistance(stepCount) + " miles");
                try {
                    time_week.setText(Util.millisecToHours(calculateTotalTimeWorkouts(workouts)));
                } catch (ParseException e) {
                    Log.e("ParseTotalTime", "Error parsing total time of workouts");
                    time_week.setText("00 hrs 00 min 00 sec");
                }
                calories_week.setText(Util.getCaloriesBurnt(stepCount) + " Cal");
                //time_week.setText(Util.timeConversion(stepCount));
                workouts_week.setText(Integer.toString(workouts.size()) + " times");
            } else {
                distance_week.setText("0 miles");
                time_week.setText("00 hrs 00 min 00 sec");
                workouts_week.setText("0 times");
                calories_week.setText("0 Cal");
            }
        }

        @Override
        protected void onProgressUpdate(String... text) {
        }
    }



    public long calculateTotalTimeWorkouts(ArrayList<WorkoutTracking> workouts) throws ParseException {
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long total = 0L;
        for(WorkoutTracking workout : workouts) {
            if(workout.getWorkout_end() != null && workout.getWorkout_end() != "") {
                Date startDate = outputFormat.parse(workout.getWorkout_start());
                Date endDate = outputFormat.parse(workout.getWorkout_end());
                total += endDate.getTime() - startDate.getTime();
            }
        }

        return total;
    }

    private void initialize(UserModel user) {
        Picasso.with(getActivity()).load(Uri.parse(user.getProf_pic())).transform(new CircleTransform()).into(prof_pic);
      //  Picasso.with(getActivity()).load(Uri.parse(user.getProf_pic())).fit().centerCrop().into(prof_pic);
        username.setText(user.getName());
        gender.setText(user.getGender());
        weight.setText(user.getWeight() + " lbs");
        height.setText(user.getHeight() + " ft");
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
        //void homeClick();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mReceiver != null)
                getActivity().unregisterReceiver(mReceiver);
        } catch (Exception e) {

        }

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RecordWorkFragment.mBroadcastStringAction)) {
                int counter = intent.getIntExtra("count", 0);
                step_all += 1;
                step_week += 1;
                distance_week.setText(Util.returnDistance(step_week) + " miles");
                distance_all.setText(Util.returnDistance(step_all) + " miles");
                calories_week.setText(Util.getCaloriesBurnt(step_week) + " Cal");
                calories_all.setText(Util.getCaloriesBurnt(step_all) + " Cal");

            }
        }
    };
}

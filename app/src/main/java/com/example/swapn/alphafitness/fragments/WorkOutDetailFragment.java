package com.example.swapn.alphafitness.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.swapn.alphafitness.R;
import com.example.swapn.alphafitness.RecordWorkOutActivity;
import com.example.swapn.alphafitness.common.Util;
import com.example.swapn.alphafitness.models.UserModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WorkOutDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WorkOutDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WorkOutDetailFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String mBroadcastCounterAction = "com.example.broadcast.counter";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    LineChart lineChart;
    private int previousCounter = 0;
    private int previousWorkoutId;
    private int currentTime;
    IntentFilter mIntentFilter = new IntentFilter();
    public static int duration = 1; // sec divide 10
    TextView max_speed;
    TextView min_speed;
    TextView avg_speed;
    private double maxSpeed = 0;
    private double minSpeed = 0;
    private double avgSpeed = 0;
    UserModel userData;

    private OnFragmentInteractionListener mListener;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(mBroadcastCounterAction)) {
                int counter = intent.getIntExtra("count", 0);
                int workout_id = intent.getIntExtra("workoutid", 0);
                if(workout_id != 0 ) {
                    handleNewCOunter(counter, workout_id);
                }
            }
        }
    };

    public WorkOutDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WorkOutDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WorkOutDetailFragment newInstance(String param1, String param2) {
        WorkOutDetailFragment fragment = new WorkOutDetailFragment();
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
        View view = inflater.inflate(R.layout.fragment_work_out_detail, container, false);
        return view;
    }

    ArrayList<String> labels = new ArrayList<String>();
    public void handleNewCOunter(int count, int workout_id) {
        try {
            //To task in this. Can do network operation Also
            Log.d("check","Check Run" );
            LineData data1 = lineChart.getData();
            if(previousCounter == 0 && count != 0)
                previousCounter = count;
            int diff = count - previousCounter;
            //ArrayList<String> labels = new ArrayList<String>();
            //labels.add("5");
            //LineData data = new LineData(labels, data1);
            labels.add(Integer.toString(currentTime + duration));
            if (data1 != null) {
                LineDataSet set1 = data1.getDataSetByIndex(0);
                // set.addEntry(...);
                data1.addEntry(
                        new Entry( diff, set1.getEntryCount()), 0);
            }

            double speed = Util.getSpeed(count - previousCounter, duration);
            if(speed > maxSpeed) {
                maxSpeed = speed;
                max_speed.setText(Double.toString(maxSpeed));
            }

            if(speed <= minSpeed || minSpeed == 0) {
                minSpeed = speed;
                min_speed.setText(Double.toString(minSpeed));
            }
            if(avgSpeed == 0) {
                avgSpeed = speed;
            }
            double totalspeed = avgSpeed * currentTime;
            avgSpeed = totalspeed / (currentTime + duration);
            avg_speed.setText(Util.returnTruncDouble(avgSpeed));

            //linechart
            lineChart.notifyDataSetChanged();
            previousCounter = count;
            lineChart.invalidate();
            currentTime += duration;
        } catch (Exception e) {
            // TODO Auto-generated catch block
        }
    }

    public void createChart() {
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0f, 0));

        LineDataSet dataset = new LineDataSet(entries, "Steps per " + Integer.toString(duration) +" minutes");
        labels.add("0");

        LineData data = new LineData(labels, dataset);
        //     data.addEntry("June", );
        dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
        dataset.setDrawCubic(false);
        dataset.setDrawFilled(true);
        if(lineChart != null) {
            //   lineChart.notifyDataSetChanged();
            //   lineChart.invalidate();
            lineChart.setData(data);
            lineChart.animateY(5000);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userData = ((RecordWorkOutActivity) getActivity()).getUserData();
        max_speed = (TextView) view.findViewById(R.id.max_speed);
        min_speed = (TextView) view.findViewById(R.id.min_speed);
        avg_speed = (TextView) view.findViewById(R.id.avg_speed);
        currentTime = 0;
        lineChart = (LineChart) view.findViewById(R.id.chart);
        createChart();
        mIntentFilter.addAction(mBroadcastCounterAction);
        getActivity().registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            //timer.cancel();
            getActivity().unregisterReceiver(mReceiver);
        } catch (Exception e) {

        }
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
    }
}

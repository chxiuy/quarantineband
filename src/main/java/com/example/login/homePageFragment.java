package com.example.login;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import java.util.*;
import java.lang.*;

import static java.lang.Double.parseDouble;

public class homePageFragment extends Fragment {
    private Timer mTimer;
    private TimerTask mTimerTask;
    private Handler mHandler;
    GlobalVariable gv;

    Map<String,Object> gps  = new HashMap<String,Object>();

    FirebaseDatabase fDB = FirebaseDatabase.getInstance();

    TextView mConnectedDevice, mConnectStatus, mLatitudeMsg, mLongitudeMsg, mIntGps, mSpotStatus;
    String getConnectedDevice, getConnectStatus, getLat, getLong, date, time;
    Button mIntGpsBtn;
    Double dLat, dLong , getIntLat, getIntLong;

    Boolean click = false;
    Boolean getClickStatus = false;
    Boolean inRange;

    int i=0;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public homePageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment secondFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static homePageFragment newInstance(String param1, String param2) {
        homePageFragment fragment = new homePageFragment();
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

        mHandler = new Handler();
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        getDetails();
                    }
                });
            }
        };
        mTimer.schedule(mTimerTask, 15000, 15000); //first delay time, loop delay time
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        gv = (GlobalVariable) getActivity().getApplication(); // avoid NPE
        mConnectedDevice = (TextView) getView().findViewById(R.id.connectedDevice);
        mConnectStatus = (TextView) getView().findViewById(R.id.connectStatus);
        mLatitudeMsg = (TextView) getView().findViewById(R.id.latMsg);
        mLongitudeMsg = (TextView) getView().findViewById(R.id.longMsg);
        mIntGpsBtn = (Button) getView().findViewById(R.id.initialLoc);
        mIntGps = (TextView) getView().findViewById(R.id.intGps);
        mSpotStatus = (TextView) getView().findViewById(R.id.spotStatus);

        gv.setClickStatus(false);

        mIntGpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstGpsData();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getDetails();
    }

    public void firstGpsData() {
        if (getClickStatus==false) {  //check save or not
            if(getLat != null && getLong != null) { //avoid NPE
                if (dLat != null && dLong != null) {
                    gv.setIntLat(dLat);
                    gv.setIntLong(dLong);
                    showToast("Your initial GPS is saved");
                    getDetails();
                    gv.setClickStatus(true);
                    mIntGpsBtn.setEnabled(false);
                }
                else {
                    showToast("Not GPS data can save");
                }
            }
            else {
                showToast("Not GPS data can save");
            }
        }
    }

    public void getDetails() {
        Log.i("GD","getDetail");
        if (i<=3) {
            i++;
        }
        else {
            saveToDB();
            i=0;
        }
        getConnectedDevice = gv.getString();
        getConnectStatus = gv.getStatus();
        getLat = gv.getLat();
        getLong = gv.getLong();
        getIntLat = gv.getIntLat();
        getIntLong = gv.getIntLong();
        getClickStatus = gv.getClickStatus();

        if(getLat != null && getLong != null) { //avoid NPE
            String tLat = getLat;
            String tLong = getLong;
            if(tLat.contains("A") || tLong.contains("A") || tLat.contains("O") || tLong.contains("O")) {  //avoid double type crash
                return;
            }
            else {
                dLat = ParseDouble(tLat);
                dLong = ParseDouble(tLong);

                if(dLat > 0 && dLong > 0) {  //show non error fix
                    mLatitudeMsg.setText(String.valueOf(dLat));
                    mLongitudeMsg.setText(String.valueOf(dLong));
                }
                else if (dLat > 0 && dLong <= 0) {
                    mLatitudeMsg.setText(String.valueOf(dLat));
                }
                else if (dLat <= 0 && dLong > 0) {
                    mLongitudeMsg.setText(String.valueOf(dLong));
                }
                else {
                    return;
                }
            }
        }

        if (getIntLat != null && getIntLong != null && click == false) {     // avoidNPE , click = false -> true = only active when intBtn unclicked
            mIntGps.setText(getIntLat + " , " + getIntLong);
            mIntGpsBtn.setText("Initialized");
            mIntGpsBtn.setBackgroundResource(R.drawable.button4); //GREY
            click = true;
            Log.i("click",click.toString());
        }


        if (getIntLat != null && getIntLong != null && click == true) {  //only active when intBtn clicked
            if (dLong == -1 || dLat == -1 || dLong == 0 || dLat == 0) {  //ensure not save error fix
                return;
            }
            else if (dLong<=getIntLong - 0.1 || dLong>=getIntLong + 0.1 || dLat<=getIntLat - 0.1 || dLat>=getIntLat + 0.1) {
                mSpotStatus.setText("Out of Range");
                mSpotStatus.setTextColor(Color.RED);
                inRange = false;
            }
            else {
                mSpotStatus.setText("In Range");
                mSpotStatus.setTextColor(Color.GREEN);
                inRange = true;
            }
        }

        if(getConnectStatus == "Connected") {
            mConnectedDevice.setText(getConnectedDevice);
        }
        else {
            mConnectedDevice.setText("please connect qband");
        }
        mConnectStatus.setText(getConnectStatus);

    }

    public void saveToDB() {
        if (gv.getBtDevice() != null) {  //avoid NPE
            if(getIntLat != null && getIntLong != null && click == true) { // Ensure inRange will exist in DB
                if(dLong == -1 || dLat == -1 || dLong == 0 || dLat == 0) {
                    return;
                }
                else {
                    DatabaseReference ref = fDB.getReference();
                    Date currentDate = Calendar.getInstance().getTime();
                    DateFormat df = new SimpleDateFormat("yy-MM-dd");
                    DateFormat tf = new SimpleDateFormat("HH:mm:ss");
                    date = df.format(currentDate);
                    time = tf.format(currentDate);
                    String dt = date + " " + time;
                    gps.put("latitude",dLat);
                    gps.put("longitude",dLong);
                    gps.put("Range",inRange);
                    ref.child(gv.getBtDevice().getName()).child(dt).setValue(gps);
                }
            }
        }
    }


    double ParseDouble(String str) {
        if (str != null && str.length() > 0) {
            try {
                return parseDouble(str);
            } catch(Exception e) {
                return -1; //cannot convert to double
            }
        }
        return 0; //null , length = 0
    }

    public void showToast (String msg){
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        Log.i("Toast" , "shown");
    }
}

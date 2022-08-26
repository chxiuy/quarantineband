package com.example.login;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link bleScannerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class bleScannerFragment extends Fragment {

    Button mDiscoverBtn, mStopBtn;
    ArrayList mNameList;
    ArrayList mAddressList;
    ListView mDeviceList;
    ArrayAdapter btArrayAdapter;
    ScanRecord mScanRecord;
    BluetoothDevice deviceConnect;
    private BluetoothAdapter mBlueAdapter = null;
    private ArrayList<BluetoothDevice> devices=new ArrayList<BluetoothDevice>(); //second method

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public bleScannerFragment() {
        // Required empty public constructor
    }

    public static bleScannerFragment newInstance(String param1, String param2) {
        bleScannerFragment fragment = new bleScannerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //fragment presetting
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences settings = getContext().getSharedPreferences("BLE", MODE_PRIVATE);
        for (int i = 0;; ++i) {
            final String str = settings.getString(String.valueOf(i), "");
            if (!str.equals("")) {
                btArrayAdapter.add(str);
            } else {
                break; // Empty String means the default value was returned.
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences settings = getContext().getSharedPreferences("BLE", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();

        for(int i = 0; i < btArrayAdapter.getCount(); i++) {
            editor.putString(String.valueOf(i), (String) btArrayAdapter.getItem(i));
        }

        editor.commit();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_blescanner,container, false);

        return inflater.inflate(R.layout.fragment_blescanner, container, false);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDeviceList = (ListView) getView().findViewById(R.id.devicesList);
        mStopBtn = (Button) getView().findViewById(R.id.stopBtn);
        mDiscoverBtn = (Button) getView().findViewById(R.id.scanningBtn);
        mNameList = new ArrayList();
        mAddressList = new ArrayList();
        btArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);
        mDeviceList.setAdapter(btArrayAdapter);


        final BluetoothManager btManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBlueAdapter = btManager.getAdapter();

        mDiscoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btArrayAdapter.clear();
                devices.clear();
                mNameList.clear();
                mAddressList.clear();
                scanBleDevice();
                mScanRecord = null;
            }
        });

        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScanBleDevice();
            }
        });
    }

    private boolean hasPermissions() {
        if (mBlueAdapter == null || !mBlueAdapter.isEnabled()) {
            requestBluetoothEnable();
            return false;
        }
        else if (!hasLocationPermissions()) {
            requestLocationPermission();
            return false;
        }
        return true;
    }

    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private boolean hasLocationPermissions() {
        return ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }

    private void scanBleDevice() {
        if (!hasPermissions()) {
            return;
        }
        mBlueAdapter.getBluetoothLeScanner().startScan(mLeScanCallback);

    }

    private void stopScanBleDevice() {
        mBlueAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
        Log.i("Scanning Process", "stop");
        showToast("Stopped Scanning");
    }

    public final ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {

            //Second Version
            final BluetoothDevice device = result.getDevice();
            int rssi = result.getRssi();
            String address = device.getAddress();
            final String name = device.getName();

            if (!devices.contains(device)) {
                devices.add(device);
                btArrayAdapter.add(name + "\n" +address + "\n" + rssi + "dBm");
                btArrayAdapter.notifyDataSetChanged();
            }
            mDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    stopScanBleDevice();
                    deviceConnect = devices.get(position);
                    Log.i("Clicked Fragement:", String.valueOf(devices.get(position)) + deviceConnect);
                    GlobalVariable gv = (GlobalVariable) getActivity().getApplication();
                    gv.setBtDevice(deviceConnect);
                }
            });
        }
        /**
         private List<UUID> getServiceUUIDsList(ScanResult scanResult) {
         List<ParcelUuid> parcelUuids = scanResult.getScanRecord().getServiceUuids();

         List<UUID> serviceList = new ArrayList<>();

         for (int i = 0; i < parcelUuids.size(); i++)
         {
         UUID serviceUUID = parcelUuids.get(i).getUuid();

         if (!serviceList.contains(serviceUUID))
         serviceList.add(serviceUUID);
         Log.i("SERVICE UUID", String.valueOf(serviceList.get(i)));
         }

         return serviceList;
         }
         /**
         @Override
         public void onBatchScanResults(List<ScanResult> results) {
         System.out.println("BLE// onBatchScanResults");
         for (ScanResult sr : results) {
         Log.i("ScanResult - Results", sr.toString());
         }
         }

         @Override
         public void onScanFailed(int errorCode) {
         System.out.println("BLE// onScanFailed");
         Log.e("Scan Failed", "Error Code: " + errorCode);
         }
         **/
    };


    private void showToast (String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        Log.i("Toast" , "shown");
    }
}
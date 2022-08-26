package com.example.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.lang.*;

public class MainActivity extends AppCompatActivity {

    GlobalVariable gv;
    BluetoothDevice deviceConnect;
    String readingResults, storeStatus;
    int l;
    boolean mReadBoll = false;
    boolean mCheckBtProfile = false;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private Handler mHandler2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavController navController = Navigation.findNavController(this, R.id.fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        GlobalVariable gv = (GlobalVariable) getApplication();
        gv.setStatus("Disconnected");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler2 = new Handler();
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler2.post(new Runnable() {
                    @Override
                    public void run() {
                        checkBtDevice();
                        Log.i("MSG","checkbt" );
                    }
                });
            }
        };
        mTimer.schedule(mTimerTask, 10000, 10000);

    }

    private Handler mHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                gv = (GlobalVariable) getApplication();
                if (l==41) {                 // start at "A"
                    gv.setLat(readingResults);
                }
                else if (l==42) {            // start at "O"
                    gv.setLong(readingResults);
                }

                Log.i("MSG",readingResults );
            }
            else if (msg.what==2) {
                showToast("Connected to: " + deviceConnect.getName());
                GlobalVariable gv = (GlobalVariable) getApplication();
                gv.setString(deviceConnect.getName());
            }
        }
    };


    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            SystemClock.sleep(2000);
            Message msg = new Message();
            msg.what = 1;
            mHandler.sendMessage(msg);
        }
    };

    private Runnable mRunnable2 = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = 2;
            mHandler.sendMessage(msg);
        }
    };

    private void checkBtDevice() {
        GlobalVariable gv = (GlobalVariable) getApplication();
        deviceConnect = gv.getBtDevice();
        Log.i("Device MA", String.valueOf(deviceConnect));
        if (deviceConnect != null && !mCheckBtProfile) {
            deviceConnect.connectGatt(this,false,mBluetoothGattCallback);
            Log.i("connectGatt MA","connecting");
            mCheckBtProfile = true;
        }
        if(storeStatus == "Connected") {
            mTimerTask.cancel();
        }
    }

    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            System.out.println("BLE// BluetoothGattCallback");
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    storeStatus = "Connected";
                    break;
                case BluetoothProfile.STATE_CONNECTING:
                    Log.i("gattCallback", "STATE_CONNECTING");
                    storeStatus = "CONNECTING";
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    storeStatus = "Disconnected";
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
                    storeStatus = "STATE_OTHER";
            }
            GlobalVariable gv = (GlobalVariable) getApplication();
            gv.setStatus(storeStatus);
        }

        @Override
        //New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                boolean connected = false;
                BluetoothGattService service = gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
                if(service!= null) {
                    Log.i("UUID","Service UUID entered.");
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                    if(characteristic != null) {
                        gatt.setCharacteristicNotification(characteristic,true);
                        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                        Log.i("UUID","Characteristic UUID entered.");
                        new Thread(mRunnable2).start();

                        gatt.setCharacteristicNotification(characteristic, true);

                    }
                }
            }
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("Char write success", String.valueOf(status));
            }
            else {
                Log.i("Char write unsuccess", String.valueOf(status));
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("Char read success", String.valueOf(status));
                readCharacteristic(characteristic);
            }
            else {
                Log.i("Char read unsuccess", String.valueOf(status));
                mReadBoll = false;
            }

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            readCharacteristic(characteristic);
        }

        private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
            mReadBoll = true;
            byte[] messageBytes = characteristic.getValue();
            readingResults = byteArrayInHexFormat(messageBytes);
            Log.i("Read: ", readingResults);
            new Thread(mRunnable).start();
            if (messageBytes == null) {
                Log.i("Read","Unable to convert bytes to string");
                return;
            }

        }

        public String byteArrayInHexFormat(byte[] byteArray) {
            if (byteArray == null) {
                return null;
            }
            StringBuilder stringBuilder = new StringBuilder();
            String checkString = byteToHex(byteArray[0]);
            if (checkString.contains("41")) {
                l = 41;
            }
            else if (checkString.contains("4f")) {
                l = 42;
            }
            else {
                l=0;
            }
            for (int i = 1; i < byteArray.length; i++) {
                String hexString = byteToHex(byteArray[i]);
                if (hexString.contains("2e")) {
                    stringBuilder.append(".");
                }
                else {
                    String numValue = hexToASCII(hexString);
                    stringBuilder.append(numValue);
                }
            }
            return stringBuilder.toString();
        }

        private String byteToHex(byte b) {

            char char1 = Character.forDigit((b & 0xF0) >> 4, 16);
            char char2 = Character.forDigit((b & 0x0F), 16);
            return String.format("%1$s%2$s", char1, char2);
        }

        private String hexToASCII(String hexValue)
        {
            StringBuilder output = new StringBuilder("");
            for (int i = 0; i < hexValue.length(); i += 2)
            {
                String str = hexValue.substring(i, i + 2);
                output.append((char) Integer.parseInt(str, 16));
            }
            return output.toString();
        }

    };

    public void showToast (String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Log.i("Toast" , "shown");
    }




}
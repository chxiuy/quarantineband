package com.example.login;
import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.util.Log;


public class GlobalVariable extends Application{
    private String mString, mLat, mStatus, mLong;
    private BluetoothDevice mBtDevice;
    private Double mIntLat, mIntLong;
    private Boolean mClickStatus;
    //Save variable
    public void setString(String string) {
        this.mString = string;
    }

    public void setStatus(String status) {
        this.mStatus = status;
    }
    public void setBtDevice(BluetoothDevice btDevice) {
        this.mBtDevice = btDevice;
        Log.i("GV", String.valueOf(btDevice));
    }

    public void setLat(String lat) {
        this.mLat = lat;
        Log.i("GV", "setting Lat");
    }

    public void setLong(String longitude) {
        this.mLong = longitude;
        Log.i("GV", "setting Long");
    }

    public void setIntLat (Double intLat) {
        this.mIntLat = intLat;
    }

    public void setIntLong (Double intLong) {
        this.mIntLong = intLong;
    }

    public void setClickStatus (Boolean clickStatus) {
        this.mClickStatus = clickStatus;
    }


    //Get variable
    public String getString() {
        return mString;
    }

    public String getStatus() {
        return mStatus;
    }

    public BluetoothDevice getBtDevice() {
        return mBtDevice;
    }

    public String getLat() {
        return mLat;
    }

    public String getLong() {
        return mLong;
    }

    public Double getIntLat() {
        return mIntLat;
    }

    public Double getIntLong() {
        return mIntLong;
    }

    public Boolean getClickStatus () {
        return mClickStatus;
    }

}

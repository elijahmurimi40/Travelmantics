package com.fortie40.travelmantics;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class CheckActivityState extends Application {

    // global Var
    public static boolean listActivityVisible;
    public static boolean dealActivityVisible;

    // check state of list activity
    public static boolean isListActivityVisible() {
        return listActivityVisible;
    }

    // return true when list activity resume
    public static  void listActivityResumed() {
        listActivityVisible = true;
    }

    // return false when list activity is paused
    public static void listActivityPaused() {
        listActivityVisible = false;
    }

    // check state of deal activity
    public static boolean isDealActivityVisible() {
        return dealActivityVisible;
    }

    // return true when deal activity resume
    public static  void dealActivityResumed() {
        dealActivityVisible = true;
    }

    // return false when deal activity is paused
    public static void dealActivityPaused() {
        dealActivityVisible = false;
    }

    // check for network connectivity
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

}

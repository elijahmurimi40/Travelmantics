package com.fortie40.travelmantics;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Observable;


public class InternetConnectorReceiver extends BroadcastReceiver {
    public InternetConnectorReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        try {

            boolean isListVisible = CheckActivityState.isListActivityVisible();
            boolean isDealVisible = CheckActivityState.isDealActivityVisible();

            if(isListVisible == true) {
                if(CheckActivityState.isNetworkAvailable(context)) {
                    ListActivity.mToolbar.setTitle(context.getString(R.string.title_activity_list));
                    ListActivity listActivity = ListActivity.getInstance();
                    listActivity.reload();
                } else {
                    ListActivity.mToolbar.setTitle(context.getString(R.string.connecting));
                    ListActivity listActivity = ListActivity.getInstance();
                    listActivity.getDialog();
                }
            } else if(isDealVisible == true) {
                if(CheckActivityState.isNetworkAvailable(context)) {
                    DealActivity.mToolbar.setTitle(context.getString(R.string.title_activity_deal));
                    ListActivity listActivity = ListActivity.getInstance();
                    listActivity.reload();
                } else {
                    DealActivity.mToolbar.setTitle(context.getString(R.string.connecting));
                    ListActivity listActivity = ListActivity.getInstance();
                    listActivity.getDialog();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

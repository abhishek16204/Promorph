package com.example.android.promorph;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class Restarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Broadcast Listened", "Service tried to stop");
        //Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show();
        String action = intent.getAction();
        if (action.equals("restart service")) {
             TempActivity.flag = intent.getExtras().getInt("flag");
            TempActivity.status = intent.getExtras().getString("status");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //Toast.makeText(context, "Values in restarter "+flag+"       "+status, Toast.LENGTH_SHORT).show();
                context.startForegroundService(new Intent(context, LocationService.class).putExtra("flag", TempActivity.flag).putExtra("status", TempActivity.status));
            } else {
                context.startService(new Intent(context, LocationService.class).putExtra("flag", TempActivity.flag).putExtra("status", TempActivity.status));
            }
        }
    }
}

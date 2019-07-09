package com.example.android.promorph;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class DashboardActivity extends AppCompatActivity {
    private FusedLocationProviderClient client;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    int serial = -1;
    ProfileDB pd;
    String localTime;
    TextClock textClock;
    String user_id, status;
    HttpResponse response;
    Button check_in_out;
    Intent intent;
    ImageButton progress;
    JSONTask jsonTask;
    AlertDialog.Builder builder1;
    ProgressDialog progressDialog;
    Boolean isconnected;
    String attendance_status, dateToStr, message = null;
    SyncWithServerAsync async;
    Date date;

    @Override
    protected void onRestart() {
        super.onRestart();
        if (isconnected) {
            new JSONTask().execute("http://pms.promorph.in/dashboard_api/?user_id=" + user_id);
        }
        else {
            Toast.makeText(DashboardActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!defaultPermissionCheck()) {
            askForPermissions();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>Dashboard</font>"));
        getSupportActionBar().setBackgroundDrawable(ContextCompat.getDrawable(DashboardActivity.this, R.drawable.upper_half_login));
        client = LocationServices.getFusedLocationProviderClient(this);
        ImageButton attendance = findViewById(R.id.attendance);
        pd = new ProfileDB(DashboardActivity.this);
        Cursor c = pd.getUserID();
        c.moveToNext();

        user_id = c.getString(1);
        textClock = findViewById(R.id.text_clock);
        attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intent = new Intent(DashboardActivity.this, AttendanceActivity.class);
                startActivity(intent);

            }
        });


        // New paste----------------------------------------------------------------------------------------------------
        isconnected = ConnectivityReceiver.isConnected();
        check_in_out = findViewById(R.id.check_in_out);
        progress = findViewById(R.id.progress);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
        Date currentLocalTime = cal.getTime();
        DateFormat date5 = new SimpleDateFormat("HH:mm:ss");
        date5.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));


        localTime = date5.format(currentLocalTime);



                   LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
                                                                     @Override

                                                                     public void onReceive(Context context, Intent intent) {

     String latitude = intent.getStringExtra(LocationService.EXTRA_LATITUDE);
     String longitude = intent.getStringExtra(LocationService.EXTRA_LONGITUDE);
     String distance = intent.getStringExtra(LocationService.EXTRA_DISTANCE);
     TempActivity.flag = intent.getIntExtra("flag", 0);


 }
}, new IntentFilter(LocationService.ACTION_LOCATION_BROADCAST)
        );


         intent = new Intent(DashboardActivity.this, LocationService.class);
         intent.putExtra("flag", TempActivity.flag);
        isconnected = ConnectivityReceiver.isConnected();

        jsonTask = new JSONTask();
        if (isconnected) {

            jsonTask.execute("http://pms.promorph.in/dashboard_api/?user_id=" + user_id);
        } else {
            Toast.makeText(DashboardActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }

        pd = new ProfileDB(DashboardActivity.this);

        Log.e("user id is", " " + user_id);
        Log.e("inside", "cursor  " + attendance_status);
        Log.e("Service started", "");
        date = new Date();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

        dateToStr = format.format(date);


        progress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, WorkProgress.class));
            }
        });

        check_in_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                  Intent intent = new Intent(DashboardActivity.this, LocationService.class);
                 intent.putExtra("flag", 0);
                EditText editText = findViewById(R.id.edittext);
                async = new SyncWithServerAsync();
                String text = editText.getText().toString();
                if (check_in_out.getText().equals("CHECK-IN")) {
                    //intent.putExtra("flag", 0);
                     intent.putExtra("status", "CHECK-OUT");
                    TempActivity.status="CHECK_OUT";
                    pd.status_check_in(user_id);
                    DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                    Date d = Calendar.getInstance().getTime();
                    check_in_out.setText("CHECK-OUT");
                    check_in_out.setBackgroundDrawable(ContextCompat.getDrawable(DashboardActivity.this, R.drawable.check_out_button));
                    textClock.setBackgroundDrawable(ContextCompat.getDrawable(DashboardActivity.this, R.drawable.login_b2));
                    Toast.makeText(DashboardActivity.this, "CHECKED IN SUCCESSful", Toast.LENGTH_SHORT).show();
                    pd.attendance_check_in(user_id, dateToStr, dateFormat.format(d), "PRESENT");
                    if (isconnected)
                        async.execute(user_id, "1", text);
                    else {
                        Toast.makeText(DashboardActivity.this, "Check your Internet Connection", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.e("Data entered ", "successfully check-in");
                } else {
                    String diff = "";
                    Log.e("inside", "else");
                    intent.putExtra("status", "CHECK-IN");
                    TempActivity.status="CHECK-IN";
                    //intent.putExtra("flag", 0);
                    pd.status_check_out(user_id);
                    DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                    Date d = Calendar.getInstance().getTime();
                    Cursor cursor = pd.getAll();
                    String g = dateFormat.format(d);
                    if (!(pd.first_check_out(dateToStr))) {
                        pd.check_out(dateToStr, g, "PRESENT");
                    } else {
                        while (cursor.moveToNext()) {

                            if (cursor.getString(1) != null && cursor.getString(1).equals(user_id) && cursor.getString(2) != null && cursor.getString(2).equals(dateToStr) && cursor.getString(4) == null) {
                                try {
                                    Log.e("cursor", "    " + cursor.getString(3));
                                    diff = time_difference(g, cursor.getString(3));
                                    serial = cursor.getInt(0);
                                    pd.attendance_check_out(serial, g, diff);

                                } catch (ParseException e) {
                                    diff = "hello";
                                    Log.e(" calculation exception ", " ");
                                }
                            } else {

                                Log.e("galat jagah ", " aa gye aap");
                            }

                        }
                        if (pd.only_check_out(dateToStr)) {
                            pd.check_out(dateToStr, g, "PRESENT");
                        }
                    }
                    if (isconnected)
                        async.execute(user_id, "2", text);
                    else {
                        Toast.makeText(DashboardActivity.this, "Check your Internet Connection", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.e("Data entered ", "successfully check-out");
                    Log.e("button value", "" + check_in_out.getText());
                    Toast.makeText(DashboardActivity.this, "CHECKED-OUT SUCCESSFULLY", Toast.LENGTH_LONG).show();
                    check_in_out.setText("CHECK-IN");
                    check_in_out.setBackgroundDrawable(ContextCompat.getDrawable(DashboardActivity.this, R.drawable.check_in_button));
                    textClock.setBackgroundDrawable(ContextCompat.getDrawable(DashboardActivity.this, R.drawable.login_b1));
                }
                editText.setText("");
                Log.e("on click", "service");
                startService(intent);
            }


        });
    }

    public String time_difference(String date1, String date2) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        Date d1 = format.parse(date1);
        Date d2 = format.parse(date2);

        long mills = d1.getTime() - d2.getTime();
        int hours = (int) mills / (1000 * 60 * 60);
        int mins = (int) (mills / (1000 * 60)) % 60;
        Log.e(" hours" + hours, "        mins" + mins);

        String diff = hours + ":" + mins;
        return diff;

    }

    class SyncWithServerAsync extends AsyncTask<String, String, String> {
        ProgressDialog dialog = new ProgressDialog(DashboardActivity.this);

        @Override
        protected void onPreExecute() {

            dialog.setMessage("Sending status........Please Wait");
            dialog.setCancelable(false);
            dialog.show();

            super.onPreExecute();


        }

        @Override
        protected String doInBackground(String... strings) {


            String user_id = strings[0];
            String status1 = strings[1];
            String remark = strings[2];


            Log.e("Final check", "    " + user_id + "   " + status1 + "   " + remark);

            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost("http://pms.promorph.in/attendance_api/");
            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(3);
            nameValuePair.add(new BasicNameValuePair("user_id", user_id));
            nameValuePair.add(new BasicNameValuePair("status", status1));
            nameValuePair.add(new BasicNameValuePair("remark", remark));
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            } catch (UnsupportedEncodingException e) {
                // writing error to Log
                e.printStackTrace();
            }
            try {
                response = httpClient.execute(httpPost);
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject obj = new JSONObject(responseBody);
                message = obj.getString("message");
                status = obj.getString("status");
                if (message.equals("Failed") && status.equals("Fail")) {

                    Log.e("failed ", " failed to send data to server");

                    //Toast.makeText(context,"failed to upload data to server.",Toast.LENGTH_LONG ).show();
                } else {
                    Log.e("PAssed ", " sent data to server");

                    //Toast.makeText(context,"Successfully uploaded data to server.",Toast.LENGTH_LONG ).show();
                }
                Log.d("Http Response:", response.toString());
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Toast.makeText(DashboardActivity.this, "IO", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } catch (JSONException e) {
                //Toast.makeText(getApplicationContext(), "JSON", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }


            return status1;
        }

        @Override
        protected void onPostExecute(String s) {
            dialog.cancel();
            if (status != null && status != "Fail") {

                if (s.equals("1")) {
                    builder1 = new AlertDialog.Builder(DashboardActivity.this);
                    builder1.setTitle("CHECK-IN SUCCESSFUL");
                    builder1.setCancelable(false);
                    builder1.setPositiveButton(
                            "ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
                if (s.equals("2")) {
                    builder1 = new AlertDialog.Builder(DashboardActivity.this);
                    builder1.setTitle("CHECK-OUT SUCCESSFUL");
                    builder1.setCancelable(false);
                    builder1.setPositiveButton(
                            "ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            } else
                Toast.makeText(DashboardActivity.this, "Failed to send data ", Toast.LENGTH_LONG).show();
            super.onPostExecute(s);
        }
    }

    private class JSONTask extends AsyncTask<String, String, String> {
        String status2;
        ProgressDialog progressDialog = new ProgressDialog(DashboardActivity.this);

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Fetching Status......");
            progressDialog.setCancelable(false);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            attendance_status = s;
            progressDialog.cancel();
            if (attendance_status.equals("1")) {
                check_in_out.setText("CHECK-OUT");
                check_in_out.setBackgroundDrawable(ContextCompat.getDrawable(DashboardActivity.this, R.drawable.check_out_button));
                textClock.setBackgroundDrawable(ContextCompat.getDrawable(DashboardActivity.this, R.drawable.login_b2));
                 intent.putExtra("status", "CHECK-OUT");
                 TempActivity.status="CHECK_OUT";
                 startService(intent);
                Log.e("inside if", "");

            } else {
                check_in_out.setText("CHECK-IN");
                check_in_out.setBackgroundDrawable(ContextCompat.getDrawable(DashboardActivity.this, R.drawable.check_in_button));
                textClock.setBackgroundDrawable(ContextCompat.getDrawable(DashboardActivity.this, R.drawable.login_b1));
                intent.putExtra("status", "CHECK-IN");
                TempActivity.status="CHECK_IN";
                startService(intent);
                Log.e("inside else", "");
            }

            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... params) {


            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = null;
            try {
                httpResponse = httpClient.execute(new HttpGet(params[0]));

                String responseBody = EntityUtils.toString(httpResponse.getEntity());
                Log.e("vvv", responseBody);
                JSONObject parentObject = new JSONObject(responseBody);
                status2 = parentObject.getString("status");
                String attends = parentObject.getString("attendance_status");
                return attends;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

    }


    // Pastedd--------------------------------------------------------------------------------------------


    private void askForPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    private boolean defaultPermissionCheck() {
        int loc = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        return loc == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission required ", Toast.LENGTH_LONG).show();
                // requestPermission();
            }
        }


    }
}

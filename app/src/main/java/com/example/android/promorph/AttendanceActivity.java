package com.example.android.promorph;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_CALENDAR;

public class AttendanceActivity extends AppCompatActivity {
    private FusedLocationProviderClient client;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    ProfileDB pd;
    Boolean isconnected;
    String user_id;
    TableLayout tableLayout;
    TableRow t1;
  NestedScrollView nested;
    TextView tv,no_record;
    String status=null;
    List<String>date=new ArrayList<>(),check_in=new ArrayList<>(),check_out=new ArrayList<>(),holiday=new ArrayList<>(),
    attendance_status=new ArrayList<>();
    @Override
    public void onBackPressed() {
        super.onBackPressed();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        pd=new ProfileDB(AttendanceActivity.this);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>Attendance</font>"));
        getSupportActionBar().setBackgroundDrawable(ContextCompat.getDrawable(AttendanceActivity.this,R.drawable.upper_half_login));
        client = LocationServices.getFusedLocationProviderClient(this);
      /*  final TextView textView = findViewById(R.id.text_location);
        Button stop = findViewById(R.id.stop);*/
      no_record=findViewById(R.id.no_record);
      tableLayout=findViewById(R.id.table_layout);
      nested=findViewById(R.id.nested);
      Cursor c=pd.getUserID();
        c.moveToNext();
        String user_id=c.getString(1);
        Log.e("work user-id",""+user_id);
        isconnected=ConnectivityReceiver.isConnected();
        if( isconnected==true)
        {
            new JSONTask().execute("http://pms.promorph.in/my_checkin_out_api/?user_id="+user_id);
        }


    }



    private class JSONTask extends AsyncTask<String,String,String>
    {
        ProgressDialog dialog=new ProgressDialog(AttendanceActivity.this);


        @Override
        protected void onPreExecute() {
            dialog.setMessage("Getting your personal  data........Please Wait");
            dialog.setCancelable(false);
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {


                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse = httpClient.execute(new HttpGet(strings[0]));
                String responseBody = EntityUtils.toString(httpResponse.getEntity());
                Log.e("work proggress response", responseBody);
                JSONObject parentObject = new JSONObject(responseBody);
                status = parentObject.getString("status");
                if (status.equals("Success")) {
                    if(date!=null)
                    {
                        date.clear();
                    }
                    if(check_in!=null) {
                        check_in.clear();
                        attendance_status.clear();
                    }
                    if(check_out!=null)
                        check_out.clear();
                    if(holiday!=null)
                        holiday.clear();
                    JSONArray parentArray = parentObject.getJSONArray("my_progress");
                    for (int i = 0; i < parentArray.length(); i++) {
                        JSONObject finalobject = parentArray.getJSONObject(i);
                        date.add(finalobject.getString("date"));
                        check_in.add(finalobject.getString("check_in"));
                        check_out.add(finalobject.getString("check_out"));
                        holiday.add(finalobject.getString("holiday"));
                        attendance_status.add(finalobject.getString("status"));
                    }

                }
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {

                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            return status;
        }
        @Override
        protected void onPostExecute(String s) {
            dialog.cancel();
            if(s.equals("Success"))
            {
                no_record.setVisibility(View.INVISIBLE);
                TableRow tableRow[] = new TableRow[date.size()];
                t1= new TableRow(AttendanceActivity.this);
                t1.setBackgroundDrawable(ContextCompat.getDrawable(AttendanceActivity.this,R.drawable.upper_half_login));
                TextView tv1 = new TextView(AttendanceActivity.this);
                tv1.setText("DATE");
                tv1.setTextColor(Color.parseColor("#ffffff"));
                tv1.setPadding(10, 0, 10, 10);
                tv1.setGravity(Gravity.CENTER_HORIZONTAL);
                tv1.setTypeface(null, Typeface.BOLD);
                tv1.setTextSize(15);
                t1.addView(tv1);
                TextView tv2 = new TextView(AttendanceActivity.this);
                tv2.setText("CHECK-IN");
                tv2.setTextColor(Color.parseColor("#ffffff"));
                tv2.setPadding(10, 0, 25, 10);
                tv2.setGravity(Gravity.CENTER_HORIZONTAL);
                tv2.setTypeface(null, Typeface.BOLD);
                tv2.setTextSize(15);
                t1.addView(tv2);
                TextView tv3 = new TextView(AttendanceActivity.this);
                tv3.setText("CHECK-OUT");
                tv3.setTextColor(Color.parseColor("#ffffff"));
                tv3.setPadding(10, 0, 10, 10);
                tv3.setGravity(Gravity.CENTER_HORIZONTAL);
                tv3.setTypeface(null, Typeface.BOLD);
                t1.addView(tv3);
                TextView tv4 = new TextView(AttendanceActivity.this);
                tv4.setText("HOLIDAY");
                tv4.setPadding(10, 0, 10, 10);
                tv4.setGravity(Gravity.CENTER_HORIZONTAL);
                tv4.setTextColor(Color.parseColor("#ffffff"));
                tv4.setTypeface(null, Typeface.BOLD);
                t1.addView(tv4);
                TextView tv5 = new TextView(AttendanceActivity.this);
                tv5.setText("STATUS");
                tv5.setTextSize(15);
                tv5.setPadding(10, 0, 10, 10);
                tv5.setGravity(Gravity.CENTER_HORIZONTAL);
                tv5.setTextColor(Color.parseColor("#ffffff"));
                tv5.setTypeface(null, Typeface.BOLD);
                t1.addView(tv5);
                tableLayout.addView(t1);

                for(int i=date.size()-1;i>=0;i--)
                {

                    tableRow[date.size()-1-i] = new TableRow(AttendanceActivity.this);
                    if (i % 2 != 0)
                        tableRow[date.size()-1-i].setBackgroundColor(Color.parseColor("#cccccc"));
                    for (int j = 0; j < 5; j++) {
                        tv = new TextView(AttendanceActivity.this);
                        tv.setPadding(10, 0, 10, 10);
                        tv.setGravity(Gravity.CENTER_HORIZONTAL);
                        tv.setTextSize(15);
                        if (j == 0)
                            tv.setText(date.get(i));
                        if (j == 1)
                            tv.setText(check_in.get(i));
                        if (j == 2) {
                            tv.setText(check_out.get(i));
                        }
                        if (j == 3) {
                            tv.setText(holiday.get(i));
                        }
                        if (j == 4) {
                            if(attendance_status.get(i).equals("1"))
                            {
                                tv.setText("PRESENT");
                            }
                            else if(attendance_status.get(i).equals("0"))
                                tv.setText("ABSENT");
                            else
                            {
                                tv.setText("null");
                            }
                        }
                        tableRow[date.size()-1-i].addView(tv);


                    }
                    tableLayout.addView(tableRow[date.size()-1-i]);

                }


            }
            else
            {
                nested.setVisibility(View.INVISIBLE);
            }
            super.onPostExecute(s);
        }
    }



}


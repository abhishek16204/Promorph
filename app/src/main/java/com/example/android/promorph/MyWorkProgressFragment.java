package com.example.android.promorph;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class MyWorkProgressFragment extends Fragment {
    TableLayout tableLayout;
    Boolean isconnected;
    String status=null;
    TableRow t1;
    TextView tv;
    TextView no_record;
    NestedScrollView nested;
    List<String> s_name=new ArrayList<>(),date=new ArrayList<>(),done=new ArrayList<>(),perfection=new ArrayList<>()
            ,percentage=new ArrayList<>(),s_remark=new ArrayList<>(),s_rating=new ArrayList<>(),remark=new ArrayList<>()
            ,total=new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.my_work_progress, container, false);
        tableLayout = view.findViewById(R.id.table_layout);
        isconnected=ConnectivityReceiver.isConnected();
        ProfileDB pd=new ProfileDB(getContext());
        no_record=view.findViewById(R.id.no_record);
        nested=view.findViewById(R.id.nested);
        Cursor c=pd.getUserID();
        c.moveToNext();
        String user_id=c.getString(1);
        Log.e("work user-id",""+user_id);
        if( isconnected==true)
        {
            new JSONTask().execute("http://pms.promorph.in/work_progress_detail/?user_id="+user_id);
        }

        return view;


    }

    private class JSONTask extends AsyncTask<String,String,String>
    {
        ProgressDialog dialog=new ProgressDialog(getContext());


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
                        total.clear();
                        done.clear();
                        percentage.clear();
                        perfection.clear();
                        remark.clear();
                    }
                    if(s_name!=null)
                        s_name.clear();
                    if(s_rating!=null)
                        s_rating.clear();
                    if(s_remark!=null)
                    s_remark.clear();
                    JSONArray parentArray = parentObject.getJSONArray("my_progress");
                    for (int i = 0; i < parentArray.length(); i++) {
                        JSONObject finalobject = parentArray.getJSONObject(i);
                         s_name.add(finalobject.getString("senior_name"));
                         date.add(finalobject.getString("date"));
                         done.add(finalobject.getString("no_of_activity_done"));
                         perfection.add(finalobject.getString("work_perfection_rating"));
                        total.add(finalobject.getString("no_of_activity"));
                         percentage.add((finalobject.getString("percentage_of_work"))+"%");
                        s_remark.add(finalobject.getString("senior_remark"));
                         s_rating.add(finalobject.getString("senior_quality_rating"));
                         remark.add(finalobject.getString("remark"));
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
                tableLayout.removeAllViews();
                no_record.setVisibility(View.INVISIBLE);
                TableRow tableRow[] = new TableRow[total.size()];
                t1= new TableRow(getContext());
                t1.setBackgroundDrawable(ContextCompat.getDrawable(getContext(),R.drawable.upper_half_login));
                TextView tv1 = new TextView(getContext());
                tv1.setText("DATE");
                tv1.setTextColor(Color.parseColor("#ffffff"));
                tv1.setPadding(10, 10, 10, 10);
                tv1.setGravity(Gravity.CENTER_HORIZONTAL);
                tv1.setTypeface(null, Typeface.BOLD);
                tv1.setTextSize(15);
                t1.addView(tv1);
                TextView tv2 = new TextView(getContext());
                tv2.setText("NO OF ACTIVITIES");
                tv2.setTextColor(Color.parseColor("#ffffff"));
                tv2.setPadding(10, 10, 25, 10);
                tv2.setGravity(Gravity.CENTER_HORIZONTAL);
                tv2.setTypeface(null, Typeface.BOLD);
                tv2.setTextSize(15);
                t1.addView(tv2);
                TextView tv3 = new TextView(getContext());
                tv3.setText("NO OF ACTIVITIES DONE");
                tv3.setTextColor(Color.parseColor("#ffffff"));
                tv3.setPadding(10, 10, 10, 10);
                tv3.setGravity(Gravity.CENTER_HORIZONTAL);
                tv3.setTypeface(null, Typeface.BOLD);
                t1.addView(tv3);
                TextView tv4 = new TextView(getContext());
                tv4.setText("WORK PERCENTAGE");
                tv4.setPadding(10, 10, 10, 10);
                tv4.setGravity(Gravity.CENTER_HORIZONTAL);
                tv4.setTextColor(Color.parseColor("#ffffff"));
                tv4.setTypeface(null, Typeface.BOLD);
                t1.addView(tv4);
                TextView tv5 = new TextView(getContext());
                tv5.setText("RATING");
                tv5.setTextSize(15);
                tv5.setPadding(10, 10, 10, 10);
                tv5.setGravity(Gravity.CENTER_HORIZONTAL);
                tv5.setTextColor(Color.parseColor("#ffffff"));
                tv5.setTypeface(null, Typeface.BOLD);
                t1.addView(tv5);
                TextView tv6 = new TextView(getContext());
                tv6.setText("REMARK");
                tv6.setTextSize(15);
                tv6.setGravity(Gravity.LEFT);
                tv6.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                tv6.setPadding(10, 10, 10, 10);
                tv6.setGravity(Gravity.CENTER_HORIZONTAL);
                tv6.setTextColor(Color.parseColor("#ffffff"));
                tv6.setTypeface(null, Typeface.BOLD);
                t1.addView(tv6);
                TextView tv7 = new TextView(getContext());
                tv7.setText("SENIOR NAME");
                tv7.setTextSize(15);
                tv7.setPadding(10, 10, 25, 10);
                tv7.setGravity(Gravity.CENTER_HORIZONTAL);
                tv7.setTextColor(Color.parseColor("#ffffff"));
                tv7.setTypeface(null, Typeface.BOLD);
                t1.addView(tv7);
                TextView tv8 = new TextView(getContext());
                tv8.setText("SENIOR RATING");
                tv8.setTextSize(15);
                tv8.setPadding(10, 10, 10, 10);
                tv8.setGravity(Gravity.CENTER_HORIZONTAL);
                tv8.setTextColor(Color.parseColor("#ffffff"));
                tv8.setTypeface(null, Typeface.BOLD);
                t1.addView(tv8);
                TextView tv9 = new TextView(getContext());
                tv9.setText("SENIOR REMARK");
                tv9.setTextSize(15);
                tv9.setGravity(Gravity.LEFT);
                tv9 .setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                tv9.setPadding(10, 10, 10, 10);
                tv9.setGravity(Gravity.CENTER_HORIZONTAL);
                tv9.setTextColor(Color.parseColor("#ffffff"));
                tv9.setTypeface(null, Typeface.BOLD);
                t1.addView(tv9);

                tableLayout.addView(t1);

                for(int i=0;i<total.size();i++)
                {

                    tableRow[i] = new TableRow(getContext());
                    if (i % 2 != 0)
                        tableRow[i].setBackgroundColor(Color.parseColor("#cccccc"));
                    for (int j = 0; j < 9; j++) {
                        tv = new TextView(getContext());
                        tv.setPadding(10, 10, 10, 10);
                        tv.setGravity(Gravity.CENTER_HORIZONTAL);
                        tv.setTextSize(15);
                        if (j == 0)
                            tv.setText(date.get(i));
                        if (j == 1)
                            tv.setText(total.get(i));
                        if (j == 2) {
                            tv.setText(done.get(i));
                        }
                        if (j == 3) {
                            tv.setText(percentage.get(i));
                        }
                        if (j == 4) {
                            tv.setText(perfection.get(i));
                        }
                        if (j == 5) {
                            tv.setText(remark.get(i));
                            tv.setGravity(Gravity.LEFT);
                            tv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                        } if (j == 6) {
                            tv.setText(s_name.get(i));
                        } if (j == 7) {
                            tv.setText(s_rating.get(i));
                        } if (j == 8) {
                            tv.setText(s_remark.get(i));
                        }
                        tableRow[i].addView(tv);


                    }
                    tableLayout.addView(tableRow[i]);

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

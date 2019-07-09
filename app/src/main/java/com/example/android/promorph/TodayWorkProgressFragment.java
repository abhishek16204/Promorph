package com.example.android.promorph;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TodayWorkProgressFragment extends Fragment {
EditText activity_number,activity_done,work_percentage,remark;
Spinner spinner;
List<String> perfection=new ArrayList<>();
String work_perfection;
Button add,cancel;
    String done,number,percentage,remark_work,message=null,status=null;
    HttpResponse response;
    syncwithServer async;
    ArrayAdapter<String> adapter;
    AlertDialog.Builder builder1;
    Boolean isConnected;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.today_work_progress, container, false);
        add=view.findViewById(R.id.add);
        cancel=view.findViewById(R.id.cancel);
        activity_number=view.findViewById(R.id.no_of_activity);
        activity_done=view.findViewById(R.id.activity_done);
        work_percentage=view.findViewById(R.id.work_percentage);
        spinner=view.findViewById(R.id.spinner);
        remark=view.findViewById(R.id.remark);
        refresh();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                work_perfection=perfection.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(),DashboardActivity.class));
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                    number = activity_number.getText().toString().trim();
                    done = activity_done.getText().toString().trim();
                    percentage=work_percentage.getText().toString().trim();
                    remark_work=remark.getText().toString();


                try {
                    double per = (Double.parseDouble(done) / Double.parseDouble(number))*100;
                    if (per < (Double.parseDouble(work_percentage.getText().toString().trim()))) {
                        Toast.makeText(getContext(), "Value should not exceed actual work percentage", Toast.LENGTH_LONG).show();
                    return;
                    }
                }
                catch (NumberFormatException e)
                {
                    Toast.makeText(getContext(),"All Fields need to be filled",Toast.LENGTH_LONG).show();
                    return;
                }
                if(perfection.equals("")|| remark_work.equals("") )
                {
                    Toast.makeText(getContext(),"All Fields need to be filled",Toast.LENGTH_LONG).show();
                    return;

                }
                async=new syncwithServer();
                isConnected=ConnectivityReceiver.isConnected();
                ProfileDB pd=new ProfileDB(getContext());
                Cursor c=pd.getUserID();
                c.moveToNext();
                String user_id=c.getString(1);
                Log.e("user-id is",""+user_id);
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                String dateToStr = format.format(new Date());
                if( pd.progress_check(user_id,dateToStr))
                { Toast.makeText(getContext(), "Work Progress already filled today", Toast.LENGTH_SHORT).show();
                    activity_number.setText("");
                    activity_done.setText("");
                    refresh();
                    work_percentage.setText("");
                    remark.setText("");
                    remark.setCursorVisible(false);
                }
                else {
                   Toast.makeText(getContext(), "Work Progress will be filled today", Toast.LENGTH_SHORT).show();

                   if (isConnected) {
                       pd.today_progress(user_id,dateToStr,number,done,percentage,work_perfection,remark_work);
                        async.execute(number, done, percentage, work_perfection, remark_work, user_id);
                        activity_number.setText("");
                        activity_done.setText("");
                        work_percentage.setText("");
                        remark.setText("");
                        remark.setCursorVisible(false);
                    }
                    else
                    {
                        Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                }
                
                


            }

        });

        return view;
    }
    public void refresh(){

        perfection.clear();
        perfection.add("Select Rating");
      for(int i=0;i<11;i++)
      {
          perfection.add(i+"");
      }
        adapter=new ArrayAdapter(getContext(),android.R.layout.simple_spinner_item,perfection.toArray() );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    class syncwithServer extends AsyncTask<String,String,String>
    {
        ProgressDialog dialog=new ProgressDialog(getContext());
        @Override
        protected void onPreExecute() {
            dialog.setMessage("Sending data........Please Wait");
            dialog.setCancelable(false);
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            String no_of_activity=strings[0];
            String no_of_activity_done=strings[1];
            String percentage_of_work=strings[2];
            String work_perfection_rating=strings[3];
            String remark=strings[4];
            String user_id=strings[5];


            HttpClient httpClient = new DefaultHttpClient();


            HttpPost httpPost = new HttpPost("http://pms.promorph.in/pms_work_progress/");
            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(6);
            nameValuePair.add(new BasicNameValuePair("no_of_activity",no_of_activity));
            nameValuePair.add(new BasicNameValuePair("no_of_activity_done",no_of_activity_done));
            nameValuePair.add(new BasicNameValuePair("percentage_of_work",percentage_of_work));
            nameValuePair.add(new BasicNameValuePair("work_perfection_rating",work_perfection_rating));
            nameValuePair.add(new BasicNameValuePair("remark",remark));
            nameValuePair.add(new BasicNameValuePair("user_id",user_id));

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
                } else {
                    Log.e("Passed ", " sent data to server");

                    //Toast.makeText(context,"Successfully uploaded data to server.",Toast.LENGTH_LONG ).show();
                }
                Log.d("Http Response:", response.toString());
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
               // Toast.makeText(getContext(), "IO", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } catch (JSONException e) {
                //Toast.makeText(getContext(), "JSON", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            return status;



        }

        @Override
        protected void onPostExecute(String s) {
            if(s.equals("Success")) {
                dialog.cancel();
                builder1 = new AlertDialog.Builder(getContext());
                builder1.setTitle("ADDED SUCCESSFULLY");
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
            else
            {
                dialog.cancel();
                builder1 = new AlertDialog.Builder(getContext());
                builder1.setTitle("FAILED TO SEND DATA");
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
            super.onPostExecute(s);
        }
    }
}

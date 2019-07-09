package com.example.android.promorph;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {
    ProgressDialog dialog;
    String usid, pawrd;
    EditText username, password;
    AlertDialog.Builder builder1;
    String status;
    SharedPreferences loginData;
    public static final String str="loginData";
    Boolean isconnected;
    ImageView open_eye;
    String user_id;
    private  ProfileDB  pd =new ProfileDB(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginData  = getSharedPreferences(str,Context.MODE_PRIVATE);

        if(loginData.contains("username") && loginData.contains("password")){
            startActivity(new Intent(MainActivity.this,DashboardActivity.class));
        }

        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        Button login = findViewById(R.id.login);
        open_eye=findViewById(R.id.open_eye);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        open_eye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(password.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    open_eye.setImageDrawable(getResources().getDrawable(R.drawable.closed));
                    password.setInputType( InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                else {
                    open_eye.setImageDrawable(getResources().getDrawable(R.drawable.open));
                    password.setInputType( InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD );
                }
                password.setSelection(password.getText().length());
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setMessage("Authenticating........please wait");
                dialog.show();
                dialog.setCancelable(true);
                Log.d("Login Pressed", "In Login onCLick Listener");
                usid = username.getText().toString().trim();
                pawrd = password.getText().toString().trim();
                isconnected = ConnectivityReceiver.isConnected();
                if (usid.isEmpty() || pawrd.isEmpty()) {
                    dialog.cancel();
                    builder1 = new AlertDialog.Builder(v.getContext());
                    builder1.setTitle("Details can't be empty");
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
                else if( isconnected==true)

                    {

                        loginData = getSharedPreferences(str, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = loginData.edit();
                        editor.putString("username", usid);
                        editor.putString("password", pawrd);
                        editor.commit();
                        new JSONTask().execute("http://pms.promorph.in/user_login_api/?username=" + usid + "&password=" + pawrd);
                    }



                else if (isconnected == false) {
                    dialog.cancel();
                    builder1 = new AlertDialog.Builder(v.getContext());
                    builder1.setTitle("No Internet Connection!!");
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
                    builder1 = new AlertDialog.Builder(v.getContext());
                    builder1.setTitle("Username or Password Incorrect");
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
            }

        });
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

    }

    private class JSONTask extends AsyncTask<String, String, String> {
        @Override
        public String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {


                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse = httpClient.execute(new HttpGet(params[0]));
                String responseBody = EntityUtils.toString(httpResponse.getEntity());


                Log.e("vvv",responseBody);
                JSONObject parentObject = new JSONObject(responseBody);
                status = parentObject.getString("status");
                if (status.equals("Success")) {
                    JSONArray parentArray = parentObject.getJSONArray("lst");
                    JSONArray parentArray1 = parentObject.getJSONArray("leave_type");

                    for (int i = 0; i < parentArray.length(); i++) {
                        JSONObject finalobject = parentArray.getJSONObject(i);
                        String designation = finalobject.getString("designation");
                        user_id = finalobject.getString("user_id");
                        String name = finalobject.getString("name");
                        String role  = finalobject.getString("role");
                        if(!pd.id_already_exists(user_id))
                        pd.lst(designation,user_id,name,role);
                    }
                    for (int i = 0; i < parentArray1.length(); i++) {
                        JSONObject finalobject = parentArray1.getJSONObject(i);
                        String leave_id = finalobject.getString("leave_id");
                        String leave_type= finalobject.getString("leave_type");
                        pd.leave(leave_id,leave_type);
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
            Log.e("s value","    "+s);
            if(s==null)
            {
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "Slow internet connection", Toast.LENGTH_SHORT).show();
            }
            else if(s.equals("Success"))
            {
                dialog.cancel();
                Intent intent=new Intent(MainActivity.this,DashboardActivity.class);
                intent.putExtra("user-id",user_id);
                Log.e("send user id through  " ," "+user_id);
                startActivity(intent);

            }
            else
            {
                dialog.cancel();
                builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setTitle("Username or Password Incorrect");
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
        }
        }
    }



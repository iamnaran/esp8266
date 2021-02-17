package com.android.esp8266_wifi_ledcontrol;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private TCPClient mTcpClient;
    Switch ledSwitch;
    TextView reader;
    private int switchValue = 0;

    private TextView temperatureLabel;
    private TextView moistureLabel;
    private TextView waterLabel;
    private Handler handler;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().
                detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());

        initaliseView();
        initaliseListener();

    }

    private void initaliseView() {
        ledSwitch = (Switch) findViewById(R.id.led_switch);
        reader = (TextView) findViewById(R.id.textView);
        temperatureLabel = (TextView) findViewById(R.id.temperature_label);
        moistureLabel = (TextView) findViewById(R.id.moisture_label);
        waterLabel = (TextView) findViewById(R.id.water_label);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        reader.setText("Please wait...");

    }

    private void initaliseListener() {
        swipeRefreshLayout.setOnRefreshListener(this);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new ConnectTask().execute("");
            }
        }, 0, 2000);//put here time 1000 milliseconds=1 second

        ledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b) {
                    switchValue = 1;

                    final String sendValue = "pin=" + switchValue;

                    mTcpClient.sendMessage(sendValue);

                } else {
                    switchValue = 0;
                    final String sendValue = "pin=" + switchValue;

                    mTcpClient.sendMessage(sendValue);


                }
            }
        });


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onRefresh() {

        mTcpClient.sendMessage("4");

        handler.postDelayed(new Runnable() {
            public void run() {

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

            }
        }, 5000);




    }


    @SuppressLint("StaticFieldLeak")
    public class ConnectTask extends AsyncTask<String, String, TCPClient> {

        @Override
        protected TCPClient doInBackground(String... message) {


            //we create a TCPClient object and
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(final String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);

//                    final List<String> list = new ArrayList<String>(Arrays.asList(message.split(" , ")));
                    final List<String> list = Arrays.asList(message.split("\\s*,\\s*"));
                    if (!list.isEmpty()){
                        reader.post(new Runnable() {
                            public void run() {
                                reader.setText("Connection is active...");
                            }
                        });
                    }
                    temperatureLabel.post(new Runnable() {
                        public void run() {
                            temperatureLabel.setText(list.get(0));
                        }
                    });
                    moistureLabel.post(new Runnable() {
                        public void run() {
                            moistureLabel.setText(list.get(1));
                        }
                    });

                    waterLabel.post(new Runnable() {
                        public void run() {
                            waterLabel.setText(list.get(2));
                        }
                    });

                    Log.e("Message", message);
                    for (String data : list) {
                        Log.e("Message", data);

                    }


                }
            });
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(final String... values) {
            super.onProgressUpdate(values);
            reader.post(new Runnable() {
                @SuppressLint("SetTextI18n")
                public void run() {
                    reader.setText("Hmmmmm... "+values[0]);
                }
            });
            Log.d("values", values[0]);
            //in the arrayList we add the messaged received from server
//            arrayList.add(values[0]);
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
//            mAdapter.notifyDataSetChanged();
        }

    }

}

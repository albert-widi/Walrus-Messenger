package com.valge.champchat;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import com.valge.champchat.httppost.HttpPostModule;
import com.valge.champchat.list_view_adapter.TesterAdapter;
import com.valge.champchat.util.Message;
import com.valge.champchat.util.SharedPrefsUtil;

import org.json.JSONObject;

import java.util.ArrayList;

public class TesterActivity extends Activity {
    Switch testerSwitch;
    SharedPrefsUtil sharedPrefsUtil;
    BroadcastReceiver testerReceiver;
    TesterAdapter testerAdapter;
    ArrayList<Message> message = new ArrayList<Message>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tester);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tester, container, false);
            return rootView;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        sharedPrefsUtil = new SharedPrefsUtil(getApplicationContext());
        sharedPrefsUtil.loadApplicationPrefs();

        testerSwitch = (Switch) findViewById(R.id.tester_switch);
        if(sharedPrefsUtil.isTesterMode()) {
            testerSwitch.setChecked(true);
        }
        else {
            testerSwitch.setChecked(false);
        }

        testerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    setTester(true);
                }
                else {
                    setTester(false);
                }
            }
        });

        ListView messageList = (ListView) findViewById(R.id.tester_message_list_tester);
        testerAdapter = new TesterAdapter(getApplicationContext(), message);
        messageList.setAdapter(testerAdapter);
    }

    private void setTester(boolean testerVal) {
        final boolean tester = testerVal;
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    System.out.println("Setting tester");
                    JSONObject jsonResponse;
                    String postAction = "setTester";
                    String testerValue = " ";
                    if(tester) {
                        testerValue = "tester";
                    }
                    else {
                        testerValue = "notester";
                    }
                    String[] postData= {String.valueOf(sharedPrefsUtil.userId), testerValue};
                    String[] postDataName = {"id", "tester"};

                    HttpPostModule httpPostModule = new HttpPostModule();
                    jsonResponse = httpPostModule.echatHttpPost(postAction, postData, postDataName);

                    String status = jsonResponse.getString("message");
                    //String userName = jsonResponse.getString("name");

                    if(status.equalsIgnoreCase("SET_TESTER_SUCCESS")) {
                        if(tester) {
                            System.out.println("Tester mode on");
                        }
                        else {
                            System.out.println("Tester mode off");
                        }
                    }


                }
                catch(Exception e) {
                    e.printStackTrace();
                }

                return "";
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if(tester) {
                    sharedPrefsUtil.setToTesterModeOn();
                }
                else {
                    sharedPrefsUtil.setTesterModeOff();
                }
            }
        }.execute(null, null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        testerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("Messaging Activity : Processing message to adapter/notification");
                //get data from intent
                String message = intent.getStringExtra("message");
                String date = intent.getStringExtra("date");
                String time = intent.getStringExtra("time");
                String name = intent.getStringExtra("name");

                final Message newMessage = new Message(message, name, date, time, "", 1);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        testerAdapter.add(newMessage);
                        testerAdapter.notifyDataSetChanged();
                    }
                });

            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(testerReceiver, new IntentFilter("messagingtester"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(testerReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(testerReceiver);
    }
}

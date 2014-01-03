package com.valge.champchat;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.valge.champchat.gcm_package.GCMBroadcastReceiver;
import com.valge.champchat.httppost.HttpPostModule;
import com.valge.champchat.util.DbAdapter;
import com.valge.champchat.util.HttpPostUtil;
import com.valge.champchat.util.IntentExtrasUtil;
import com.valge.champchat.util.SharedPrefsUtil;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.util.Iterator;

public class ApplicationActivationActivity extends Activity {
    //db
    DbAdapter dbAdapter;

    //textview
    TextView imActivationStatus;

    //receiver
    GCMBroadcastReceiver broadCastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_activation);
        this.setTitle("Activating Application");
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
            View rootView = inflater.inflate(R.layout.fragment_application_activation, container, false);
            return rootView;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        imActivationStatus = (TextView) findViewById(R.id.activation_status);
        imActivationStatus.setText("Set Application Properties");
        saveApplicationPreferences();
        //imActivationStatus.setText("Finding Friends From Contacts...");
        //getFriendList();
        imActivationStatus.setText("Application activation complete");
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Intent intent = new Intent(ApplicationActivationActivity.this, ChatActivity.class);
                startActivity(intent);
                ApplicationActivationActivity.this.finish();
            }
        }, 5000);
    }

    private void saveApplicationPreferences() {
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object... params) {
                // TODO Auto-generated method stub
                Context context = getApplicationContext();
                Intent intent = getIntent();
                int userId = intent.getExtras().getInt(IntentExtrasUtil.XTRAS_ACTIV_USER_ID);
                String userName = intent.getExtras().getString(IntentExtrasUtil.XTRAS_ACTIV_USERNAME);
                String stringPrivateKey = intent.getExtras().getString(IntentExtrasUtil.XTRAS_ACTIV_PRIVATEKEY);
                String regid = intent.getExtras().getString(IntentExtrasUtil.XTRAS_ACTIV_GCMID);
                String phoneNumber = intent.getExtras().getString(IntentExtrasUtil.XTRAS_ACTIV_PHONENUMBER);
                String secretKey = intent.getExtras().getString(IntentExtrasUtil.XTRAS_ACTIV_SECRET_KEY);

                final SharedPreferences prefs = getAppPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(SharedPrefsUtil.KEY_APP_ACTIV, true);
                editor.putInt(SharedPrefsUtil.KEY_USER_ID, userId);
                editor.putString(SharedPrefsUtil.KEY_USER_NAME, userName);
                editor.putString(SharedPrefsUtil.KEY_PRIVATE_KEY, stringPrivateKey);
                editor.putString(SharedPrefsUtil.KEY_PHONE_NUMBER, phoneNumber);
                editor.putString(SharedPrefsUtil.PROPERTY_REG_ID, regid);
                editor.putString(SharedPrefsUtil.KEY_SECRET_KEY, secretKey);
                editor.putBoolean(SharedPrefsUtil.KEY_TESTER_MODE, false);
                editor.putBoolean(SharedPrefsUtil.KEY_HISTORY, true);
                editor.putBoolean(SharedPrefsUtil.KEY_SOUND_NOTIFICATION, true);
                editor.putBoolean(SharedPrefsUtil.KEY_SOUND_MESSAGE, true);
                editor.commit();
                return "";
            }


            protected void onPostExecute(Object result) {

            };
        }.execute(null, null, null);


    }

    private SharedPreferences getAppPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(SharedPrefsUtil.PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void getFriendList() {
        System.out.println("Getting friend list activation");
        new AsyncTask() {
            String stringResponse = "";
            JSONObject jsonResponse;

            @Override
            protected Object doInBackground(Object[] params) {
                //Toast.makeText(MainActivity.this, "Refreshing friend list", Toast.LENGTH_SHORT).show();
                String friendList = getPhoneNumberFromContacts();

                //do http post
                final HttpClient httpClient = new DefaultHttpClient();
                final HttpPost httpPost = new HttpPost(HttpPostUtil.postURL);
                JSONObject json = new JSONObject();

                try {
                    System.out.println("Friend list : "  + friendList);
                    String postAction = "getFriendList";
                    String[] postData = {friendList};
                    String[] postDataName = {"friendlist"};

                    HttpPostModule httpPostModule = new HttpPostModule();
                    jsonResponse = httpPostModule.echatHttpPost(postAction, postData, postDataName);

                    if(jsonResponse.getString("message").equalsIgnoreCase("FRIEND_SEARCH_SUCCESS")) {
                        System.out.println("eChat Application Activity: Friend search success");
                        Iterator<?> keys = jsonResponse.keys();
                        //skipping non friends data keys
                        while(keys.hasNext()) {
                            String key = (String) keys.next();
                            System.out.println("Key : " + key);
                            if(!key.equalsIgnoreCase("MESSAGE") && !key.equalsIgnoreCase("ERROR")) {
                                System.out.println("Processing friends data...");
                                String nonSplitedData = jsonResponse.get(key).toString();
                                String[] splitedData = nonSplitedData.split(";");

                                int id = Integer.parseInt(splitedData[0]);
                                String name = splitedData[1];
                                String phoneNumber = key;
                                String gcmId = splitedData[2];

                                String publicKeyString = splitedData[3];
                                byte[] decodedKey = Base64.decode(publicKeyString, Base64.DEFAULT);


                                //debug
                                System.out.println("eChat Application Activity: Friend ID = " + id);
                                System.out.println("eChat Application Activity: Friend Name = " + name);
                                System.out.println("eChat Application Activity: Phone Number = " + phoneNumber);
                                System.out.println("eChat Application Activity: GCM ID = " + gcmId);
                                System.out.println("eChat Application Activity: Friend Public Key = " + publicKeyString);

                                if(decodedKey == null) {
                                    System.out.println("ini null coi");
                                }
                                /*/saving friend to db
                                if(!dbAdapter.saveFriend(id, name, phoneNumber, decodedKey)) {
                                    System.out.println("Refresh friend list: Save friend to db failed");
                                }
                                else {
                                    System.out.println("Refresh friend list : Save to db success");
                                }*/
                            }
                        }
                    }
                    else {
                        System.out.println("GAGAL");
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }

                System.out.println("Processing complete...");
                return "";
            }

            protected void onPostExecute(Object result) {
                imActivationStatus.setText("Setting receiver");

                imActivationStatus.setText("Acitvation Success");

                Intent intent = new Intent(ApplicationActivationActivity.this, ChatActivity.class);
                startActivity(intent);
                ApplicationActivationActivity.this.finish();
                /*new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        try {
                            //Thread.sleep(2500);
                            Intent intent = new Intent(ApplicationActivationActivity.this, ChatActivity.class);
                            startActivity(intent);
                            ApplicationActivationActivity.this.finish();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }).run();*/
                //go to main activity
            };
        }.execute(null, null, null);
    }

    private String getPhoneNumberFromContacts() {
        String friendList = "";

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                null, null, null);

        System.out.println("Contacts lenght = " + cur.getCount());
        if(cur.getCount() > 0) {
            while(cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +
                                    " = ?", new String[] { id },
                            null);

                    while(pCur.moveToNext()) {
                        String name = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String phoneNo =   pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phoneNo = phoneNo.replaceAll("-", "");
                        phoneNo = phoneNo.replaceAll(" ", "");
                        friendList += phoneNo + ";";
                        System.out.println("Name : " + name + "Number : " + phoneNo);
                    }

                    pCur.close();
                }
            }

            int stringLength = friendList.length();
            System.out.println("Friend list length = " + stringLength);
            friendList = friendList.substring(0, stringLength-1);
            System.out.println("Friend list: " + friendList);
        }
        else {
            //Toast.makeText(context, text, duration)
        }
        cur.close();
        return friendList;
    }
}

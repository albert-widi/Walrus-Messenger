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
import android.provider.ContactsContract;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.valge.champchat.gcm_package.GCMBroadcastReceiver;
import com.valge.champchat.util.DbAdapter;
import com.valge.champchat.util.HttpPostUtil;
import com.valge.champchat.util.IntentExtrasUtil;
import com.valge.champchat.util.SharedPrefsUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        imActivationStatus.setText("Finding Friends From Contacts...");
        getFriendList();
    }

    private void saveApplicationPreferences() {
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object... params) {
                // TODO Auto-generated method stub
                Context context = getApplicationContext();
                Intent intent = getIntent();
                String userName = intent.getExtras().getString(IntentExtrasUtil.XTRAS_ACTIV_USERNAME);
                String stringPrivateKey = intent.getExtras().getString(IntentExtrasUtil.XTRAS_ACTIV_PRIVATEKEY);
                String regid = intent.getExtras().getString(IntentExtrasUtil.XTRAS_ACTIV_GCMID);

                final SharedPreferences prefs = getAppPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(SharedPrefsUtil.KEY_APP_ACTIV, true);
                editor.putString(SharedPrefsUtil.KEY_USER_NAME, userName);
                editor.putString(SharedPrefsUtil.KEY_PRIVATE_KEY, stringPrivateKey);
                editor.putString(SharedPrefsUtil.PROPERTY_REG_ID, regid);
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
                String friendList = "";
                ContentResolver cr = getContentResolver();
                Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                        null, null, null);

                if(cur.getCount() >0) {
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
                                friendList += phoneNo + ";";
                                System.out.println("Name : " + name + "Number : " + phoneNo);
                            }

                            pCur.close();
                        }
                    }

                    int stringLength = friendList.length();
                    friendList = friendList.substring(0, stringLength-1);
                }
                else {
                    //Toast.makeText(context, text, duration)
                }

                cur.close();

                //do http post
                final HttpClient httpClient = new DefaultHttpClient();
                final HttpPost httpPost = new HttpPost(HttpPostUtil.postURL);
                JSONObject json = new JSONObject();

                try {
                    json.put("action", "getFriendList");
                    json.put("friendlist", friendList);

                    //System.out.println("Json : " + json.toString());

                    List<NameValuePair> pairs = new ArrayList<NameValuePair>(4);
                    pairs.add(new BasicNameValuePair("data", json.toString()));

                    httpPost.setEntity(new UrlEncodedFormEntity(pairs));

                    HttpResponse response = httpClient.execute(httpPost);
                    HttpEntity resEntity = response.getEntity();

                    stringResponse = EntityUtils.toString(resEntity);
                    //System.out.println("Response : " + stringResponse);
                    jsonResponse = new JSONObject(stringResponse);
                    System.out.println("JSON : " + jsonResponse.toString());

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

                                String name = splitedData[0];
                                String phoneNumber = key;
                                String gcmId = splitedData[1];
                                byte[] decodedKey = Base64.decode(splitedData[2], Base64.DEFAULT);
                                PublicKey tmpPublicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decodedKey));

                                //debug
                                System.out.println("eChat Application Activity: Friend Name = " + name);
                                System.out.println("eChat Application Activity: Phone Number = " + phoneNumber);
                                System.out.println("eChat Application Activity: GCM ID = " + gcmId);

                                //saving friend to db
                                if(!dbAdapter.saveFriend(name, phoneNumber, gcmId, decodedKey)) {
                                    System.out.println("Save friend to db failed, name :" + name);
                                }
                                else {
                                    System.out.println("Save to DB success, name : " + name);
                                }
                            }
                        }
                    }
                    else {
                        System.out.println("GAGAL");
                    }
                }
                catch(Exception e) {

                }

                System.out.println("Processing complete...");
                return "";
            }

            protected void onPostExecute(Object result) {
                imActivationStatus.setText("Setting receiver");

                imActivationStatus.setText("Acitvation Success");
                new Thread(new Runnable() {

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
                }).run();
                //go to main activity
            };
        }.execute(null, null, null);
    }
}

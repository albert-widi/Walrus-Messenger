package com.valge.champchat;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.valge.champchat.httppost.HttpPostModule;
import com.valge.champchat.util.DbAdapter;
import com.valge.champchat.util.HttpPostUtil;
import com.valge.champchat.util.IntentExtrasUtil;
import com.valge.champchat.util.SharedPrefsUtil;

import org.apache.commons.logging.Log;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

public class UserDataRegistrationActivity extends Activity {
    //RSA key
    PublicKey publicKey;
    PrivateKey privateKey;
    static final String TAG = "EAndroidIM";

    Context context;

    //db
    private DbAdapter dbAdapter;

    //gcm
    private GoogleCloudMessaging gcm;
    private String regid;
    private String SENDER_ID = "296103438650";

    //shared preferences
    public static final String PROPERTY_REG_ID = "gcm_id";

    //registration data
    private String action;
    private int userId;
    private String userName;
    private String secretKey;
    private String phoneNumber;

    //json object
    private JSONObject jsonResponse;

    //button
    Button registerButton;
    //progress
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data_registration);

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
            View rootView = inflater.inflate(R.layout.fragment_user_data_registration, container, false);
            return rootView;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        progressBar = (ProgressBar) findViewById(R.id.progress_user_data_regis);
        progressBar.setVisibility(View.INVISIBLE);
        context = getApplicationContext();
        dbAdapter = new DbAdapter(context);

        final HttpClient httpClient = new DefaultHttpClient();
        final HttpPost httpPost = new HttpPost(HttpPostUtil.postURL);

        final EditText phoneNumberEdit = (EditText) findViewById(R.id.register_phone_number);
        final EditText userNameEdit = (EditText) findViewById(R.id.register_user_name);
        //final EditText secretKeyEdit = (EditText) findViewById(R.id.register_secret_pass);

        Intent intent = getIntent();
        userName = intent.getExtras().getString(IntentExtrasUtil.XTRAS_NAME_USERNAME);
        phoneNumber = intent.getExtras().getString(IntentExtrasUtil.XTRAS_PHONENUMBER);
        action = intent.getExtras().getString(IntentExtrasUtil.XTRAS_ACTION);

        phoneNumberEdit.setText(phoneNumber);
        userNameEdit.setText(userName);

        registerButton = (Button) findViewById(R.id.reg_button);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                registerButton.setEnabled(false);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(registerButton.getWindowToken(), 0);

                // TODO Auto-generated method stub
                new AsyncTask() {

                    @Override
                    protected Object doInBackground(Object[] params) {
                        //secretKey = secretKeyEdit.getText().toString();
                        userName = userNameEdit.getText().toString();
                        phoneNumber = phoneNumberEdit.getText().toString();

                        createRSAKey();

                        gcm = GoogleCloudMessaging.getInstance(context);
                        regid = getRegistrationId(context);

                        System.out.println("Phone Number : " + phoneNumber);
                        System.out.println("User Name : " + userName);
                        System.out.println("Secret Key : " + secretKey);
                        System.out.println("GCM ID : " + regid);
                        System.out.println("Private Key : " + privateKey.toString());

                        if (regid.isEmpty()) {
                            registerInBackground();
                        }
                        else {
                            saveRegistrationDataToBackend();
                        }

                        return "";
                    }
                }.execute(null, null, null);
            }
        });
    }

    private void createRSAKey() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024, new SecureRandom());
            KeyPair keyPair = kpg.generateKeyPair();
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getAppPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        //Toast.makeText(context, registrationId, Toast.LENGTH_SHORT).show();
        Log Log;
        if (registrationId.isEmpty()) {
            //Log.i(TAG, "Registration not found.");
            return "";
        }

        return registrationId;
    }

    private void activateApplication() {
        String stringPrivateKey = Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT);

        Intent intent = new Intent(UserDataRegistrationActivity.this, ApplicationActivationActivity.class);
        intent.putExtra(IntentExtrasUtil.XTRAS_ACTIV_USER_ID, userId);
        intent.putExtra(IntentExtrasUtil.XTRAS_ACTIV_USERNAME, userName);
        intent.putExtra(IntentExtrasUtil.XTRAS_ACTIV_PRIVATEKEY, stringPrivateKey);
        intent.putExtra(IntentExtrasUtil.XTRAS_ACTIV_GCMID, regid);
        intent.putExtra(IntentExtrasUtil.XTRAS_ACTIV_PHONENUMBER, phoneNumber);
        intent.putExtra(IntentExtrasUtil.XTRAS_ACTIV_SECRET_KEY, secretKey);
        startActivity(intent);
        UserDataRegistrationActivity.this.finish();
    }

    private SharedPreferences getAppPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(SharedPrefsUtil.PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void registerInBackground() {
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    regid = gcm.register(SENDER_ID);
                    System.out.println("Device registered : " + regid);
                }
                catch(IOException e) {
                    System.out.println(e.getMessage());
                }
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }

                return "";
            }

            protected void onPostExecute(Object result) {
                saveRegistrationDataToBackend();
            };
        }.execute(null, null, null);
    }

    private void unsetRegistrationData() {
        dbAdapter.deleteRegisteredUser(phoneNumber);
    }

    private void saveRegistrationDataToBackend() {

        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    String stringPublicKey = Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
                    String postAction = action;
                    String[] postData = {phoneNumber, userName, regid, stringPublicKey};
                    String[] postDataName = {"phonenumber", "username", "gcmid", "publickey"};

                    HttpPostModule httpPostModule = new HttpPostModule();
                    jsonResponse = httpPostModule.echatHttpPost(postAction, postData, postDataName);
                }
                catch(Exception e) {
                    e.printStackTrace();

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            progressBar.setVisibility(View.INVISIBLE);
                            registerButton.setEnabled(true);
                            Toast.makeText(UserDataRegistrationActivity.this, "Failed, please check your internet connection", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                return "";
            }

            protected void onPostExecute(Object result) {
                try {
                    if(jsonResponse.getString("message").equalsIgnoreCase("REG_SUCCESS")) {
                        userId = jsonResponse.getInt("userid");

                        if(dbAdapter.registerUser(userId, phoneNumber, userName, regid, "staticfornow", privateKey.getEncoded())) {
                            activateApplication();
                        }
                        else {
                            System.out.println("ERROR SAVE TO DB");
                        }
                    }
                    else {
                        unsetRegistrationData();
                        progressBar.setVisibility(View.INVISIBLE);
                        registerButton.setEnabled(true);
                        Toast.makeText(UserDataRegistrationActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            };
        }.execute(null, null, null);
    }
}

package com.valge.champchat;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.valge.champchat.httppost.HttpPostModule;
import com.valge.champchat.util.HttpPostUtil;
import com.valge.champchat.util.IntentExtrasUtil;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

public class PhoneNumberRegistrationActivity extends Activity {
    //button
    Button confirmButton;
    //progress
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number_registration);

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
            View rootView = inflater.inflate(R.layout.fragment_phone_number_registration, container, false);
            return rootView;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        progressBar = (ProgressBar) findViewById(R.id.progress_phone_number_confirm);
        progressBar.setVisibility(View.INVISIBLE);
        confirmButton = (Button) findViewById(R.id.confirm_button);
        Button appInfoButton = (Button) findViewById(R.id.appinfo_button);
        final EditText editPhoneCode = (EditText) findViewById(R.id.check_phone_code);
        final EditText editPhoneNumber;
        editPhoneNumber = (EditText) findViewById(R.id.check_phone_number);

        final HttpClient httpClient = new DefaultHttpClient();
        final HttpPost httpPost = new HttpPost(HttpPostUtil.postURL);

        confirmButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(confirmButton.getWindowToken(), 0);
                confirmButton.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                final String phoneCode = editPhoneCode.getText().toString();
                final String phoneNumber = editPhoneNumber.getText().toString();

                final String fixedPhoneNumber = phoneCode+phoneNumber;

                doConfirmationOnBackground(fixedPhoneNumber);
            }
        });

        appInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PhoneNumberRegistrationActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
    }

    private void doConfirmationOnBackground(String fixedPhoneNumberString) {
        final String fixedPhoneNumber = fixedPhoneNumberString;
        new AsyncTask() {
            String stringResponse = "";
            JSONObject jsonResponse;

            @Override
            protected Object doInBackground(Object[] params) {
                JSONObject json = new JSONObject();

                try {
                    String postAction = "checkreg";
                    String[] postData= {fixedPhoneNumber};
                    String[] postDataName = {"phonenumber"};

                    HttpPostModule httpPostModule = new HttpPostModule();
                    jsonResponse = httpPostModule.echatHttpPost(postAction, postData, postDataName);
                }
                catch(Exception e) {
                    e.printStackTrace();

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            confirmButton.setEnabled(true);
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(PhoneNumberRegistrationActivity.this, "Failed, please check your internet connection", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                return "";
            }

            protected void onPostExecute(Object result) {
                try {
                    Intent intent = new Intent(PhoneNumberRegistrationActivity.this, UserDataRegistrationActivity.class);

                    String status = jsonResponse.getString("status");
                    String userName = jsonResponse.getString("name");
                    if(status.equalsIgnoreCase("exists")) {
                        intent.putExtra(IntentExtrasUtil.XTRAS_PHONENUMBER, fixedPhoneNumber);
                        intent.putExtra(IntentExtrasUtil.XTRAS_NAME_USERNAME, userName);
                        intent.putExtra(IntentExtrasUtil.XTRAS_ACTION, "updateuser");
                    }
                    else {
                        intent.putExtra(IntentExtrasUtil.XTRAS_PHONENUMBER, fixedPhoneNumber);
                        intent.putExtra(IntentExtrasUtil.XTRAS_NAME_USERNAME, "");
                        intent.putExtra(IntentExtrasUtil.XTRAS_ACTION, "register");
                    }

                    confirmButton.setEnabled(true);
                    progressBar.setVisibility(View.INVISIBLE);
                    startActivity(intent);
                    PhoneNumberRegistrationActivity.this.finish();
                }
                catch(Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            confirmButton.setEnabled(true);
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(PhoneNumberRegistrationActivity.this, "Failed, please check your internet connection", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            };
        }.execute(null, null, null);
    }
}

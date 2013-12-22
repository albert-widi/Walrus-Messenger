package com.valge.champchat;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.valge.champchat.httppost.HttpPostModule;
import com.valge.champchat.util.HttpPostUtil;
import com.valge.champchat.util.IntentExtrasUtil;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

public class PhoneNumberRegistrationActivity extends Activity {

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

        Button confirmButton = (Button) findViewById(R.id.confirm_button);
        final EditText editPhoneCode = (EditText) findViewById(R.id.check_phone_code);
        final EditText editPhoneNumber;
        editPhoneNumber = (EditText) findViewById(R.id.check_phone_number);

        final HttpClient httpClient = new DefaultHttpClient();
        final HttpPost httpPost = new HttpPost(HttpPostUtil.postURL);

        confirmButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                final String phoneCode = editPhoneCode.getText().toString();
                final String phoneNumber = editPhoneNumber.getText().toString();

                final String fixedPhoneNumber = phoneCode+phoneNumber;

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

                            startActivity(intent);
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    };
                }.execute(null, null, null);
            }
        });
    }
}

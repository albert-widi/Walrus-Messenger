package com.valge.champchat;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.valge.champchat.gcm_package.GCMBroadcastReceiver;
import com.valge.champchat.list_view_adapter.MessagingAdapter;
import com.valge.champchat.util.IntentExtrasUtil;
import com.valge.champchat.util.Message;
import com.valge.champchat.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MessagingActivity extends Activity {
    ArrayList<Message> message = new ArrayList<Message>();
    Context context;
    GCMBroadcastReceiver broadCastReceiver;

    MessagingAdapter messagingAdapater;

    //user
    private String userName;
    private byte[] privateKey;

    //friend variable
    private String friendName;
    private String friendPhoneNumber;
    private String friendGcmId;
    byte[] friendPublicKey;

    //callendar
    GregorianCalendar gCalendar;

    //button
    Button sendButton;
    //editext
    EditText editMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        context = getApplicationContext();
        gCalendar = new GregorianCalendar();

        Intent intent = getIntent();
        friendName = intent.getExtras().getString(IntentExtrasUtil.XTRAS_FRIEND_NAME);
        friendPhoneNumber = intent.getExtras().getString(IntentExtrasUtil.XTRAS_FRIEND_PHONENUMBER);
        friendGcmId = intent.getExtras().getString(IntentExtrasUtil.XTRAS_FRIEND_GCMID);
        friendPublicKey = intent.getExtras().getByteArray(IntentExtrasUtil.XTRAS_FRIEND_PUBLICKEY);
        userName = intent.getExtras().getString(IntentExtrasUtil.XTRAS_USER_NAME);
        privateKey = intent.getExtras().getByteArray(IntentExtrasUtil.XTRAS_USER_PRIVATE_KEY);

        this.setTitle(friendName);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.messaging, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            View rootView = inflater.inflate(R.layout.fragment_messaging, container, false);
            return rootView;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ListView messageList = (ListView) findViewById(R.id.message_list);
        messagingAdapater = new MessagingAdapter(context, message);
        messageList.setAdapter(messagingAdapater);

        sendButton = (Button) findViewById(R.id.send_button);
        editMessage = (EditText) findViewById(R.id.enter_message);

        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                sendMessage();
            }
        });
    }

    public void sendMessage() {
        String mText = editMessage.getText().toString();
        if(mText.isEmpty()) {
            return;
        }
        //Toast.makeText(context, mText, Toast.LENGTH_SHORT).show();
        String date = gCalendar.get(Calendar.DATE) + "-" + gCalendar.get(Calendar.MONTH) + "-" + gCalendar.get(Calendar.YEAR) + " /";
        String time = gCalendar.get(Calendar.HOUR) + ":" + gCalendar.get(Calendar.MINUTE);

        Message messageObject = new Message(mText, userName, date, time);
        message.add(messageObject);
        messagingAdapater.notifyDataSetChanged();
        editMessage.setText("");
    }

    public void messageHandler() {

    }
}

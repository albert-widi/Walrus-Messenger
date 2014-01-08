package com.valge.champchat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.valge.champchat.gcm_package.GCMBroadcastReceiver;
import com.valge.champchat.httppost.HttpPostModule;
import com.valge.champchat.list_view_adapter.MessagingAdapter;
import com.valge.champchat.util.ActivityLocationSharedPrefs;
import com.valge.champchat.util.ChampNotification;
import com.valge.champchat.util.DbAdapter;
import com.valge.champchat.util.EncryptionUtil;
import com.valge.champchat.util.IntentExtrasUtil;
import com.valge.champchat.util.Message;
import com.valge.champchat.util.MessageEncrypt;
import com.valge.champchat.util.SharedPrefsUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MessagingActivity extends Activity {
    ArrayList<Message> message = new ArrayList<Message>();
    Context context;
    GCMBroadcastReceiver broadCastReceiver;

    MessagingAdapter messagingAdapater;
    SharedPrefsUtil sharedPrefsUtil;

    //user
    private String userName;
    private int userId;

    //friend variable
    private int friendId;
    private String friendName;
    private String friendPhoneNumber;
    byte[] friendPublicKey;

    //callendar
    GregorianCalendar gCalendar;

    //button
    Button sendButton;
    //editext
    EditText editMessage;

    //broadCastReceiver
    BroadcastReceiver onPauseReceiver;
    BroadcastReceiver onResumeReceiver;
    BroadcastReceiver onStopReceiver;

    //media player
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        Context context = getApplicationContext();
        ActivityLocationSharedPrefs activityLocationSharedPrefs = new ActivityLocationSharedPrefs(context);
        activityLocationSharedPrefs.saveLastActivityToChat();

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        context = getApplicationContext();
        gCalendar = new GregorianCalendar();

        Intent intent = getIntent();
        friendId = intent.getExtras().getInt(IntentExtrasUtil.XTRAS_FRIEND_USER_ID);
        friendName = intent.getExtras().getString(IntentExtrasUtil.XTRAS_FRIEND_NAME);
        friendPhoneNumber = intent.getExtras().getString(IntentExtrasUtil.XTRAS_FRIEND_PHONENUMBER);
        friendPublicKey = intent.getExtras().getByteArray(IntentExtrasUtil.XTRAS_FRIEND_PUBLICKEY);

        System.out.println("Friend ID : " + friendId);

        sharedPrefsUtil = new SharedPrefsUtil(context);
        sharedPrefsUtil.loadApplicationPrefs();
        userName = sharedPrefsUtil.userName;
        userId = sharedPrefsUtil.userId;

        this.setTitle(friendName);
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
            View rootView = inflater.inflate(R.layout.fragment_test, container, false);
            return rootView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.messaging, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_action_call:
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+friendPhoneNumber));
                startActivity(callIntent);
                return true;

            case R.id.action_contact_details:
                AlertDialog.Builder adbContactDetails = new AlertDialog.Builder(this);
                adbContactDetails.setTitle("Contact Details:");
                adbContactDetails
                .setMessage(friendName + "\n" + friendPhoneNumber)
                .setCancelable(true);
                adbContactDetails.show();
                return true;

            case R.id.action_delete_all_message:
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle("Clear Chat");

                adb
                .setMessage("All message will be deleted?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAllChat();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                adb.show();
                return true;

            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.messaging_listview, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_delete_message:
                deleteChat(info.position);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ListView messageList = (ListView) findViewById(R.id.message_list);
        messagingAdapater = new MessagingAdapter(getApplicationContext(), message);
        messageList.setAdapter(messagingAdapater);
        registerForContextMenu(messageList);

        sendButton = (Button) findViewById(R.id.send_button);
        editMessage = (EditText) findViewById(R.id.enter_message);

        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                sendMessage();
                if(sharedPrefsUtil.isMessagingSoundOn()) {
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.message);
                    mediaPlayer.start();
                }
            }
        });

        /*messageList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                return false;
            }
        });*/

        //load message from db
        loadChatContent();
    }

    public void sendMessage() {
        String mText = editMessage.getText().toString();
        if(mText.isEmpty()) {
            return;
        }
        //Toast.makeText(context, mText, Toast.LENGTH_SHORT).show();
        String date = getCurrentDate();
        String time = getCurrentTime();

        sendMessageToBackend(mText, date, time);
        //setNewMessage(mText, date, time);
        /*if(!message.isEmpty()) {
            int size = message.size() - 1;
            System.out.println("Date previous : " + message.get(size).date + " , Date Now : " + date);
            System.out.println("Time previous : " + message.get(size).time + " , Time Now : " + time);
            if(message.get(size).date.equals(date) && message.get(size).time.equals(time)) {
                message.get(size).text += "\n\n" + mText;
                messagingAdapater.notifyDataSetChanged();
            }
            else {
                setNewMessage(mText, date, time);
            }
        }
        else {
            setNewMessage(mText, date, time);
        }*/

        editMessage.setText("");
    }

    private void setNewMessage(String text, String date, String time) {
        Message messageObject = new Message(text, userName, date, time, "SEND", 2);
        message.add(messageObject);
        //sendMessageToBackend(messageObject);
        messagingAdapater.notifyDataSetChanged();
    }

    private String getCurrentDate() {
        return gCalendar.get(Calendar.DATE) + "-" + gCalendar.get(Calendar.MONTH)+1 + "-" + gCalendar.get(Calendar.YEAR);
    }

    private String getCurrentTime() {
        return gCalendar.get(Calendar.HOUR) + ":" + gCalendar.get(Calendar.MINUTE);
    }

    private void sendMessageToBackend(String messageText, String date, String time) {
        final Message messageToSend = new Message(messageText, userName, date, time, "SENT", 2);

        new AsyncTask() {
            JSONObject jsonResponse = new JSONObject();
            long insertId;
            DbAdapter asyncDbAdapter = new DbAdapter(getApplicationContext());
            @Override
            protected Object doInBackground(Object[] params) {
                //display message
                message.add(messageToSend);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        messagingAdapater.notifyDataSetChanged();
                    }
                });

                int history = 0;
                if(sharedPrefsUtil.isMessageHistoryOn()) {
                    history = 1;
                }
                //save message to db
                insertId = asyncDbAdapter.saveMessage(friendId, friendPhoneNumber, userName, messageToSend.text, messageToSend.date, messageToSend.time, "SENT", "2", history);
                asyncDbAdapter.saveChatThread(friendId);
                System.out.println("Send insert id : " + insertId);
                if(insertId != -1) {
                    messageToSend.id = insertId;
                    System.out.println("Processing messing activity : Save message success");
                }
                else {
                    System.out.println("Processing messaging activity : Save message failed");
                }

                System.out.println("Send Message To Backend : Encrypt message");
                EncryptionUtil encryptionUtil = new EncryptionUtil();
                MessageEncrypt messageEncrypt = encryptionUtil.encryptMessage(messageToSend.text, friendPublicKey, getApplicationContext());

                String postAction = "sendMessage";
                String[] postData = {String.valueOf(userId), String.valueOf(friendId), messageEncrypt.encryptedMessage, messageEncrypt.messageKey, messageEncrypt.messageHash};
                String[] postDataName = {"idsender", "idreceive", "message", "messagekey", "messagehash"};

                System.out.println("Send Message To Backend : Send message to backend");
                try {
                    HttpPostModule httpPostModule = new HttpPostModule();
                    jsonResponse = httpPostModule.echatHttpPost(postAction, postData, postDataName);
                }
                catch(Exception e) {
                    e.printStackTrace();
                    messageToSend.status = "FAILED";
                    asyncDbAdapter.updateMessage(insertId, "FAILED");
                }


                return "";
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                try {
                    if(jsonResponse.getString("message").equalsIgnoreCase("SEND_SUCCESS")) {
                        messageToSend.status = "DELIVERED";
                        asyncDbAdapter.updateMessage(insertId, "DELIVERED");
                    }
                    else {
                        messageToSend.status = "FAILED";
                        asyncDbAdapter.updateMessage(insertId, "FAILED");
                    }

                    ActivityLocationSharedPrefs activityLocationSharedPrefs = new ActivityLocationSharedPrefs(getApplicationContext());
                    if(activityLocationSharedPrefs.isChatActivityActive()) {
                        //refresh adapter
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                messagingAdapater.notifyDataSetChanged();
                            }
                        });
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute(null, null, null);
    }

    private void loadChatContent() {
        DbAdapter dbAdapter = new DbAdapter(getApplicationContext());
        Cursor chatContentCursor = dbAdapter.getMessage(String.valueOf(friendId));
        if(chatContentCursor.getCount() > 0) {
            while(chatContentCursor.moveToNext()) {
                String from = chatContentCursor.getString(chatContentCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_MESSAGE_FROM));
                String message = chatContentCursor.getString(chatContentCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_MESSAGE));
                String status = chatContentCursor.getString(chatContentCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_MESSAGE_STATUS));
                String mode = chatContentCursor.getString(chatContentCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_MESSAGE_MODE));
                String date = chatContentCursor.getString(chatContentCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_MESSAGE_TIME_DATE));
                String time = chatContentCursor.getString(chatContentCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_MESSAGE_TIME_TIME));
                Message messageObject = new Message(message, from, date, time, status, Integer.valueOf(mode));
                messagingAdapater.add(messageObject);
            }
        }

        if(messagingAdapater.getCount() > 0) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    messagingAdapater.notifyDataSetChanged();
                }
            });
        }
    }

    private void deleteAllChat() {
        DbAdapter dbAdapter = new DbAdapter(getApplicationContext());
        if(dbAdapter.deleteAllMessage(friendId)) {
            System.out.println("Delete all message success");
            message.clear();

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    messagingAdapater.notifyDataSetChanged();
                }
            });
        }
        else {
            System.out.println("Delete all message failed");
        };
    }

    private void deleteChat(int position) {
        DbAdapter dbAdapter = new DbAdapter(getApplicationContext());
        System.out.println("Delete message id : " + message.get(position).id);
        if(dbAdapter.deleteMessage(message.get(position).id)) {
            System.out.println("Delete message success");
            message.remove(position);

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    messagingAdapater.notifyDataSetChanged();
                }
            });
        }
        else {
            System.out.println("Delete message failed");
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setNotification(int friendId, String friendName, String friendPhoneNumber, byte[] friendPublicKey ) {
        System.out.println("Set notification from messaging");
        Intent intent = new Intent(this, MessagingActivity.class);
        intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_USER_ID, friendId);
        intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_NAME, friendName);
        intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_PHONENUMBER, friendPhoneNumber);
        intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_PUBLICKEY, friendPublicKey);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(ChatActivity.class);
        taskStackBuilder.addNextIntent(intent);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("New Message")
                        .setContentText("New message from " + friendName);

        PendingIntent resultPendingIntent =
                taskStackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(101, mBuilder.build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("This is onresume in messaging");
        SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(getApplicationContext());
        sharedPrefsUtil.setToReceiveMode();

        onResumeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("Messaging Activity : Processing message to adapter/notification");
                //get data from intent
                String message = intent.getStringExtra("message");
                String date = intent.getStringExtra("date");
                String time = intent.getStringExtra("time");
                int id = intent.getIntExtra("id", 0);
                String phoneNumber = intent.getStringExtra("phonenumber");
                byte[] publicKey = intent.getByteArrayExtra("publickey");
                String name = intent.getStringExtra("name");
                long insertId = intent.getLongExtra("insertid", 0);

                if(String.valueOf(friendId).equals(String.valueOf(id))) {
                    final Message newMessage = new Message(message, friendName, date, time, "", 1);
                    newMessage.id = insertId;

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            messagingAdapater.add(newMessage);
                            messagingAdapater.notifyDataSetChanged();
                        }
                    });
                }
                else {
                    ChampNotification champNotification = new ChampNotification();
                    champNotification.setNotification(id, name, phoneNumber, publicKey, getApplicationContext());
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(onResumeReceiver, new IntentFilter("messagingactiv"));
        //Message messageObject = new Message(mText, userName, date, time, "SEND");
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("This is onpause in messaging");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onResumeReceiver);
        SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(getApplicationContext());
        sharedPrefsUtil.setToNotificationMode();

        DbAdapter dbAdapter = new DbAdapter(getApplicationContext());
        dbAdapter.deleteMessageWithNoHistory(friendId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("This is onstop in messaging");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onResumeReceiver);
        SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(getApplicationContext());
        sharedPrefsUtil.setToNotificationMode();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.out.println("this is back pressed");
    }
}

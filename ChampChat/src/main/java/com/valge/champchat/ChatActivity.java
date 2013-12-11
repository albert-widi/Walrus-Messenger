package com.valge.champchat;

import android.app.Activity;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

import com.valge.champchat.gcm_package.GCMBroadcastReceiver;
import com.valge.champchat.list_view_adapter.FriendMessageListAdapter;
import com.valge.champchat.util.ActivityLocationSharedPrefs;
import com.valge.champchat.util.DbAdapter;
import com.valge.champchat.util.EncryptionUtil;
import com.valge.champchat.util.FriendMessage;
import com.valge.champchat.util.IntentExtrasUtil;
import com.valge.champchat.util.SharedPrefsUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ChatActivity extends Activity {
    Context context;
    //db
    DbAdapter dbAdapter;

    //app variable
    private String userName;
    private byte[] privateKey;
    private String userPhoneNumber;

    //messages
    ArrayList<FriendMessage> messageArrayList = new ArrayList<FriendMessage>();
    ArrayList<FriendMessage> messageSearchList = new ArrayList<FriendMessage>();
    ArrayList<FriendMessage> tmpMessageList = new ArrayList<FriendMessage>();
    //message fragment
    ListView messageListView;
    FriendMessageListAdapter fmla;

    //broadCastReceiver
    BroadcastReceiver onPauseReceiver;
    BroadcastReceiver onResumeReceiver;
    BroadcastReceiver onStopReceiver;

    //GCMBroadcast
    GCMBroadcastReceiver gcmBroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        System.out.println("Creating chat activity");
        context = getApplicationContext();
        ActivityLocationSharedPrefs activityLocationSharedPrefs = new ActivityLocationSharedPrefs(context);
        activityLocationSharedPrefs.saveLastActivityToNonChat();

        //load shared prefs
        SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(context);
        sharedPrefsUtil.loadApplicationPrefs();
        if(!sharedPrefsUtil.appActivated) {
            Intent intent = new Intent(this, PhoneNumberRegistrationActivity.class);
            startActivity(intent);
            this.finish();
        }
        userName = sharedPrefsUtil.userName;
        privateKey = sharedPrefsUtil.privateKey;
        userPhoneNumber = sharedPrefsUtil.phoneNumber;

        context = this;
        dbAdapter = new DbAdapter(context);
        context = getApplicationContext();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.menu_new_message) {
            Intent intent = new Intent(this, FriendsActivity.class);
            intent.putExtra(IntentExtrasUtil.XTRAS_USER_NAME, userName);
            intent.putExtra(IntentExtrasUtil.XTRAS_USER_PRIVATE_KEY, privateKey);
            startActivity(intent);
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
            View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
            return rootView;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        System.out.println("On create post chat activity");
        loadMessageListFromDB();
    }

    private void loadMessageListFromDB() {
        new AsyncTask() {
            int position;

            int friendId;
            String phoneNumber;
            String friendName;
            String friendGcmId;
            byte[] friendPublicKey;
            @Override
            protected Object doInBackground(Object[] params) {

                Cursor friendMessageCursor = dbAdapter.getWhoMessage();
                if(friendMessageCursor.getCount() > 0) {
                    while(friendMessageCursor.moveToNext()) {
                        phoneNumber = friendMessageCursor.getString(friendMessageCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_MESSAGE_WITH));
                        //phoneNumber = dbAdapter.unescapeSqlString(phoneNumber);
                        Cursor friendDataCursor = dbAdapter.getFriendInfo(phoneNumber, "phonenumber");

                        if(friendDataCursor.getCount() > 0) {
                            friendDataCursor.moveToFirst();
                            friendId = friendDataCursor.getInt(friendDataCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_ID));
                            friendName = friendDataCursor.getString(friendDataCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_NAME));
                            friendGcmId = friendDataCursor.getString(friendDataCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_GCM_ID));
                            //friendGcmId = dbAdapter.unescapeSqlString(friendGcmId);
                            friendPublicKey = friendDataCursor.getBlob(friendDataCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_PUBLIC_KEY));
                        }
                        friendDataCursor.close();

                        FriendMessage friendMessage = new FriendMessage(friendId, friendName, phoneNumber, friendGcmId, friendPublicKey);

                        Cursor messageCursor = dbAdapter.getFriendLastMessage(phoneNumber);

                        if(messageCursor.getCount() > 0) {
                            messageCursor.moveToFirst();
                            String lastMessage = messageCursor.getString(messageCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_MESSAGE));
                            //lastMessage = dbAdapter.unescapeSqlString(lastMessage);
                            String messageDate = messageCursor.getString(messageCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_MESSAGE_TIME_DATE));
                            String messageTime = messageCursor.getString(messageCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_MESSAGE_TIME_TIMESTAMP));

                            if(lastMessage.length() > 35) {
                                lastMessage = lastMessage.substring(0, 32) + "...";
                            }
                            friendMessage.lastMessage = lastMessage;
                            friendMessage.lastMessageDate = messageDate;
                            friendMessage.lastMessageTime = messageTime;
                        }
                        messageCursor.close();
                        messageArrayList.add(friendMessage);
                    }
                    friendMessageCursor.close();
                }

                return "";
            }

            @Override
            protected void onPostExecute(Object o) {
                setLoadedMessage();
            }
        }.execute(null, null, null);
    }

    private void setLoadedMessage() {
        if(!messageArrayList.isEmpty()) {
            fmla = new FriendMessageListAdapter(context, messageArrayList);
            messageListView = (ListView) findViewById(R.id.friends_message_listview);
            messageListView.setAdapter(fmla);
        }
    }

    private void updateMessageList() {

    }

    private void processMessage(Context context, Intent intent, String condition) {
        final Context asyncContext = context;
        final String asyncCondition = condition;
        final Intent asyncIntent = intent;

        new AsyncTask() {
            Intent intent = asyncIntent;
            Context context = asyncContext;
            String condition = asyncCondition;

            @Override
            protected Object doInBackground(Object[] params) {
                System.out.println("Process Message: Processing message");
                GregorianCalendar gCalendar = new GregorianCalendar();
                EncryptionUtil encryptionUtil = new EncryptionUtil();

                String message = intent.getStringExtra("message");
                String messageKey = intent.getStringExtra("messageKey");
                String messageHash = intent.getStringExtra("messageHash");
                int friendId = intent.getIntExtra("friendId", 0);
                String phoneNumber = intent.getStringExtra("phoneNumber");
                String name = intent.getStringExtra("name");

                //debug
                System.out.println("Process Message : Intent extra debug");
                System.out.println("=====================================");
                System.out.println("Message:" + message);
                System.out.println("Message Key:" + messageKey);
                System.out.println("Message Hash:" + messageHash);
                System.out.println("Friend ID:" + friendId);
                System.out.println("Friend Phone:" + phoneNumber);
                System.out.println("Friend Name:" + name);
                System.out.println("=====================================");

                //date-time
                String date = gCalendar.get(Calendar.DATE) + "-" + gCalendar.get(Calendar.MONTH) + "-" + gCalendar.get(Calendar.YEAR) + " /";
                String time = gCalendar.get(Calendar.HOUR) + ":" + gCalendar.get(Calendar.MINUTE);

                String originalMessage = encryptionUtil.decryptMessage(message, messageKey, messageHash, context);
                System.out.println("Process Message: Original Message = " + originalMessage);

                if(condition.equalsIgnoreCase("onresume")) {
                    System.out.println("Processing on resume message");
                    //load friend information
                    String friendName;
                    String friendGcmId;
                    byte[] friendPublicKey;

                    boolean friendExists = false;
                    int messageListLength = messageArrayList.size();
                    int friendNumber = 0;
                    for(int i = 0; i < messageListLength; i++) {
                        if(messageArrayList.get(i).id == friendId) {
                            friendExists = true;
                            friendNumber = i;
                            break;
                        }
                    }

                    if(!friendExists) {
                        System.out.println("Processing on resume message : friend not exists");
                        Cursor friendDataCursor = dbAdapter.getFriendInfo(friendId);

                        if(friendDataCursor.getCount() > 0) {
                            friendDataCursor.moveToFirst();
                            friendName = friendDataCursor.getString(friendDataCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_NAME));
                            friendGcmId = friendDataCursor.getString(friendDataCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_GCM_ID));
                            //friendGcmId = dbAdapter.unescapeSqlString(friendGcmId);
                            friendPublicKey = friendDataCursor.getBlob(friendDataCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_PUBLIC_KEY));
                            //friendPublicKey = Base64.decode(friendPublicKeyString, Base64.DEFAULT);
                            FriendMessage friendMessage = new FriendMessage(friendId, friendName, phoneNumber, friendGcmId, friendPublicKey);
                            friendMessage.lastMessage = originalMessage;
                            friendMessage.lastMessageDate = date;
                            friendMessage.lastMessageTime = time;

                            //save message to db
                            messageArrayList.add(friendMessage);
                            friendDataCursor.close();
                            if(dbAdapter.saveMessage(friendId, phoneNumber, friendName, originalMessage, date, time)) {
                                System.out.println("Processing on resume message : Save message success");
                            }
                            else {
                                System.out.println("Processing on resume message : Save message failed");
                            }
                        }
                        else {
                            System.out.println("Processing on resume message : Friend not exists in database");
                            return "";
                        }
                    }
                    else {
                        messageArrayList.get(friendNumber).lastMessage = originalMessage;
                        messageArrayList.get(friendNumber).lastMessageDate = date;
                        messageArrayList.get(friendNumber).lastMessageTime = time;
                    }

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            fmla.notifyDataSetChanged();
                        }
                    });
                }
                else if(condition.equalsIgnoreCase("onpause")) {
                    System.out.println("Processing on pause message");
                    //set notification
                }
                else {
                    System.out.println("Processing on stop message");
                    ActivityLocationSharedPrefs activityLocationSharedPrefs = new ActivityLocationSharedPrefs(context);
                    if(activityLocationSharedPrefs.isChatActivityActive()) {
                        Intent messagingIntent = new Intent("messagingactiv");
                        intent.putExtra("name", name);
                        intent.putExtra("phonenumber", phoneNumber);
                        intent.putExtra("message", originalMessage);
                        intent.putExtra("date", date);
                        intent.putExtra("time", time);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(messagingIntent);
                    }
                    else {
                        //set notification

                    }
                }
                return "";
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
            }
        }.execute(null, null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("This is on resume on Chat activity");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onStopReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onPauseReceiver);
        onResumeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            //processMessage(context, intent, "onpause");
            System.out.println("Resume : " + intent.getStringExtra("message"));
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(onResumeReceiver, new IntentFilter("messageIntent"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("This is on pause on Chat activity");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onResumeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onStopReceiver);
        onPauseReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("Pause : " + intent.getStringExtra("message"));
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(onPauseReceiver, new IntentFilter("messageIntent"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("This is on stop on Chat activity");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onPauseReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onResumeReceiver);
        onStopReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("Stop : " + intent.getStringExtra("message"));

            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(onStopReceiver, new IntentFilter("messageIntent"));
    }
}

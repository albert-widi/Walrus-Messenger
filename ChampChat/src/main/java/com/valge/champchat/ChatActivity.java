package com.valge.champchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.valge.champchat.gcm_package.GCMBroadcastReceiver;
import com.valge.champchat.list_view_adapter.FriendMessageListAdapter;
import com.valge.champchat.util.ActivityLocationSharedPrefs;
import com.valge.champchat.util.ChampNotification;
import com.valge.champchat.util.DbAdapter;
import com.valge.champchat.util.FriendMessage;
import com.valge.champchat.util.IntentExtrasUtil;
import com.valge.champchat.util.SharedPrefsUtil;

import java.util.ArrayList;

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
    LocalBroadcastManager currentLocalBroadcastManager;

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
        sharedPrefsUtil.setToReceiveMode();
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
        this.setTitle("Chats");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat, menu);
        /*SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));*/

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
        else if(id == R.id.action_tester_mode) {
            Intent intent = new Intent(this, TesterActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.menu_new_message) {
            Intent intent = new Intent(this, FriendsActivity.class);
            intent.putExtra(IntentExtrasUtil.XTRAS_USER_NAME, userName);
            intent.putExtra(IntentExtrasUtil.XTRAS_USER_PRIVATE_KEY, privateKey);
            startActivity(intent);
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
            View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
            return rootView;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.chat_listview, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_delete_thread:
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle("Delete Chat Thread");

                adb
                .setMessage("Delete chat with " + messageArrayList.get(info.position).name)
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        deleteChatThread(info.position);
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.cancel();
                    }
                });
                adb.show();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        System.out.println("On create post chat activity");
    }

    private void deleteChatThread(int position) {
        DbAdapter dbAdapter = new DbAdapter(getApplicationContext());
        System.out.println("Delete thread id : " + messageArrayList.get(position).id);
        if(dbAdapter.deleteChatThread(messageArrayList.get(position).id)) {
            System.out.println("Delete thread success");
            messageArrayList.remove(position);

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    fmla.notifyDataSetChanged();
                }
            });
        }
        else {
            System.out.println("Delete thread failed");
        }
    }

    private void loadMessageListFromDB() {
        new AsyncTask() {
            int position;

            int friendId;
            String phoneNumber;
            String friendName;
            String friendGcmId;
            byte[] friendPublicKey;

            boolean newMessage = true;
            @Override
            protected Object doInBackground(Object[] params) {

                Cursor friendMessageCursor = dbAdapter.getChatThread();
                if(friendMessageCursor.getCount() > 0) {
                    while(friendMessageCursor.moveToNext()) {
                        friendId = friendMessageCursor.getInt(friendMessageCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_THREAD_ID));
                        Cursor friendDataCursor = dbAdapter.getFriendInfo(Integer.valueOf(friendId));

                        if(friendDataCursor.getCount() > 0) {
                            friendDataCursor.moveToFirst();
                            //friendId = friendDataCursor.getInt(friendDataCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_ID));
                            phoneNumber = friendDataCursor.getString(friendDataCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_PHONE_NUMBER));
                            friendName = friendDataCursor.getString(friendDataCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_NAME));
                            friendPublicKey = friendDataCursor.getBlob(friendDataCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_PUBLIC_KEY));
                        }
                        friendDataCursor.close();

                        int messageArrayListSize = messageArrayList.size();
                        boolean friendExists = false;
                        int friendNumber = 0;

                        if(messageArrayListSize > 0) {
                            for(int i = 0;i < messageArrayListSize; i++) {
                                if(messageArrayList.get(i).id == friendId) {
                                    friendExists = true;
                                    friendNumber = i;
                                    break;
                                }
                            }
                        }

                        FriendMessage friendMessage = null;
                        if(!friendExists) {
                            friendMessage = new FriendMessage(friendId, friendName, phoneNumber, friendPublicKey);
                        }

                        Cursor messageCursor = dbAdapter.getFriendLastMessage(String.valueOf(friendId));
                        String lastMessage = "";
                        String messageDate = "";
                        String messageTime = "";

                        if(messageCursor.getCount() > 0) {
                            messageCursor.moveToFirst();
                            lastMessage = messageCursor.getString(messageCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_MESSAGE));
                            messageDate = messageCursor.getString(messageCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_MESSAGE_TIME_DATE));
                            messageTime = messageCursor.getString(messageCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_MESSAGE_TIME_TIME));

                            if(lastMessage.length() > 25) {
                                lastMessage = lastMessage.substring(0, 22) + "...";
                            }

                            System.out.println("Last message : " + lastMessage);
                            System.out.println("Message date : " + messageDate);
                            System.out.println("Message time : " + messageTime);
                        }

                        //add or update friends
                        if(!friendExists) {
                            friendMessage.lastMessage = lastMessage;
                            friendMessage.lastMessageDate = messageDate;
                            friendMessage.lastMessageTime = messageTime;
                            messageArrayList.add(friendMessage);
                        }
                        else {
                            messageArrayList.get(friendNumber).lastMessage = lastMessage;
                            messageArrayList.get(friendNumber).lastMessageDate = messageDate;
                            messageArrayList.get(friendNumber).lastMessageTime = messageTime;
                        }

                        messageCursor.close();
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
        System.out.println("Set loaded message");
        fmla = new FriendMessageListAdapter(context, messageArrayList);
        messageListView = (ListView) findViewById(R.id.friends_message_listview);
        messageListView.setAdapter(fmla);
        registerForContextMenu(messageListView);

        messageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ChatActivity.this, MessagingActivity.class);
                intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_USER_ID, messageArrayList.get(position).id);
                intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_NAME, messageArrayList.get(position).name);
                intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_PHONENUMBER, messageArrayList.get(position).phoneNumber);
                intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_PUBLICKEY, messageArrayList.get(position).publicKey);
                startActivity(intent);
            }
        });
    }

    private void updateMessageList() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMessageListFromDB();
        System.out.println("This is on resume on Chat activity");
        SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(getApplicationContext());
        sharedPrefsUtil.setToReceiveMode();

        onResumeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("Receiving intent in onresume");
                System.out.println("Processing on resume message");

                String message = intent.getStringExtra("message");
                int friendId = intent.getIntExtra("id", 0);
                String friendName = intent.getStringExtra("name");
                byte[] friendPublicKey = intent.getByteArrayExtra("publickey");
                String friendPhoneNumber = intent.getStringExtra("phonenumber");
                String date = intent.getStringExtra("date");
                String time = intent.getStringExtra("time");

                if(message.length() > 25) {
                    message = message.substring(0, 22) + "...";
                }

                //load friend information
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

                    FriendMessage friendMessage = new FriendMessage(friendId, friendName, friendPhoneNumber, friendPublicKey);
                    friendMessage.lastMessage = message;
                    friendMessage.lastMessageDate = date;
                    friendMessage.lastMessageTime = time;

                    messageArrayList.add(friendMessage);
                }
                else {
                    messageArrayList.get(friendNumber).lastMessage = message;
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

                ChampNotification champNotification = new ChampNotification();
                champNotification.setNotification(friendId, friendName, friendPhoneNumber, friendPublicKey, getApplicationContext());
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(onResumeReceiver, new IntentFilter("messagingactiv"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("This is on pause on Chat activity");
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(onResumeReceiver);
        SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(getApplicationContext());
        sharedPrefsUtil.setToNotificationMode();
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("This is on stop on Chat activity");
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(onResumeReceiver);
    }
}

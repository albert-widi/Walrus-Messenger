package com.valge.champchat;

import android.app.Activity;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.valge.champchat.httppost.HttpPostModule;
import com.valge.champchat.list_view_adapter.FriendListAdapter;
import com.valge.champchat.util.DbAdapter;
import com.valge.champchat.util.Friend;
import com.valge.champchat.util.IntentExtrasUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class FriendsActivity extends Activity {
    Context context;
    //db
    DbAdapter dbAdapter;

    private String userName;
    private byte[] privateKey;

    //friends
    ArrayList<Friend> friendArrayList = new ArrayList<Friend>();
    ArrayList<Friend> friendSearchList = new ArrayList<Friend>();
    ArrayList<Friend> tmpFriendList = new ArrayList<Friend>();
    ArrayList<Friend> tmpRefreshFriendList = new ArrayList<Friend>();
    //friends fragment
    ListView friendListView;
    FriendListAdapter fla;
    private boolean haveFriends = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        context = this;
        dbAdapter = new DbAdapter(context);

        Intent intent = getIntent();
        userName = intent.getExtras().getString(IntentExtrasUtil.XTRAS_USER_NAME, "");
        privateKey = intent.getExtras().getByteArray(IntentExtrasUtil.XTRAS_USER_PRIVATE_KEY);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.friends, menu);
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
            return true;
        }
        else if(id == R.id.menu_refresh) {
            refreshFriendList();
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
            View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
            return rootView;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        loadFriendListFromDB();
    }

    //------------------------------------------------------ REFRESH FRIEND LIST --------------------------------------------------------------------
    private void refreshFriendList() {
        new AsyncTask() {
            String stringResponse = "";
            JSONObject jsonResponse;

            @Override
            protected Object doInBackground(Object[] params) {
                //Toast.makeText(MainActivity.this, "Refreshing friend list", Toast.LENGTH_SHORT).show();
                String friendList = "";

                friendList = getPhoneNumberFromContacts();

                try {
                    String postAction = "getFriendList";
                    String[] postData = {friendList};
                    String[] postDataName = {"friendlist"};

                    HttpPostModule httpPostModule = new HttpPostModule();
                    jsonResponse = httpPostModule.echatHttpPost(postAction, postData, postDataName);

                    if(jsonResponse.getString("message").equalsIgnoreCase("FRIEND_SEARCH_SUCCESS")) {
                        if(updateRefreshedFriendListView(jsonResponse)) {

                        }
                        else {

                        }
                    }
                    else {
                        System.out.println("GAGAL");
                    }
                }
                catch(Exception e) {

                }

                return "";
            }

            protected void onPostExecute(Object result) {
                /*if(!haveFriends) {
                    getLoadedFriendList();
                }*/
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

    public boolean updateRefreshedFriendListView(JSONObject jsonData) {
        JSONObject jsonResponse = jsonData;

        Iterator<?> keys = jsonResponse.keys();
        //skipping non friends data keys
        try {
            while(keys.hasNext()) {
                boolean friendExists = false;
                boolean friendNeedUpdate = false;
                int updateFriendNumber;

                String key = (String) keys.next();
                System.out.println("Key : " + key);
                if(!key.equalsIgnoreCase("MESSAGE") && !key.equalsIgnoreCase("ERROR")) {
                    String nonSplitedData = jsonResponse.get(key).toString();
                    String[] splitedData = nonSplitedData.split(";");

                    int id = Integer.parseInt(splitedData[0]);
                    String name = splitedData[1];
                    String phoneNumber = key;
                    String gcmId = splitedData[2];

                    byte[] decodedKey = Base64.decode(splitedData[3], Base64.DEFAULT);
                    //PublicKey tmpPublicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decodedKey));

                    //-------------------------------------- PARTIAL LIST VIEW UPDATE ----------------------------------------------------------
                    int count = friendArrayList.size();
                    for(int i = 0; i < count; i++) {
                        //debug
                        System.out.println("Debug: friend phone number : " + friendArrayList.get(i).phoneNumber + " and " + phoneNumber);

                        if(friendArrayList.get(i).phoneNumber.equalsIgnoreCase(phoneNumber)) {
                            System.out.println("Refresh friend list: Friend exists in database");
                            //debug
                            if(friendArrayList.get(i).gcmId.equalsIgnoreCase(gcmId)) {
                                System.out.println("Refresh friend list: GCM match");
                            }
                            else {
                                System.out.println("Refresh friend list: GCM not match");
                            }
                            if(Arrays.equals(friendArrayList.get(i).publicKey, decodedKey)) {
                                System.out.println("Refresh friend list: PublicKey match");
                            }
                            else {
                                System.out.println("Refresh friend list: PublicKey not match");
                            }

                            //friend already exists
                            if(friendArrayList.get(i).gcmId.equalsIgnoreCase(gcmId) && Arrays.equals(friendArrayList.get(i).publicKey, decodedKey)) {
                                System.out.println("Refresh friend list: Friend need no update");
                                friendExists = true;
                            }
                            //friend update
                            else {
                                System.out.println("Refresh friend list: Friend need update");
                                friendNeedUpdate = true;
                                friendArrayList.get(i).name = name;
                                friendArrayList.get(i).gcmId = gcmId;
                                friendArrayList.get(i).publicKey = decodedKey;

                                if(dbAdapter.updateFriend(name, phoneNumber, gcmId, decodedKey)) {
                                    System.out.println("Refresh friend list: Friend update success");
                                }
                                else {
                                    System.out.println("Refresh friend list: Friend update failed");
                                }
                            }
                            break;
                        }
                    }

                    if(!friendExists) {
                        System.out.println("Refresh friend list: Add new friend");
                        Friend friend = new Friend(id, name, phoneNumber, gcmId, decodedKey);
                        friendArrayList.add(friend);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                fla.notifyDataSetChanged();
                            }
                        });

                        //saving friend to db
                        if(!dbAdapter.saveFriend(id, name, phoneNumber, gcmId, decodedKey)) {
                            System.out.println("Refresh friend list: Save friend to db failed, name :" + name);
                        }
                        else {
                            System.out.println("Refresh friend list : Save to db success");
                        }
                    }
                    //---------------------------------------------------------------------------------------------------------------------------
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    //-------------------------------------------------------------------------------------------------------------------------

    public void loadFriendListFromDB() {
        System.out.println("Loading friend list from database");
        try {
            if(friendArrayList.size() > 0) {
                friendArrayList.clear();
            }

            Cursor cursor = dbAdapter.getFriends();
            //if friends data exists
            if(cursor.getCount() > 0) {
                System.out.println("Friends Exists");
                haveFriends = true;
                if(cursor.moveToFirst()) {
                    do {
                        int id = cursor.getInt(cursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_ID));
                        String name = cursor.getString(cursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_NAME));
                        System.out.println("Name : " + name);
                        String phoneNumber = cursor.getString(cursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_PHONE_NUMBER));
                        //phoneNumber = dbAdapter.unescapeSqlString(phoneNumber);
                        String gcmId = cursor.getString(cursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_GCM_ID));
                        //gcmId = dbAdapter.unescapeSqlString(gcmId);
                        byte[] key = cursor.getBlob(cursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_PUBLIC_KEY));

                        Friend friend = new Friend(id, name, phoneNumber, gcmId, key);
                        friendArrayList.add(friend);
                    }while(cursor.moveToNext());
                }
                else {
                    System.out.println("Keluar");
                }
            }
            else {
                System.out.println("Friends Not Exists");
            }
            cursor.close();
            getLoadedFriendList();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void getLoadedFriendList() {
        System.out.println("Loading loaded friends");
        fla = new FriendListAdapter(getApplicationContext(), friendArrayList);
        friendListView = (ListView) findViewById(R.id.friends_listview);
        friendListView.setAdapter(fla);

        //debug
        int count = friendArrayList.size();
        for(int i = 0; i < count; i++) {
            System.out.println("Friend name : " + friendArrayList.get(i).name);
        }

        friendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(context, MessagingActivity.class);
                intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_USER_ID, friendArrayList.get(position).id);
                intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_NAME, friendArrayList.get(position).name);
                intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_PHONENUMBER, friendArrayList.get(position).phoneNumber);
                intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_GCMID, friendArrayList.get(position).gcmId);
                intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_PUBLICKEY, friendArrayList.get(position).publicKey);
                intent.putExtra(IntentExtrasUtil.XTRAS_USER_NAME, userName);
                intent.putExtra(IntentExtrasUtil.XTRAS_USER_PRIVATE_KEY, privateKey);

                startActivity(intent);
            }

        });
    }

    public void updateFriendListView(String param) {
        if(!param.equalsIgnoreCase("")) {
            int friendsLength = friendArrayList.size();

            if(friendsLength > 0) {
                for(int i = 0; i < friendsLength; i++) {
                    String friendName = friendArrayList.get(i).toString().toLowerCase();
                    if(friendName.startsWith(param)) {
                        System.out.println("Friend found");
                        friendSearchList.add(friendArrayList.get(i));
                    }
                    else {
                        System.out.println("Friend not found");
                    }
                }
            }

            if(friendSearchList.size() > 0) {
                tmpFriendList = friendArrayList;
                friendArrayList.clear();
                friendArrayList = friendSearchList;

                System.out.println("Friend :" + friendSearchList.get(0).name);
            }
            else {

            }
        }
        else {
            if(!friendSearchList.isEmpty()) {
                friendSearchList.clear();
            }
            friendArrayList = tmpFriendList;
            tmpFriendList.clear();
        }

        fla.notifyDataSetChanged();
    }
    //------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("This is on resume on Friends activity");
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("This is on pause on Friends activity");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("This is on stop on Friends activity");
    }
}

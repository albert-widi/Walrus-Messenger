package com.valge.champchat.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

/**
 * Created by Albert Widiatmoko on 07/12/13.
 */
public class DbAdapter {
    private SQLiteDatabase db;
    private DbHelper dbHelper;

    Context appContext;

    public DbAdapter(Context context) {
        System.out.println("Db adapter");
        dbHelper = new DbHelper(context);
        appContext = context;
    }

    public void openConnection() {
        if(db != null) {
            if(db.isOpen()) {
                return;
            }
        }

        db = dbHelper.getWritableDatabase();
    }

    public void finalize() throws Throwable{
        super.finalize();
        closeConnection();
    }

    public void closeConnection() {
        try {
            db.close();
        }
        catch(Exception e) {

        }

    }

    public boolean registerUser(int userId, String phoneNumber, String userName, String gcmId, String secretKey, byte[] privateKey) {
        openConnection();
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_USER_ID, userId);
        values.put(DbHelper.COLUMN_USER_NAME, userName);
        values.put(DbHelper.COLUMN_USER_PHONE_NUMBER, phoneNumber);
        values.put(DbHelper.COLUMN_USER_GCM_ID, gcmId);
        //values.put(DbHelper.COLUMN_USER_PHONE_NUMBER, DatabaseUtils.sqlEscapeString(phoneNumber));
        //values.put(DbHelper.COLUMN_USER_GCM_ID, DatabaseUtils.sqlEscapeString(gcmId));
        values.put(DbHelper.COLUMN_SECRET_PASS, secretKey);
        values.put(DbHelper.COLUMN_PRIVATE_KEY, privateKey);

        long id = db.insert(DbHelper.TABLE_USERDAT, null, values);
        closeConnection();

        if(id == -1) {
            return false;
        }

        return true;
    }

    public boolean updatePrivateKey(int userId, byte[] privateKey) {
        openConnection();
        ContentValues values = new ContentValues();
        String[] selectionArg = {String.valueOf(userId)};

        values.put(DbHelper.COLUMN_PRIVATE_KEY, privateKey);

        long id = db.update(DbHelper.TABLE_USERDAT,
                values,
                DbHelper.COLUMN_USER_ID + " = ?",
                selectionArg);

        if(id == -1) {
            closeConnection();
            return false;
        }
        closeConnection();
        return true;
    }

    public void deleteRegisteredUser(String phoneNumber) {
        openConnection();
        db.delete(DbHelper.TABLE_USERDAT, DbHelper.COLUMN_USER_PHONE_NUMBER + " = " + phoneNumber, null);
        return;
    }

    public boolean saveFriend(int friendId, String name, String phoneNumber, byte[] publicKey) {
        openConnection();

        //check if friend already exists
        String[] columns = {DbHelper.COLUMN_FRIEND_PHONE_NUMBER};
        String[] selectionArg = {phoneNumber};
        //String[] selectionArg = {DatabaseUtils.sqlEscapeString(phoneNumber)};
        Cursor cursor = db.query(DbHelper.TABLE_FRIEND_LIST,
                columns,
                DbHelper.COLUMN_FRIEND_ID + " = ?",
                selectionArg, null, null, null);

        if(cursor.getCount() > 0) {
            ContentValues values = new ContentValues();
            values.put(DbHelper.COLUMN_FRIEND_NAME, name);
            //values.put(DbHelper.COLUMN_FRIEND_GCM_ID, DatabaseUtils.sqlEscapeString(gcmId));
            values.put(DbHelper.COLUMN_FRIEND_PUBLIC_KEY, publicKey);

            long id = db.update(DbHelper.TABLE_FRIEND_LIST,
                    values,
                    DbHelper.COLUMN_FRIEND_ID + " = ?",
                    selectionArg);

            if(id == -1) {
                cursor.close();
                closeConnection();
                return false;
            }
        }
        else {
            ContentValues values = new ContentValues();
            values.put(DbHelper.COLUMN_FRIEND_ID, friendId);
            values.put(DbHelper.COLUMN_FRIEND_NAME, name);
            values.put(DbHelper.COLUMN_FRIEND_PHONE_NUMBER, phoneNumber);
            values.put(DbHelper.COLUMN_FRIEND_PUBLIC_KEY, publicKey);

            long id = db.insert(DbHelper.TABLE_FRIEND_LIST, null, values);

            if(id == -1) {
                cursor.close();
                closeConnection();
                return false;
            }
        }
        cursor.close();
        closeConnection();
        return true;
    }

    public boolean updateFriend(String name, String phoneNumber, byte[] publicKey) {
        openConnection();
        ContentValues values = new ContentValues();
        String[] selectionArg = {phoneNumber};
        //String[] selectionArg = {DatabaseUtils.sqlEscapeString(phoneNumber)};
        values.put(DbHelper.COLUMN_FRIEND_NAME, name);
        //values.put(DbHelper.COLUMN_FRIEND_GCM_ID, DatabaseUtils.sqlEscapeString(gcmId));
        values.put(DbHelper.COLUMN_FRIEND_PUBLIC_KEY, publicKey);

        long id = db.update(DbHelper.TABLE_FRIEND_LIST,
                values,
                DbHelper.COLUMN_FRIEND_PHONE_NUMBER + " = ?",
                selectionArg);

        if(id == -1) {
            closeConnection();
            return false;
        }
        closeConnection();
        return true;
    }

    public Cursor getFriends() {
        openConnection();
        String query = "SELECT * FROM " + DbHelper.TABLE_FRIEND_LIST;
        Cursor cursor = db.rawQuery(query, null);
        //closeConnection();
        return cursor;
    }

    public Cursor getFriendInfo(String filter, String filterSelection) {
        openConnection();
        String[] columns = {DbHelper.COLUMN_FRIEND_NAME,
                DbHelper.COLUMN_FRIEND_PUBLIC_KEY,};
        String[] selectionArg = {filter};
        //String[] selectionArg = {DatabaseUtils.sqlEscapeString(filter)};

        Cursor cursor = null;
        if(filterSelection.equalsIgnoreCase("phonenumber")) {
            cursor = db.query(DbHelper.TABLE_FRIEND_LIST,
                    columns,
                    DbHelper.COLUMN_FRIEND_PHONE_NUMBER + " = ?",
                    selectionArg, null, null, DbHelper.COLUMN_ID);
        }
        else if(filterSelection.equalsIgnoreCase("friendid")) {
            cursor = db.query(DbHelper.TABLE_MESSAGE_HISTORY,
                    columns,
                    DbHelper.COLUMN_FRIEND_ID + " = ?",
                    selectionArg, null, null, DbHelper.COLUMN_ID);
        }
        return cursor;
    }

    public Cursor getFriendInfo(int friendId) {
        openConnection();
        String[] columns = {DbHelper.COLUMN_FRIEND_NAME,
                DbHelper.COLUMN_FRIEND_PHONE_NUMBER,
                DbHelper.COLUMN_FRIEND_PUBLIC_KEY,};
        String[] selectionArg = {String.valueOf(friendId)};
        //String[] selectionArg = {DatabaseUtils.sqlEscapeString(filter)};

        Cursor cursor;
        cursor = db.query(DbHelper.TABLE_FRIEND_LIST,
                columns,
                DbHelper.COLUMN_FRIEND_ID + " = ?",
                selectionArg, null, null, DbHelper.COLUMN_ID);

        return cursor;
    }

    //message
    public long saveMessage(int friendId, String phoneNumber, String friendName, String message, String date, String time, String status, String mode, int historyMode) {
        openConnection();
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_MESSAGE_WITH_ID, friendId);
        values.put(DbHelper.COLUMN_MESSAGE_WITH, phoneNumber);
        values.put(DbHelper.COLUMN_MESSAGE_FROM, friendName);
        values.put(DbHelper.COLUMN_MESSAGE, message);
        values.put(DbHelper.COLUMN_MESSAGE_STATUS, status);
        values.put(DbHelper.COLUMN_MESSAGE_TIME_DATE, date);
        values.put(DbHelper.COLUMN_MESSAGE_TIME_TIME, time);
        values.put(DbHelper.COLUMN_MESSAGE_MODE, mode);
        values.put(DbHelper.COLUMN_MESSAGE_HISTORYMODE, historyMode);
        long id = db.insert(DbHelper.TABLE_MESSAGE_HISTORY, null, values);
        closeConnection();

        return id;
    }

    public boolean updateMessage(long insertId, String status) {
        openConnection();
        ContentValues values = new ContentValues();
        String[] selectionArg = {String.valueOf(insertId)};

        values.put(DbHelper.COLUMN_MESSAGE_STATUS, status);

        long id = db.update(DbHelper.TABLE_MESSAGE_HISTORY,
                values,
                DbHelper.COLUMN_ID + " = ?",
                selectionArg);

        if(id == -1) {
            closeConnection();
            return false;
        }
        closeConnection();
        return true;
    }

    public Cursor getAllMessage() {
        openConnection();
        String[] columns = {DbHelper.COLUMN_MESSAGE,
                DbHelper.COLUMN_MESSAGE_FROM,
                DbHelper.COLUMN_MESSAGE_WITH_ID,
                DbHelper.COLUMN_MESSAGE_TIME_DATE,
                DbHelper.COLUMN_MESSAGE_TIME_TIME,
                DbHelper.COLUMN_MESSAGE_TIME_TIMESTAMP};

        String query = "SELECT * FROM " + DbHelper.TABLE_MESSAGE_HISTORY;
        Cursor cursor = db.rawQuery(query, null);

        return cursor;
    }

    public Cursor getWhoMessage() {
        openConnection();
        String[] columns = {DbHelper.COLUMN_MESSAGE_WITH_ID};

        Cursor cursor = db.query(DbHelper.TABLE_MESSAGE_HISTORY,
                columns,
                null,
                null,
                DbHelper.COLUMN_MESSAGE_WITH, null, null);

        return cursor;
    }

    public Cursor getMessage(String friendId) {
        openConnection();
        String[] columns = {DbHelper.COLUMN_MESSAGE,
                DbHelper.COLUMN_ID,
                DbHelper.COLUMN_MESSAGE_FROM,
                DbHelper.COLUMN_MESSAGE_STATUS,
                DbHelper.COLUMN_MESSAGE_MODE,
                DbHelper.COLUMN_MESSAGE_TIME_DATE,
                DbHelper.COLUMN_MESSAGE_TIME_TIME,
                DbHelper.COLUMN_MESSAGE_TIME_TIMESTAMP};
        String[] selectionArg = {friendId};

        Cursor cursor = db.query(DbHelper.TABLE_MESSAGE_HISTORY,
                columns,
                DbHelper.COLUMN_MESSAGE_WITH_ID + " = ?",
                selectionArg, null, null, DbHelper.COLUMN_ID);

        return cursor;
    }

    public Cursor getFriendLastMessage(String friendId) {
        openConnection();
        String[] columns = {DbHelper.COLUMN_MESSAGE,
                DbHelper.COLUMN_MESSAGE_FROM,
                DbHelper.COLUMN_MESSAGE_TIME_DATE,
                DbHelper.COLUMN_MESSAGE_TIME_TIME,
                DbHelper.COLUMN_MESSAGE_TIME_TIMESTAMP};

        String[] selectionArg = {friendId};

        Cursor cursor = db.query(DbHelper.TABLE_MESSAGE_HISTORY,
                columns,
                DbHelper.COLUMN_MESSAGE_WITH_ID + " = ?",
                selectionArg, null, null, DbHelper.COLUMN_ID + " DESC", "1");

        return cursor;
    }

    public boolean deleteMessage(long id) {
        if(id == 0) {
            return true;
        }

        openConnection();
        long deleteId = db.delete(DbHelper.TABLE_MESSAGE_HISTORY, DbHelper.COLUMN_ID + " = " + id, null);
        closeConnection();

        if(deleteId == -1) {
            return false;
        }

        return true;
    }

    public boolean deleteAllMessage(int friendId) {
        openConnection();
        long deleteId = db.delete(DbHelper.TABLE_MESSAGE_HISTORY, DbHelper.COLUMN_MESSAGE_WITH_ID + " = " + friendId, null);
        closeConnection();

        if(deleteId == -1) {
            return false;
        }

        return true;
    }

    public boolean deleteMessageWithNoHistory(int friendId) {
        openConnection();
        String[] selectionArg = {String.valueOf(friendId), "0"};
        long deleteId = db.delete(DbHelper.TABLE_MESSAGE_HISTORY, DbHelper.COLUMN_MESSAGE_WITH_ID + " = ? AND " + DbHelper.COLUMN_MESSAGE_HISTORYMODE + " = ?", selectionArg);
        closeConnection();

        if(deleteId == -1) {
            System.out.println("Cannot delete message history");
            return false;
        }

        return true;
    }

    public void saveChatThread(int friendId) {
        openConnection();
        //check
        String[] columns = {DbHelper.COLUMN_FRIEND_THREAD_ID};
        String[] selectionArg = {String.valueOf(friendId)};
        //String[] selectionArg = {DatabaseUtils.sqlEscapeString(filter)};

        Cursor cursor;
        cursor = db.query(DbHelper.TABLE_CHAT_THREAD,
                columns,
                DbHelper.COLUMN_FRIEND_THREAD_ID + " = ?",
                selectionArg, null, null, DbHelper.COLUMN_ID);

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            int id = cursor.getInt(cursor.getColumnIndex(DbHelper.COLUMN_FRIEND_THREAD_ID));
            String[] updateArg = {String.valueOf(id)};
            ContentValues values = new ContentValues();

            values.put(DbHelper.COLUMN_TIMESTAMP_THREAD, " time('now') ");

            long updateId = db.update(DbHelper.TABLE_CHAT_THREAD,
                            values,
                            DbHelper.COLUMN_FRIEND_THREAD_ID + " = ?",
                            updateArg);
            cursor.close();
            return;
        }

        //save
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_FRIEND_THREAD_ID, friendId);
        long id = db.insert(DbHelper.TABLE_CHAT_THREAD, null, values);
        cursor.close();
        closeConnection();
    }

    public Cursor getChatThread() {
        openConnection();
        String[] columns = {DbHelper.COLUMN_FRIEND_THREAD_ID};

        Cursor cursor = db.query(DbHelper.TABLE_CHAT_THREAD,
                columns,
                null,
                null, null, null, DbHelper.COLUMN_TIMESTAMP_THREAD + " DESC", null);

        return cursor;
    }

    public boolean deleteChatThread(int friendId) {
        openConnection();
        long deleteIdThread = db.delete(DbHelper.TABLE_CHAT_THREAD, DbHelper.COLUMN_FRIEND_THREAD_ID + " = " + friendId, null);
        long deleteIdChat = db.delete(DbHelper.TABLE_MESSAGE_HISTORY, DbHelper.COLUMN_MESSAGE_WITH_ID + " = " + friendId, null);
        closeConnection();

        if(deleteIdThread == -1 || deleteIdChat == -1) {
            return false;
        }

        return true;
    }

    public boolean databaseExists() {
        File database = appContext.getDatabasePath(DbHelper.DATABASE_NAME);
        return database.exists();
    }

    public String unescapeSqlString(String escapedString) {
        return escapedString.substring(1, escapedString.length()-1);
    }

    //dbhelper class
    public class DbHelper extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 3;
        private static final String DATABASE_NAME = "EAndroidIM.db";

        public static final String COLUMN_ID = "_id";
        //table user data
        public static final String TABLE_USERDAT = "userdat";
        public static final String COLUMN_USER_ID = "userid";
        public static final String COLUMN_USER_PHONE_NUMBER = "userphonenum";
        public static final String COLUMN_USER_NAME = "username";
        public static final String COLUMN_USER_GCM_ID = "usergcmid";
        public static final String COLUMN_SECRET_PASS = "usersecpass";
        public static final String COLUMN_PRIVATE_KEY = "privatekey";
        //table friend list
        public static final String TABLE_FRIEND_LIST = "friendlist";
        public static final String COLUMN_FRIEND_ID = "friendid";
        public static final String COLUMN_FRIEND_NAME = "friendname";
        public static final String COLUMN_FRIEND_PHONE_NUMBER = "fphonenum";
        public static final String COLUMN_FRIEND_PUBLIC_KEY = "fpublic";
        //table message history
        public static final String TABLE_MESSAGE_HISTORY = "msghistory";
        public static final String COLUMN_MESSAGE_WITH_ID = "chatwithid";
        public static final String COLUMN_MESSAGE_WITH = "chatwith";
        public static final String COLUMN_MESSAGE_FROM = "msgfrom";
        public static final String COLUMN_MESSAGE = "msgstring";
        public static final String COLUMN_MESSAGE_MODE = "msgmode";
        public static final String COLUMN_MESSAGE_TIME_DATE = "msgtimedate";
        public static final String COLUMN_MESSAGE_TIME_TIME = "msgtimetime";
        public static final String COLUMN_MESSAGE_TIME_TIMESTAMP = "msgtimestamp";
        public static final String COLUMN_MESSAGE_STATUS = "msgstatus";
        public static final String COLUMN_MESSAGE_HISTORYMODE = "msghistorymode";
        //table block
        public static final String TABLE_ID_BLOCK = "idblock";
        public static final String COLUMN_BLOCKED_ID = "blockedid";
        //table available chat thread
        public static final String TABLE_CHAT_THREAD = "chatthread";
        public static final String COLUMN_FRIEND_THREAD_ID = "friendthreadid";
        public static final String COLUMN_TIMESTAMP_THREAD = "timestampthread";

        //creating table
        public static final String TABLE_CREATE_USERDAT = "CREATE TABLE " + TABLE_USERDAT + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " NUMBER NOT NULL, " +
                COLUMN_USER_PHONE_NUMBER + " TEXT NOT NULL, " +
                COLUMN_USER_NAME + " TEXT NOT NULL, " +
                COLUMN_USER_GCM_ID + " TEXT NOT NULL, " +
                COLUMN_SECRET_PASS + " TEXT NOT NULL, " +
                COLUMN_PRIVATE_KEY + " BLOB NOT NULL);";

        public static final String TABLE_CREATE_FRIEND_LIST = "CREATE TABLE " + TABLE_FRIEND_LIST + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FRIEND_ID + " NUMBER NOT NULL, " +
                COLUMN_FRIEND_PHONE_NUMBER + " TEXT NOT NULL, " +
                COLUMN_FRIEND_NAME + " TEXT NOT NULL, " +
                COLUMN_FRIEND_PUBLIC_KEY + " BLOB NOT NULL);";

        public static final String TABLE_CREATE_MESSAGE_HISTORY = "CREATE TABLE " + TABLE_MESSAGE_HISTORY + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MESSAGE_WITH_ID + " NUMBER NOT NULL, " +
                COLUMN_MESSAGE_WITH + " TEXT NOT NULL, " +
                COLUMN_MESSAGE_FROM + " TEXT NOT NULL, " +
                COLUMN_MESSAGE + " TEXT NOT NULL, " +
                COLUMN_MESSAGE_STATUS + " TEXT, " +
                COLUMN_MESSAGE_MODE + " NUMBER NOT NULL, " +
                COLUMN_MESSAGE_HISTORYMODE + " TINYINT NOT NULL, " +
                COLUMN_MESSAGE_TIME_DATE + " TEXT NOT NULL, " +
                COLUMN_MESSAGE_TIME_TIME + " TEXT NOT NULL, " +
                COLUMN_MESSAGE_TIME_TIMESTAMP + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);";

        public static final String TABLE_CREATE_ID_BLOCK = "CREATE TABLE " + TABLE_ID_BLOCK + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_BLOCKED_ID + " NUMBER NOT NULL);";

        public static final String TABLE_CREATE_CHAT_THREAD = "CREATE TABLE " + TABLE_CHAT_THREAD + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FRIEND_THREAD_ID + " NUMBER NOT NULL, " +
                COLUMN_TIMESTAMP_THREAD + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);";

        private static final String TRIGGER_CREATE_UPDATE_THREAD_TIMESTAMP =
                "CREATE TRIGGER update_time_trigger" +
                        "  AFTER UPDATE ON " + TABLE_CHAT_THREAD + " FOR EACH ROW" +
                        "  BEGIN " +
                        "UPDATE " + TABLE_CHAT_THREAD +
                        "  SET " + COLUMN_TIMESTAMP_THREAD + " = current_timestamp" +
                        "  WHERE " + COLUMN_ID + " = old." + COLUMN_ID + ";" +
                        "  END";


        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            System.out.println("Db helper");
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            System.out.println("Creating table");
            try {
                sqLiteDatabase.execSQL(TABLE_CREATE_USERDAT);
                sqLiteDatabase.execSQL(TABLE_CREATE_FRIEND_LIST);
                sqLiteDatabase.execSQL(TABLE_CREATE_MESSAGE_HISTORY);
                sqLiteDatabase.execSQL(TABLE_CREATE_ID_BLOCK);
                sqLiteDatabase.execSQL(TABLE_CREATE_CHAT_THREAD);
                sqLiteDatabase.execSQL(TRIGGER_CREATE_UPDATE_THREAD_TIMESTAMP);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            System.out.println("Upgrading database");
            Log.w(DbHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion +
                    " all data will be deleted");
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_USERDAT);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIEND_LIST);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE_HISTORY);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ID_BLOCK);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_THREAD);
            onCreate(sqLiteDatabase);
        }
    }
}

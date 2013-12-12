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

    public void closeConnection() {
        db.close();
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

    public void deleteRegisteredUser(String phoneNumber) {
        openConnection();
        db.delete(DbHelper.TABLE_USERDAT, DbHelper.COLUMN_USER_PHONE_NUMBER + " = " + phoneNumber, null);
        return;
    }

    public boolean saveFriend(int friendId, String name, String phoneNumber, String gcmId, byte[] publicKey) {
        openConnection();

        //check if friend already exists
        String[] columns = {DbHelper.COLUMN_FRIEND_PHONE_NUMBER};
        String[] selectionArg = {phoneNumber};
        //String[] selectionArg = {DatabaseUtils.sqlEscapeString(phoneNumber)};
        Cursor cursor = db.query(DbHelper.TABLE_FRIEND_LIST,
                columns,
                DbHelper.COLUMN_FRIEND_PHONE_NUMBER + " = ?",
                selectionArg, null, null, null);

        if(cursor.getCount() > 0) {
            ContentValues values = new ContentValues();
            values.put(DbHelper.COLUMN_FRIEND_NAME, name);
            values.put(DbHelper.COLUMN_FRIEND_GCM_ID, gcmId);
            //values.put(DbHelper.COLUMN_FRIEND_GCM_ID, DatabaseUtils.sqlEscapeString(gcmId));
            values.put(DbHelper.COLUMN_FRIEND_PUBLIC_KEY, publicKey);

            long id = db.update(DbHelper.TABLE_FRIEND_LIST,
                    values,
                    DbHelper.COLUMN_FRIEND_PHONE_NUMBER + " = ?",
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
            values.put(DbHelper.COLUMN_FRIEND_GCM_ID, gcmId);
            //values.put(DbHelper.COLUMN_FRIEND_PHONE_NUMBER, DatabaseUtils.sqlEscapeString(phoneNumber));
            //values.put(DbHelper.COLUMN_FRIEND_GCM_ID, DatabaseUtils.sqlEscapeString(gcmId));
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

    public boolean updateFriend(String name, String phoneNumber, String gcmId, byte[] publicKey) {
        openConnection();
        ContentValues values = new ContentValues();
        String[] selectionArg = {phoneNumber};
        //String[] selectionArg = {DatabaseUtils.sqlEscapeString(phoneNumber)};
        values.put(DbHelper.COLUMN_FRIEND_NAME, name);
        values.put(DbHelper.COLUMN_FRIEND_GCM_ID, gcmId);
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
                DbHelper.COLUMN_FRIEND_GCM_ID,
                DbHelper.COLUMN_FRIEND_PUBLIC_KEY,};
        String[] selectionArg = {filter};
        //String[] selectionArg = {DatabaseUtils.sqlEscapeString(filter)};

        Cursor cursor;
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
        else {
            cursor = db.query(DbHelper.TABLE_FRIEND_LIST,
                    columns,
                    DbHelper.COLUMN_FRIEND_GCM_ID + " = ?",
                    selectionArg, null, null, DbHelper.COLUMN_ID);
        }
        return cursor;
    }

    public Cursor getFriendInfo(int friendId) {
        openConnection();
        String[] columns = {DbHelper.COLUMN_FRIEND_NAME,
                DbHelper.COLUMN_FRIEND_PHONE_NUMBER,
                DbHelper.COLUMN_FRIEND_GCM_ID,
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
    public long saveMessage(int friendId, String phoneNumber, String friendName, String message, String date, String time, String status, String mode) {
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
        public static final String COLUMN_FRIEND_GCM_ID = "fgcmid";
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
                COLUMN_FRIEND_GCM_ID + " TEXT NOT NULL, " +
                COLUMN_FRIEND_PUBLIC_KEY + " BLOB NOT NULL);";

        public static final String TABLE_CREATE_MESSAGE_HISTORY = "CREATE TABLE " + TABLE_MESSAGE_HISTORY + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MESSAGE_WITH_ID + " NUMBER NOT NULL, " +
                COLUMN_MESSAGE_WITH + " TEXT NOT NULL, " +
                COLUMN_MESSAGE_FROM + " TEXT NOT NULL, " +
                COLUMN_MESSAGE + " TEXT NOT NULL, " +
                COLUMN_MESSAGE_STATUS + " TEXT, " +
                COLUMN_MESSAGE_MODE + " NUMBER NOT NULL, " +
                COLUMN_MESSAGE_TIME_DATE + " TEXT NOT NULL, " +
                COLUMN_MESSAGE_TIME_TIME + " TEXT NOT NULL, " +
                COLUMN_MESSAGE_TIME_TIMESTAMP + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);";


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
            onCreate(sqLiteDatabase);
        }
    }
}

//-----------------------------
//-----------------------------
// USER REPOSITORY
//-----------------------------
//-----------------------------
// This class is responsible for:
// - creating accounts
// - checking login credentials
// - storing and reading SMS settings (phone + opt-in)
// - looking up the userId for a username
//
// All interaction with the "users" table goes through here.

package com.example.cs360projectthreeedgarpena.repository;

//-----------------------------
// IMPORTS
//-----------------------------
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.cs360projectthreeedgarpena.schema.AppDatabaseHelper;
import com.example.cs360projectthreeedgarpena.schema.DbContract;

//-----------------------------
// CLASS DEFINITION
//-----------------------------
public class UserRepository {

    private final AppDatabaseHelper dbHelper;

    public UserRepository(AppDatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    //-----------------------------
    // registerUser
    //-----------------------------
    public boolean registerUser(String username,
                                String password,
                                String phoneNumber,
                                boolean smsEnabled) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.Users.COL_USERNAME, username);
        values.put(DbContract.Users.COL_PASSWORD_HASH, password);
        values.put(DbContract.Users.COL_PHONE, phoneNumber);
        values.put(DbContract.Users.COL_SMS_ENABLED, smsEnabled ? 1 : 0);
        values.put(DbContract.Users.COL_CREATED_AT, System.currentTimeMillis());

        long result = db.insert(DbContract.Users.TABLE_NAME, null, values);
        db.close();
        return result != -1;
    }

    //-----------------------------
    // doesUserExist
    //-----------------------------
    public boolean doesUserExist(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DbContract.Users.COL_USERNAME + "=?";
        String[] selectionArgs = { username };
        Cursor cursor = db.query(
                DbContract.Users.TABLE_NAME,
                new String[]{ DbContract.Users.COL_ID },
                selection,
                selectionArgs,
                null,
                null,
                null,
                "1"
        );
        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    //-----------------------------
    // authenticateUserCredentials
    //-----------------------------
    public boolean authenticateUserCredentials(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DbContract.Users.COL_USERNAME + "=? AND " +
                DbContract.Users.COL_PASSWORD_HASH + "=?";
        String[] selectionArgs = { username, password };
        Cursor cursor = db.query(
                DbContract.Users.TABLE_NAME,
                new String[]{ DbContract.Users.COL_ID },
                selection,
                selectionArgs,
                null,
                null,
                null,
                "1"
        );
        boolean isValid = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        db.close();
        return isValid;
    }

    //-----------------------------
    // getUserIdByUsername
    //-----------------------------
    public int getUserIdByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DbContract.Users.COL_USERNAME + "=?";
        String[] selectionArgs = { username };
        Cursor cursor = db.query(
                DbContract.Users.TABLE_NAME,
                new String[]{ DbContract.Users.COL_ID },
                selection,
                selectionArgs,
                null,
                null,
                null,
                "1"
        );
        int id = -1;
        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.Users.COL_ID));
        }
        if (cursor != null) cursor.close();
        db.close();
        return id;
    }

    //-----------------------------
    // getUserSmsSettings
    //-----------------------------
    public SmsSettings getUserSmsSettings(String username) {
        SmsSettings result = new SmsSettings(null, false);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DbContract.Users.COL_USERNAME + "=?";
        String[] selectionArgs = { username };
        Cursor cursor = db.query(
                DbContract.Users.TABLE_NAME,
                new String[]{
                        DbContract.Users.COL_PHONE,
                        DbContract.Users.COL_SMS_ENABLED
                },
                selection,
                selectionArgs,
                null,
                null,
                null,
                "1"
        );
        if (cursor != null && cursor.moveToFirst()) {
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Users.COL_PHONE));
            boolean enabled = cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.Users.COL_SMS_ENABLED)) == 1;
            result = new SmsSettings(phone, enabled);
        }
        if (cursor != null) cursor.close();
        db.close();
        return result;
    }

    //-----------------------------
    // updateUserSmsSettings
    //-----------------------------
    public boolean updateUserSmsSettings(String username, String phone, boolean enabled) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.Users.COL_PHONE, phone);
        values.put(DbContract.Users.COL_SMS_ENABLED, enabled ? 1 : 0);
        String where = DbContract.Users.COL_USERNAME + "=?";
        String[] whereArgs = { username };
        int rows = db.update(DbContract.Users.TABLE_NAME, values, where, whereArgs);
        db.close();
        return rows > 0;
    }

    //-----------------------------
    // getUserPhoneByUsername
    //-----------------------------
    // Returns just the stored phone number for the given username, or null if not found.
    public String getUserPhoneByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String phone = null;
        Cursor cursor = db.rawQuery(
                "SELECT " + DbContract.Users.COL_PHONE +
                        " FROM " + DbContract.Users.TABLE_NAME +
                        " WHERE " + DbContract.Users.COL_USERNAME + "=? LIMIT 1",
                new String[]{ username }
        );
        if (cursor.moveToFirst()) {
            phone = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return phone;
    }

    //-----------------------------
    // SmsSettings VALUE CLASS
    //-----------------------------
    public static class SmsSettings {
        public final String phone;
        public final boolean enabled;
        public SmsSettings(String phone, boolean enabled) {
            this.phone = phone;
            this.enabled = enabled;
        }
    }
}
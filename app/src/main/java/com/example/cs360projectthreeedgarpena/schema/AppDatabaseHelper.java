//-----------------------------
//-----------------------------
// APP DATABASE HELPER
//-----------------------------
//-----------------------------
// Creates and upgrades the SQLite database.
// Defines the "users" table and the "items" table.

package com.example.cs360projectthreeedgarpena.schema;

//-----------------------------
// IMPORTS
//-----------------------------
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//-----------------------------
// CLASS DEFINITION
//-----------------------------
public class AppDatabaseHelper extends SQLiteOpenHelper {

    //-----------------------------
    // DB NAME / VERSION
    //-----------------------------
    private static final String DATABASE_NAME = "inventory_app.db";

    //***************************
    //
    //
    // Bump +1 when schema changes
    //
    //
    //****************************
    private static final int DATABASE_VERSION = 8;

    //-----------------------------
    // CONSTRUCTOR
    //-----------------------------
    public AppDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //-----------------------------
    // onCreate
    //-----------------------------
    @Override
    public void onCreate(SQLiteDatabase db) {

        //-----------------------------
        // CREATE USERS TABLE
        //-----------------------------
        // Holds login info + optional SMS alert settings.
        db.execSQL(
                "CREATE TABLE " + DbContract.Users.TABLE_NAME + " (" +
                        DbContract.Users.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        DbContract.Users.COL_USERNAME + " TEXT UNIQUE NOT NULL, " +
                        DbContract.Users.COL_PASSWORD_HASH + " TEXT NOT NULL, " +
                        DbContract.Users.COL_PHONE + " TEXT, " +
                        DbContract.Users.COL_SMS_ENABLED + " INTEGER DEFAULT 0, " +
                        DbContract.Users.COL_CREATED_AT + " INTEGER NOT NULL" +
                        ");"
        );

        //-----------------------------
        // CREATE ITEMS TABLE
        //-----------------------------
        // Each row is a single inventory item that belongs to a specific user_id.
        db.execSQL(
                "CREATE TABLE " + DbContract.Items.TABLE_NAME + " (" +
                        DbContract.Items.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        DbContract.Items.COL_USER_ID + " INTEGER NOT NULL, " +
                        DbContract.Items.COL_ITEM_NAME + " TEXT NOT NULL, " +
                        DbContract.Items.COL_AMOUNT + " INTEGER DEFAULT 0, " +
                        DbContract.Items.COL_PRICE + " REAL DEFAULT 0.0, " +
                        DbContract.Items.COL_LOCATION + " TEXT, " +
                        DbContract.Items.COL_LOW_THRESHOLD + " INTEGER DEFAULT 2, " +
                        DbContract.Items.COL_MED_THRESHOLD + " INTEGER DEFAULT 5, " +
                        "FOREIGN KEY(" + DbContract.Items.COL_USER_ID + ") REFERENCES " +
                        DbContract.Users.TABLE_NAME + "(" + DbContract.Users.COL_ID + ")" +
                        ");"
        );

        //-----------------------------
        // INDEXES
        //-----------------------------
        // Index user_id so we can quickly pull "this user's items".
        db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_items_user ON " +
                        DbContract.Items.TABLE_NAME + " (" +
                        DbContract.Items.COL_USER_ID +
                        ");"
        );

        // Index item_name to make sorting/search easier.
        db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_items_name ON " +
                        DbContract.Items.TABLE_NAME + " (" +
                        DbContract.Items.COL_ITEM_NAME +
                        ");"
        );
    }

    //-----------------------------
    // onUpgrade
    //-----------------------------
    //
    // drop tables and recreate them if DB version changes.
    // This wipes data between versions.
    // This is good for when there is changes in the actual structure of
    // the db such as adding a COL etc.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.Items.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.Users.TABLE_NAME);
        onCreate(db);
    }
}
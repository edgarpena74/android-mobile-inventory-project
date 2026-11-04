//-----------------------------
//-----------------------------
// DB CONTRACT
//-----------------------------
//-----------------------------
// Main place for table + column names so we don't typo strings across the app.

package com.example.cs360projectthreeedgarpena.schema;

//-----------------------------
// CLASS DEFINITION
//-----------------------------
public final class DbContract {

    // Private so nobody makes an instance of this
    private DbContract() {}

    //-----------------------------
    // USERS TABLE
    //-----------------------------
    // Stores login info and optional SMS alert settings for each account.
    public static final class Users {

        // Table name for the users table
        public static final String TABLE_NAME = "users";

        // Primary key (auto-increment int)
        public static final String COL_ID = "id";

        // The username for login (must be unique)
        public static final String COL_USERNAME = "username";

        // The stored password (plain text in this "academic" version. Will be encrypted for a "Production" version.)
        public static final String COL_PASSWORD_HASH = "password_hash";

        // Phone number for SMS notifications (nullable)
        public static final String COL_PHONE = "phone";

        // Whether SMS alerts are enabled for this user (1 = on, 0 = off)
        public static final String COL_SMS_ENABLED = "sms_enabled";

        // When the account was created (System.currentTimeMillis())
        public static final String COL_CREATED_AT = "created_at";
    }

    //-----------------------------
    // ITEMS TABLE
    //-----------------------------
    // Inventory items. Each row belongs to exactly one user.
    public static final class Items {

        // Table name for the items table
        public static final String TABLE_NAME = "items";

        // Primary key for the item row
        public static final String COL_ID = "id";

        // Which user owns this item
        public static final String COL_USER_ID = "user_id";

        // item name
        public static final String COL_ITEM_NAME = "item_name";

        // Current quantity in stock
        public static final String COL_AMOUNT = "amount";

        // Unit price for this item
        public static final String COL_PRICE = "price";

        // Where the item is stored (rack, bin, shelf, etc.)
        public static final String COL_LOCATION = "location";

        // Threshold for "Low" stock (amount <= low means critical)
        public static final String COL_LOW_THRESHOLD = "low_threshold";

        // Threshold for "Medium" stock (amount <= med means warning)
        public static final String COL_MED_THRESHOLD = "med_threshold";
    }
}
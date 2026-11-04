//-----------------------------
//-----------------------------
// INVENTORY REPOSITORY
//-----------------------------
//-----------------------------
// Handles CRUD for items in the "items" table.
// Every item row belongs to a specific user_id so different
// users don't see each other's inventory.

package com.example.cs360projectthreeedgarpena.repository;

//-----------------------------
// IMPORTS
//-----------------------------
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.cs360projectthreeedgarpena.schema.AppDatabaseHelper;
import com.example.cs360projectthreeedgarpena.schema.DbContract;

import java.util.ArrayList;
import java.util.List;

//-----------------------------
// CLASS DEFINITION
//-----------------------------
public class InventoryRepository {

    //-----------------------------
    // DB HELPER REFERENCE
    //-----------------------------
    private final AppDatabaseHelper dbHelper;

    //-----------------------------
    // CONSTRUCTOR
    //-----------------------------
    public InventoryRepository(AppDatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    //-----------------------------
    // createItem
    //-----------------------------
    // Inserts a new item row for this specific user.
    // Returns true if insert worked.
    public boolean createItem(
            int userId,
            String itemName,
            int amount,
            double price,
            String location,
            int lowThreshold,
            int medThreshold
    ) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        // Which user this item belongs to
        values.put(DbContract.Items.COL_USER_ID, userId);

        // Name of the item
        values.put(DbContract.Items.COL_ITEM_NAME, itemName);

        // How many we have
        values.put(DbContract.Items.COL_AMOUNT, amount);

        // Price per unit
        values.put(DbContract.Items.COL_PRICE, price);

        // Where it is stored
        values.put(DbContract.Items.COL_LOCATION, location);

        // Thresholds for status color
        values.put(DbContract.Items.COL_LOW_THRESHOLD, lowThreshold);
        values.put(DbContract.Items.COL_MED_THRESHOLD, medThreshold);

        // Insert row
        long newId = db.insert(DbContract.Items.TABLE_NAME, null, values);

        db.close();
        return newId > 0;
    }

    //-----------------------------
    // getAllItems
    //-----------------------------
    // Returns a list of all items for the given userId.
    public List<ItemRecord> getAllItems(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.query(
                // table
                DbContract.Items.TABLE_NAME,
                //all columns
                null,
                // WHERE user_id = ?
                DbContract.Items.COL_USER_ID + "=?",
                // where args
                new String[]{ String.valueOf(userId) },
                // groupBy
                null,
                //having
                null,
                // ORDER BY name
                DbContract.Items.COL_ITEM_NAME + " ASC"
        );

        List<ItemRecord> result = new ArrayList<>();

        if (c != null && c.moveToFirst()) {
            do {
                // Build ItemRecord from this row
                int id = c.getInt(c.getColumnIndexOrThrow(DbContract.Items.COL_ID));
                String name = c.getString(c.getColumnIndexOrThrow(DbContract.Items.COL_ITEM_NAME));
                int amount = c.getInt(c.getColumnIndexOrThrow(DbContract.Items.COL_AMOUNT));
                double price = c.getDouble(c.getColumnIndexOrThrow(DbContract.Items.COL_PRICE));
                String location = c.getString(c.getColumnIndexOrThrow(DbContract.Items.COL_LOCATION));

                result.add(new ItemRecord(id, name, amount, price, location));
            } while (c.moveToNext());

            c.close();
        }

        db.close();
        return result;
    }

    //-----------------------------
    // updateItem
    //-----------------------------
    // Updates amount, price, and location for an item (by id).
    public boolean updateItem(int itemId, int newAmount, double newPrice, String newLocation) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbContract.Items.COL_AMOUNT, newAmount);
        values.put(DbContract.Items.COL_PRICE, newPrice);
        values.put(DbContract.Items.COL_LOCATION, newLocation);

        int rows = db.update(
                DbContract.Items.TABLE_NAME,
                values,
                DbContract.Items.COL_ID + "=?",
                new String[]{ String.valueOf(itemId) }
        );

        db.close();
        return rows > 0;
    }

    //-----------------------------
    // deleteItem
    //-----------------------------
    // Deletes an item row by its id.
    public boolean deleteItem(int itemId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int rows = db.delete(
                DbContract.Items.TABLE_NAME,
                DbContract.Items.COL_ID + "=?",
                new String[]{ String.valueOf(itemId) }
        );

        db.close();
        return rows > 0;
    }

    //-----------------------------
    // ItemRecord
    //-----------------------------
    // data holder for one row from "items".
    public static class ItemRecord {
        public final int id;
        public final String name;
        public final int amount;
        public final double price;
        public final String location;

        public ItemRecord(int id, String name, int amount, double price, String location) {
            this.id = id;
            this.name = name;
            this.amount = amount;
            this.price = price;
            this.location = location;
        }
    }
}
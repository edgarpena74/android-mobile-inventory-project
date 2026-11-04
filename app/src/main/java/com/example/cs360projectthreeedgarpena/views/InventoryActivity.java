//-----------------------------
//-----------------------------
// INVENTORY ACTIVITY
//-----------------------------
//-----------------------------
// Shows this user's inventory and lets them Add / Update / Delete items.
// When you tap an item, it auto-fills the form fields so you can edit it.
// If stock is low (<=2) for that item (or after add/update), we *simulate*
// an SMS alert with a Toast, BUT ONLY if SMS alerts are enabled for this user.
//-----------------------------

package com.example.cs360projectthreeedgarpena.views;

//-----------------------------
// IMPORTS
//-----------------------------
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cs360projectthreeedgarpena.R;
import com.example.cs360projectthreeedgarpena.adapters.InventoryGridAdapter;
import com.example.cs360projectthreeedgarpena.repository.InventoryRepository;
import com.example.cs360projectthreeedgarpena.schema.AppDatabaseHelper;

//-----------------------------
// CLASS DEFINITION
//-----------------------------
public class InventoryActivity extends Activity {

    //-----------------------------
    // DB / REPO
    //-----------------------------
    private InventoryRepository inventoryRepository;

    //-----------------------------
    // UI
    //-----------------------------
    private GridView gridViewInventory;
    private EditText editTextItemName;
    private EditText editTextItemAmount;
    private EditText editTextItemPrice;
    private EditText editTextItemLocation;
    private Button buttonAddItem;
    private Button buttonUpdateItem;
    private Button buttonDeleteItem;
    private Button buttonLogout;
    private TextView textViewSelectedItem;

    //-----------------------------
    // STATE
    //-----------------------------
    // Passed in from HomeActivity so we know:
    // - which user's items to load
    // - whether SMS is enabled for them
    // - what phone number to "send" to
    private int userId = -1;
    private boolean smsEnabled = false;
    private String phoneNumber = null;

    // Track which item is currently selected for update/delete
    private int selectedItemId = -1;
    private InventoryRepository.ItemRecord selectedItemRecord = null;

    //-----------------------------
    // onCreate
    //-----------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Pull session info from HomeActivity
        userId = getIntent().getIntExtra("EXTRA_USER_ID", -1);
        phoneNumber = getIntent().getStringExtra("EXTRA_PHONE");
        smsEnabled = getIntent().getBooleanExtra("EXTRA_SMS_ENABLED", false);

        // If we somehow got launched without a valid userId, abort.
        if (userId < 0) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            Intent backToLogin = new Intent(InventoryActivity.this, LoginActivity.class);
            startActivity(backToLogin);
            finish();
            return;
        }

        // Init DB helper + repo
        AppDatabaseHelper dbHelper = new AppDatabaseHelper(this);
        inventoryRepository = new InventoryRepository(dbHelper);

        // Bind views
        gridViewInventory    = findViewById(R.id.gridViewInventory);
        editTextItemName     = findViewById(R.id.editTextItemName);
        editTextItemAmount   = findViewById(R.id.editTextItemAmount);
        editTextItemPrice    = findViewById(R.id.editTextItemPrice);
        editTextItemLocation = findViewById(R.id.editTextItemLocation);
        buttonAddItem        = findViewById(R.id.buttonAddItem);
        buttonUpdateItem     = findViewById(R.id.buttonUpdateItem);
        buttonDeleteItem     = findViewById(R.id.buttonDeleteItem);
        buttonLogout         = findViewById(R.id.buttonLogout);
        textViewSelectedItem = findViewById(R.id.textViewSelectedItem);

        // Load grid data for THIS user
        refreshGrid();

        //-----------------------------
        // GRID TAP HANDLER
        //-----------------------------
        gridViewInventory.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {

            // Get the tapped row's data
            InventoryRepository.ItemRecord clicked =
                    (InventoryRepository.ItemRecord) parent.getItemAtPosition(position);

            // Keep track of selection
            selectedItemRecord = clicked;
            selectedItemId     = clicked.id;

            // Auto-fill edit fields
            editTextItemName.setText(clicked.name);
            editTextItemAmount.setText(String.valueOf(clicked.amount));
            editTextItemPrice.setText(String.valueOf(clicked.price));
            editTextItemLocation.setText(clicked.location != null ? clicked.location : "");
            textViewSelectedItem.setText("Selected: " + clicked.name);

            // Only simulate an SMS alert if:
            // - SMS is enabled for this user
            // - The item's amount is low/out (<=2)
            if (smsEnabled && clicked.amount <= 2) {
                showMockSmsToast(clicked.name, clicked.amount);
            }
        });

        //-----------------------------
        // ADD
        //-----------------------------
        buttonAddItem.setOnClickListener(v -> handleAddItem());

        //-----------------------------
        // UPDATE
        //-----------------------------
        buttonUpdateItem.setOnClickListener(v -> handleUpdateItem());

        //-----------------------------
        // DELETE
        //-----------------------------
        buttonDeleteItem.setOnClickListener(v -> handleDeleteItem());

        //-----------------------------
        // LOGOUT
        //-----------------------------
        buttonLogout.setOnClickListener(v -> {
            Intent backToLogin = new Intent(InventoryActivity.this, LoginActivity.class);
            startActivity(backToLogin);
            finish();
        });
    }

    //-----------------------------
    // handleAddItem
    //-----------------------------
    private void handleAddItem() {
        String name      = editTextItemName.getText().toString().trim();
        String amountStr = editTextItemAmount.getText().toString().trim();
        String priceStr  = editTextItemPrice.getText().toString().trim();
        String location  = editTextItemLocation.getText().toString().trim();

        if (name.isEmpty() || amountStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill in name, amount, and price", Toast.LENGTH_SHORT).show();
            return;
        }

        int amount   = Integer.parseInt(amountStr);
        double price = Double.parseDouble(priceStr);

        boolean success = inventoryRepository.createItem(
                userId,
                name,
                amount,
                price,
                location,
                // lowThreshold
                2,
                // medThreshold
                5
        );

        if (success) {
            Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();

            // If SMS is enabled and this new amount is low/out, simulate SMS
            if (smsEnabled && amount <= 2) {
                showMockSmsToast(name, amount);
            }

            clearInputs();
            refreshGrid();
        } else {
            Toast.makeText(this, "Error adding item", Toast.LENGTH_SHORT).show();
        }
    }

    //-----------------------------
    // handleUpdateItem
    //-----------------------------
    private void handleUpdateItem() {
        if (selectedItemId < 0 || selectedItemRecord == null) {
            Toast.makeText(this, "Select an item first", Toast.LENGTH_SHORT).show();
            return;
        }

        String amountStr = editTextItemAmount.getText().toString().trim();
        String priceStr  = editTextItemPrice.getText().toString().trim();
        String location  = editTextItemLocation.getText().toString().trim();

        int newAmount = amountStr.isEmpty()
                ? selectedItemRecord.amount
                : Integer.parseInt(amountStr);

        double newPrice = priceStr.isEmpty()
                ? selectedItemRecord.price
                : Double.parseDouble(priceStr);

        boolean ok = inventoryRepository.updateItem(
                selectedItemId,
                newAmount,
                newPrice,
                location
        );

        if (ok) {
            Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();

            // If SMS is enabled and amount is now low/out, simulate SMS
            if (smsEnabled && newAmount <= 2) {
                showMockSmsToast(selectedItemRecord.name, newAmount);
            }

            clearInputs();
            selectedItemId = -1;
            selectedItemRecord = null;
            refreshGrid();
        } else {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }
    }

    //-----------------------------
    // handleDeleteItem
    //-----------------------------
    private void handleDeleteItem() {
        if (selectedItemId < 0) {
            Toast.makeText(this, "Select an item first", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean ok = inventoryRepository.deleteItem(selectedItemId);

        if (ok) {
            Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
            clearInputs();
            selectedItemId = -1;
            selectedItemRecord = null;
            refreshGrid();
        } else {
            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
        }
    }

    //-----------------------------
    // showMockSmsToast
    //-----------------------------
    // Builds and shows the "SMS sent" style Toast using the
    // phone number + stock status.
    private void showMockSmsToast(String itemName, int amount) {

        // If SMS wasn't enabled, don't show anything (extra safety check)
        if (!smsEnabled) {
            return;
        }

        // Fallback number if nothing provided
        String phoneDisplay = (phoneNumber != null && !phoneNumber.isEmpty())
                ? phoneNumber
                : "(000)000-0000";

        // Human-readable stock status
        String statusText = (amount == 0) ? "Out of Stock" : "Low Stock";

        // Final mock "alert" text
        String message = "SMS Notification sent to " + phoneDisplay +
                "\nItem \"" + itemName + "\" is " + statusText;

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    //-----------------------------
    // refreshGrid
    //-----------------------------
    private void refreshGrid() {
        InventoryGridAdapter adapter =
                new InventoryGridAdapter(
                        this,
                        inventoryRepository.getAllItems(userId)
                );
        gridViewInventory.setAdapter(adapter);
    }

    //-----------------------------
    // clearInputs
    //-----------------------------
    private void clearInputs() {
        editTextItemName.setText("");
        editTextItemAmount.setText("");
        editTextItemPrice.setText("");
        editTextItemLocation.setText("");
        textViewSelectedItem.setText("Selected: none");
    }
}
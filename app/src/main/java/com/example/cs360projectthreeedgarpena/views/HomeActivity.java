//-----------------------------
//-----------------------------
// HOME ACTIVITY
//-----------------------------
//-----------------------------
// Displays welcome info, allows toggling SMS alerts, editing phone number,
// testing SMS messages, opening inventory, and logging out.
//-----------------------------

package com.example.cs360projectthreeedgarpena.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cs360projectthreeedgarpena.R;
import com.example.cs360projectthreeedgarpena.repository.UserRepository;
import com.example.cs360projectthreeedgarpena.repository.UserRepository.SmsSettings;
import com.example.cs360projectthreeedgarpena.schema.AppDatabaseHelper;

public class HomeActivity extends Activity {

    //-----------------------------
    // UI FIELDS
    //-----------------------------
    private TextView textViewWelcome;
    private Switch switchEnableSms;
    private EditText editTextPhoneNumber;
    private Button buttonTestSms;
    private Button buttonOpenInventory;
    private Button buttonLogout;

    //-----------------------------
    // USER CONTEXT / DB
    //-----------------------------
    private String username;
    private int userId = -1;
    private UserRepository userRepository;

    //-----------------------------
    // onCreate
    //-----------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Get extras passed from LoginActivity
        username = getIntent().getStringExtra("EXTRA_USERNAME");
        userId = getIntent().getIntExtra("EXTRA_USER_ID", -1);

        if (username == null || userId < 0) {
            Toast.makeText(this, "Missing user session. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Init database
        AppDatabaseHelper dbHelper = new AppDatabaseHelper(this);
        userRepository = new UserRepository(dbHelper);

        // Bind UI components
        textViewWelcome = findViewById(R.id.textViewWelcome);
        switchEnableSms = findViewById(R.id.switchEnableSms);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        buttonTestSms = findViewById(R.id.buttonTestSms);
        buttonOpenInventory = findViewById(R.id.buttonOpenInventory);
        buttonLogout = findViewById(R.id.buttonLogout);

        // Display welcome message
        textViewWelcome.setText("Welcome, " + username);

        //-----------------------------
        // LOAD EXISTING SMS SETTINGS
        //-----------------------------
        SmsSettings settings = userRepository.getUserSmsSettings(username);
        switchEnableSms.setChecked(settings.enabled);
        editTextPhoneNumber.setText(settings.phone != null ? settings.phone : "");

        //-----------------------------
        // SAVE SETTINGS ON SWITCH TOGGLE
        //-----------------------------
        switchEnableSms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String phone = editTextPhoneNumber.getText().toString().trim();
            userRepository.updateUserSmsSettings(username, phone, isChecked);
            Toast.makeText(this, isChecked
                    ? "SMS alerts enabled"
                    : "SMS alerts disabled", Toast.LENGTH_SHORT).show();
        });

        //-----------------------------
        // SAVE SETTINGS WHEN PHONE CHANGES
        //-----------------------------
        editTextPhoneNumber.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String phone = editTextPhoneNumber.getText().toString().trim();
                boolean enabled = switchEnableSms.isChecked();
                userRepository.updateUserSmsSettings(username, phone, enabled);
            }
        });

        //-----------------------------
        // TEST SMS BUTTON
        //-----------------------------
        buttonTestSms.setOnClickListener(v -> {

            // Grab latest values from UI
            boolean smsEnabledNow = switchEnableSms.isChecked();
            String phoneNow = editTextPhoneNumber.getText().toString().trim();

            // Keep them (so DB stays in sync with what we just tested)
            userRepository.updateUserSmsSettings(username, phoneNow, smsEnabledNow);

            if (smsEnabledNow) {
                String phoneDisplay = phoneNow.isEmpty() ? "(000)000-0000" : phoneNow;
                Toast.makeText(this,
                        "SMS Notification sent to " + phoneDisplay +
                                "\nYour test message was successful.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this,
                        "SMS alerts are not currently enabled. Please change your settings.",
                        Toast.LENGTH_LONG).show();
            }
        });

        //-----------------------------
        // OPEN INVENTORY BUTTON
        //-----------------------------
        buttonOpenInventory.setOnClickListener(v -> {
            // Get freshest values from UI first
            String phoneNow = editTextPhoneNumber.getText().toString().trim();
            boolean smsEnabledNow = switchEnableSms.isChecked();

            // Save them before moving screens
            userRepository.updateUserSmsSettings(username, phoneNow, smsEnabledNow);

            // Launch InventoryActivity WITH full context
            Intent intent = new Intent(HomeActivity.this, InventoryActivity.class);
            // which user's inventory
            intent.putExtra("EXTRA_USER_ID", userId);
            // phone for mock SMS
            intent.putExtra("EXTRA_PHONE", phoneNow);
            // whether alerts are allowed or not
            intent.putExtra("EXTRA_SMS_ENABLED", smsEnabledNow);
            startActivity(intent);
        });

        //-----------------------------
        // LOGOUT BUTTON
        //-----------------------------
        buttonLogout.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });
    }
}
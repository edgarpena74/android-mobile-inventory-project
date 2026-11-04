package com.example.cs360projectthreeedgarpena.views;

//-----------------------------
// IMPORTS
//-----------------------------
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cs360projectthreeedgarpena.R;
import com.example.cs360projectthreeedgarpena.repository.UserRepository;
import com.example.cs360projectthreeedgarpena.schema.AppDatabaseHelper;

public class SmsSettingsActivity extends Activity {

    //-----------------------------
    // FIELDS
    //-----------------------------
    private EditText editTextPhone;
    private CheckBox checkBoxSmsEnabled;
    private Button buttonSaveSmsSettings;

    private UserRepository userRepository;
    private String currentUsername = null;

    //-----------------------------
    // ON CREATE
    //-----------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_settings);

        // Get username from previous screen so we know which row to update
        currentUsername = getIntent().getStringExtra("EXTRA_USERNAME");

        // Init DB + repo
        AppDatabaseHelper dbHelper = new AppDatabaseHelper(this);
        userRepository = new UserRepository(dbHelper);

        // Bind views
        editTextPhone = findViewById(R.id.editTextPhone);
        checkBoxSmsEnabled = findViewById(R.id.checkBoxSmsEnabled);
        buttonSaveSmsSettings = findViewById(R.id.buttonSaveSmsSettings);

        // Load existing settings from DB
        UserRepository.SmsSettings settings = userRepository.getUserSmsSettings(currentUsername);

        // Pre-fill UI with current DB values
        if (settings.phone != null) {
            editTextPhone.setText(formatAsPhone(settings.phone));
        }
        checkBoxSmsEnabled.setChecked(settings.enabled);

        // Add live formatter so typing becomes "(000)000-0000"
        editTextPhone.addTextChangedListener(new TextWatcher() {
            private boolean selfEdit = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Prevent recursive editing
                if (selfEdit) return;

                String raw = s.toString();
                // keep only 0-9
                String digitsOnly = raw.replaceAll("[^0-9]", "");

                // Limit to 10 digits max
                if (digitsOnly.length() > 10) {
                    digitsOnly = digitsOnly.substring(0, 10);
                }

                String formatted = formatAsPhone(digitsOnly);

                // If formatted looks different from what user sees, replace it
                if (!formatted.equals(raw)) {
                    selfEdit = true;
                    editTextPhone.setText(formatted);
                    // move cursor to end
                    editTextPhone.setSelection(formatted.length());
                    selfEdit = false;
                }
            }
        });

        // Save button click
        buttonSaveSmsSettings.setOnClickListener(v -> handleSave());
    }

    //-----------------------------
    // handleSave()
    //-----------------------------
    // Reads the phone and checkbox state, validates, and writes to DB.
    private void handleSave() {

        // Pull latest UI state
        String formatted = editTextPhone.getText().toString().trim();
        String digitsOnly = formatted.replaceAll("[^0-9]", "");

        // Basic validation: must be exactly 10 digits if SMS is enabled
        if (checkBoxSmsEnabled.isChecked()) {
            if (digitsOnly.length() != 10) {
                Toast.makeText(this, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Save to DB
        boolean ok = userRepository.updateUserSmsSettings(
                currentUsername,
                // store raw digits in DB
                digitsOnly,
                // store enabled flag
                checkBoxSmsEnabled.isChecked()
        );

        if (ok) {
            Toast.makeText(this, "SMS settings saved", Toast.LENGTH_SHORT).show();
            // close screen
            finish();
        } else {
            Toast.makeText(this, "Could not save settings", Toast.LENGTH_SHORT).show();
        }
    }

    //-----------------------------
    // formatAsPhone()
    //-----------------------------
    // Takes only digits, returns (123)456-7890 style as the user types.
    private String formatAsPhone(String digitsOnly) {
        // If we don't have enough digits to format nicely yet, just return as-is
        if (digitsOnly.length() == 0) return "";
        if (digitsOnly.length() <= 3) return "(" + digitsOnly;
        if (digitsOnly.length() <= 6) {
            return "(" + digitsOnly.substring(0,3) + ")" + digitsOnly.substring(3);
        }
        // length 7-10
        return "(" + digitsOnly.substring(0,3) + ")"
                + digitsOnly.substring(3,6)
                + "-" + digitsOnly.substring(6);
    }
}
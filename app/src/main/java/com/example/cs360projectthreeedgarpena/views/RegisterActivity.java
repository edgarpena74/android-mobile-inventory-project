//-----------------------------
//-----------------------------
// REGISTER ACTIVITY
//-----------------------------
//-----------------------------
// Lets a new user create an account (username + password + optional SMS phone).
// If SMS is enabled, we collect a phone number and save that to the DB.
// After success, we go back to LoginActivity.
package com.example.cs360projectthreeedgarpena.views;

//-----------------------------
// IMPORTS
//-----------------------------
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import android.content.Intent;

import com.example.cs360projectthreeedgarpena.R;
import com.example.cs360projectthreeedgarpena.repository.UserRepository;
import com.example.cs360projectthreeedgarpena.schema.AppDatabaseHelper;

//-----------------------------
// CLASS DEFINITION
//-----------------------------
public class RegisterActivity extends Activity {

    //-----------------------------
    // UI FIELDS
    //-----------------------------
    // Username input field
    private EditText editTextNewUsername;
    // Password input field
    private EditText editTextNewPassword;
    // Phone number field (visible only if SMS toggle is on)
    private EditText editTextPhoneNumber;
    // "Enable SMS Alerts" switch
    private Switch switchEnableSms;
    // Create account button
    private Button buttonCreateAccount;
    // Back to Login button
    private Button buttonBackToLogin;
    // Row wrapper for phone + switch so we can control visibility cleanly if needed
    private LinearLayout layoutSmsToggleRow;

    //-----------------------------
    // DB / REPO
    //-----------------------------
    private AppDatabaseHelper dbHelper;
    private UserRepository userRepository;

    //-----------------------------
    // onCreate
    //-----------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Init DB + repo so we can write new user rows
        dbHelper = new AppDatabaseHelper(this);
        userRepository = new UserRepository(dbHelper);

        // Bind views from XML
        editTextNewUsername   = findViewById(R.id.editTextNewUsername);
        editTextNewPassword   = findViewById(R.id.editTextNewPassword);
        editTextPhoneNumber   = findViewById(R.id.editTextPhoneNumber);
        switchEnableSms       = findViewById(R.id.switchEnableSms);
        buttonCreateAccount   = findViewById(R.id.buttonCreateAccount);
        buttonBackToLogin     = findViewById(R.id.buttonBackToLogin);
        layoutSmsToggleRow    = findViewById(R.id.layoutSmsToggleRow);

        //-----------------------------
        // SHOW / HIDE PHONE FIELD BASED ON SWITCH
        //-----------------------------
        // When SMS is OFF: hide phone field (so user doesn't feel forced).
        // When SMS is ON: show phone field so they can enter it.
        switchEnableSms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editTextPhoneNumber.setVisibility(View.VISIBLE);
            } else {
                editTextPhoneNumber.setVisibility(View.GONE);
            }
        });

        //-----------------------------
        // LIVE PHONE FORMATTER
        //-----------------------------
        // As user types digits, we keep it formatted: (123) 456-7890
        editTextPhoneNumber.addTextChangedListener(new TextWatcher() {
            // We use a guard so editing the text from inside afterTextChanged
            // doesn't recursively trigger itself forever.
            private boolean selfChange = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // not used
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (selfChange) return;

                // Grab just digits
                String digitsOnly = s.toString().replaceAll("[^0-9]", "");

                // Limit to 10 digits max
                if (digitsOnly.length() > 10) {
                    digitsOnly = digitsOnly.substring(0, 10);
                }

                // Build formatted string
                String formatted;
                int len = digitsOnly.length();
                if (len <= 3) {
                    // up to "(123"
                    formatted = "(" + digitsOnly;
                } else if (len <= 6) {
                    // "(123) 456"
                    formatted = "(" + digitsOnly.substring(0, 3) + ") " + digitsOnly.substring(3);
                } else {
                    // "(123) 456-7890"
                    formatted = "(" + digitsOnly.substring(0, 3) + ") "
                            + digitsOnly.substring(3, 6) + "-"
                            + digitsOnly.substring(6);
                }

                // Push formatted text back into EditText
                selfChange = true;
                editTextPhoneNumber.setText(formatted);
                editTextPhoneNumber.setSelection(formatted.length()); // move cursor to end
                selfChange = false;
            }
        });

        //-----------------------------
        // CREATE ACCOUNT BUTTON
        //-----------------------------
        buttonCreateAccount.setOnClickListener(v -> handleCreateAccount());

        //-----------------------------
        // BACK TO LOGIN BUTTON
        //-----------------------------
        buttonBackToLogin.setOnClickListener(v -> {
            Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        });
    }

    //-----------------------------
    // handleCreateAccount
    //-----------------------------
    // Reads all fields, validates, writes new user to DB, and (if SMS enabled)
    // we simulate an SMS setup with a Toast for grading.
    private void handleCreateAccount() {

        // Read username / password from text fields
        String newUsername = editTextNewUsername.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();

        // Read phone and sms toggle state
        String formattedPhone = editTextPhoneNumber.getText().toString().trim();
        boolean smsEnabled = switchEnableSms.isChecked();

        // 1. Basic validation: username + password required
        if (newUsername.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. If SMS is ON, phone must be valid (10 digits)
        if (smsEnabled) {
            // Strip formatting so we can count real digits
            String digitsOnly = formattedPhone.replaceAll("[^0-9]", "");
            if (digitsOnly.length() != 10) {
                Toast.makeText(this, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 3. Check if username already exists in DB
        if (userRepository.doesUserExist(newUsername)) {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Insert user into DB
        // For storage we save:
        // - username
        // - password
        // - phone (null if SMS disabled)
        // - sms_enabled (1 or 0)
        String phoneToStore = smsEnabled ? formattedPhone : null;
        boolean ok = userRepository.registerUser(
                newUsername,
                newPassword,
                phoneToStore,
                smsEnabled
        );

        if (!ok) {
            Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show();
            return;
        }

        // 5. Success UI feedback
        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();

        // 6. If SMS is enabled, simulate "SMS setup complete"
        if (smsEnabled) {
            Toast.makeText(
                    this,
                    "SMS alerts enabled for " + formattedPhone,
                    Toast.LENGTH_LONG
            ).show();
        }

        // 7. Send them back to login so they can log in
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
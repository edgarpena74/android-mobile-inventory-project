//-----------------------------
//-----------------------------
// LOGIN ACTIVITY
//-----------------------------
//-----------------------------
// This screen lets a user log in or go create an account.

package com.example.cs360projectthreeedgarpena.views;

//-----------------------------
// IMPORTS
//-----------------------------
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cs360projectthreeedgarpena.R;
import com.example.cs360projectthreeedgarpena.repository.UserRepository;
import com.example.cs360projectthreeedgarpena.schema.AppDatabaseHelper;

//-----------------------------
// CLASS DEFINITION
//-----------------------------
public class LoginActivity extends Activity {

    //-----------------------------
    // UI FIELDS
    //-----------------------------
    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private Button buttonRegisterNewUser;

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
        setContentView(R.layout.activity_login);

        // Create db helper + repo
        dbHelper = new AppDatabaseHelper(this);
        userRepository = new UserRepository(dbHelper);

        // Bind views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegisterNewUser = findViewById(R.id.buttonRegisterNewUser);

        // When "Login" is pressed, try to auth and go to HomeActivity
        buttonLogin.setOnClickListener(v -> handleLogin());

        // When "Create New User" is pressed, go to RegisterActivity
        buttonRegisterNewUser.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    //-----------------------------
    // handleLogin
    //-----------------------------
    private void handleLogin() {
        // Get input values
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString();

        // Basic check
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if there's a row with that username+password
        boolean isValid = userRepository.authenticateUserCredentials(username, password);
        if (!isValid) {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lookup this user's numeric ID
        int userId = userRepository.getUserIdByUsername(username);
        if (userId < 0) {
            Toast.makeText(this, "Login error: user not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve phone number for this user
        String userPhone = userRepository.getUserPhoneByUsername(username);

        // Go to HomeActivity, pass username + userId + phone
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.putExtra("EXTRA_USERNAME", username);
        intent.putExtra("EXTRA_USER_ID", userId);
        intent.putExtra("EXTRA_PHONE", userPhone);
        startActivity(intent);

        // Don't let user come back here with back button
        finish();
    }
}
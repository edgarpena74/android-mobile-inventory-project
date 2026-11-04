// Launcher activity that immediately forwards to the Login screen and then finishes.
package com.example.cs360projectthreeedgarpena;

// Import: Android intent for screen navigation
import android.content.Intent;

// Import: Lifecycle bundle for onCreate
import android.os.Bundle;

// Import: AppCompat base activity
import androidx.appcompat.app.AppCompatActivity;

// Import: Your LoginActivity (in the views package)
import com.example.cs360projectthreeedgarpena.views.LoginActivity;

//-----------------
//-----------------
// MAIN ACTIVITY
//-----------------
//-----------------
public class MainActivity extends AppCompatActivity {

    // Called when the activity is first created; we immediately jump to LoginActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Build an intent that targets the LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);

        // Launch the LoginActivity
        startActivity(intent);

        // Close this launcher so the back button won't return here
        finish();
    }
}
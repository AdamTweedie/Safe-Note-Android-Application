package com.example.safe_note;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity {

    EditText etName; // Input for Username
    EditText etPassword; // Input for password

    Button btnLogin; // Login button
    Button btnSignUp; // Register button

    Context context; // Application context

    String user; // etUsername as string
    String password; // etPassword as string
    String incorrectDetails;
    String userDetails; // User credentials from encrypted shared preference
    String display; // Comparable user credentials

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = (EditText) findViewById(R.id.etName);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);

        // Storing of Shared Preference key data to save info between activities
        SharedPreferences result = getSharedPreferences("TimedNote", Context.MODE_PRIVATE);
        String noteTitle = result.getString("Title", "");
        String noteBody = result.getString("Body", "");
        String refTime = result.getString("EndTime", "0:0:0");
        SharedPreferences.Editor timedNoteEditor = result.edit();
        timedNoteEditor.putString("EndTime", refTime);
        timedNoteEditor.putString("Title", noteTitle);
        timedNoteEditor.putString("Body", noteBody);
        timedNoteEditor.apply();

        String masterKeyAlias = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC); // Encryption key
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        String finalMasterKeyAlias = masterKeyAlias;

        context = getApplicationContext();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            /**
             * Once btnLogin is clicked, an encrypted_credentials shared preference file is created
             * using the encryption key which will see if the key value data is the same as the key
             * value data of user_details. If it is then the user will be allowed into the app since
             * the username and password match.*
             */
            @Override
            public void onClick(View v) {
                user = etName.getText().toString();
                password = etPassword.getText().toString();
                incorrectDetails = "Username or Password is Incorrect.";

                SharedPreferences encryptedPreferences = null;
                try {
                    encryptedPreferences = EncryptedSharedPreferences.create(
                            "encrypted_credentials",
                            finalMasterKeyAlias,
                            context,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    );
                } catch (GeneralSecurityException | IOException e) { // Throw exception e
                    e.printStackTrace();
                }

                assert encryptedPreferences != null;
                userDetails = encryptedPreferences.getString(user + password + "data", incorrectDetails);
                SharedPreferences.Editor editor = encryptedPreferences.edit();
                editor.putString("user_display", userDetails);
                editor.apply(); // Save shared preference

                display = encryptedPreferences.getString("user_display", "");
                Intent loginScreen;
                Context context = getApplicationContext();
                if (display.equals(incorrectDetails)){
                    etName.setText(""); // Reset EditText
                    etPassword.setText("");
                    Toast.makeText(context, "Incorrect details, try again.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Open NotePage.java using implicit intent to change activity
                    loginScreen = new Intent(MainActivity.this, NotePage.class);
                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show();
                    startActivity(loginScreen);
                }

            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            /**
             * Allows users to navigate to Register.java to create an account if they don't
             * already have one, or want to create a new one
             */
            @Override
            public void onClick(View v) {
                Intent registerScreen = new Intent(MainActivity.this, Register.class);
                startActivity(registerScreen);
            }
        });
    }
}
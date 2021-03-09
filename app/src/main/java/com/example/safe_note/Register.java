package com.example.safe_note;

import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Register extends AppCompatActivity {

    EditText userName; // Store user input for username
    EditText password; // Store user input for password
    EditText email; // Store user input for email

    String newUser; // userName as string
    String newPassword; // password as string
    String newEmail; // email as string

    Button btnRegister; // Allows user to register an account with Safe-Note

    Context context; // Application context

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        userName = (EditText) findViewById(R.id.etNewName);
        password = (EditText) findViewById(R.id.etNewPassword);
        email = (EditText) findViewById(R.id.etNewEmail);
        btnRegister = (Button) findViewById(R.id.btnRegister);

        String masterKeyAlias = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC); // Encryption key
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        String finalMasterKeyAlias = masterKeyAlias;

        context = getApplicationContext();
        btnRegister.setOnClickListener(new View.OnClickListener() {
            /**
             * This on click listener prompts a conditional if statement.
             * If registration information is valid, it creates an account, stores the relevant
             * information in an encrypted shared preference and loads MainActivity.java via
             * implicit intent.
             * If registration information is incorrect a Toast message is displayed notifying the
             * user that the registration information is incorrect.
             * **/
            @Override
            public void onClick(View v) {
                if (userName.length() > 3 && password.length() > 3 &&
                        email.getText().toString().contains("@")) {
                    // Then details valid
                    // Encrypted Shared Preferences to store Username and Password
                    SharedPreferences encryptedPreferences = null;
                    try {
                        encryptedPreferences = EncryptedSharedPreferences.create(
                                "encrypted_credentials",
                                finalMasterKeyAlias,
                                context,
                                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                        );
                    } catch (GeneralSecurityException | IOException e) {
                        e.printStackTrace();
                    }

                    newUser = userName.getText().toString();
                    newPassword = password.getText().toString();
                    newEmail = email.getText().toString();

                    assert encryptedPreferences != null;
                    SharedPreferences.Editor editor = encryptedPreferences.edit();
                    editor.putString(newUser + newPassword + "data", "Signed in as " + newUser +
                            " ( " + newEmail + " )");
                    editor.apply(); // Save shared preference

                    Toast.makeText(context, "Account created, log in",
                            Toast.LENGTH_SHORT).show(); // Pop-up message

                    Intent loginScreen = new Intent(Register.this,
                            MainActivity.class); // Load MainActivity via implicit intent
                    startActivity(loginScreen);
                } else {
                    Toast.makeText(context, "Either username, password, or email are invalid",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

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

    EditText userName, password, email;
    Button btnRegister;

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
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        String finalMasterKeyAlias = masterKeyAlias;

        Context context = getApplicationContext();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Encrypted Shared Preferences to store Username and Password
                //https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences
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

                //SharedPreferences preferences = getSharedPreferences("MYPREFS", MODE_PRIVATE);
                
                String newUser = userName.getText().toString();
                String newPassword = password.getText().toString();
                String newEmail = email.getText().toString();

                assert encryptedPreferences != null;
                SharedPreferences.Editor editor = encryptedPreferences.edit();

                editor.putString(newUser + newPassword + "data", "Signed in as " + newUser + " ( " + newEmail + " )");
                editor.apply();

                Toast.makeText(context, "Account created, log in", Toast.LENGTH_SHORT).show();

                Intent loginScreen = new Intent(Register.this, MainActivity.class);
                startActivity(loginScreen);

            }
        });
    }
}

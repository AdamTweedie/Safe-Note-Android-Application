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

    EditText etName, etPassword;
    Button btnLogin, btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = (EditText) findViewById(R.id.etName);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);

        // shared preferences for Notes (not working, try savedInstanceState())
        SharedPreferences result = getSharedPreferences("SaveData", Context.MODE_PRIVATE);
        String value = result.getString("Value", " β β ");
        SharedPreferences.Editor spNoteOneEditor = result.edit();
        System.out.println("SharedPref value from within MainActivity.java " + value);
        spNoteOneEditor.putString("Value", value);
        spNoteOneEditor.apply();


        String masterKeyAlias = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        String finalMasterKeyAlias = masterKeyAlias;

        Context context = getApplicationContext();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etName.getText().toString();
                String password = etPassword.getText().toString();
                String incorrectDetails = "Username or Password is Incorrect.";

                //SharedPreferences preferences = getSharedPreferences("MYPREFS", MODE_PRIVATE);

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

                String userDetails = encryptedPreferences.getString(user + password + "data", incorrectDetails);
                SharedPreferences.Editor editor = encryptedPreferences.edit();
                editor.putString("user_display", userDetails);
                editor.commit();

                String display = encryptedPreferences.getString("user_display", "");


                Intent loginScreen;
                Context context = getApplicationContext();
                if (display.equals(incorrectDetails)){
                    etName.setText("");
                    etPassword.setText("");
                    Toast.makeText(context, "Incorrect details, try again.", Toast.LENGTH_SHORT).show();
                } else{
                    loginScreen = new Intent(MainActivity.this, NotePage.class);
                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show();
                    startActivity(loginScreen);
                }

            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerScreen = new Intent(MainActivity.this, Register.class);
                startActivity(registerScreen);
            }
        });

    }
}
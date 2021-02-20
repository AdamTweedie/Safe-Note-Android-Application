package com.example.safe_note;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

public class NotePage extends AppCompatActivity{

    // Recycler view for Shopping list
    RecyclerView recyclerViewOne;
    // Recycler view for To-Do list
    RecyclerView recyclerViewTwo;
    public ArrayList<EditRecyclerView> ervArrayListOne;
    public ArrayList<EditRecyclerView> ervArrayListTwo;
    CustomAdapter customAdapterOne;
    CustomAdapter customAdapterTwo;
    Button logOut, newEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_page);

        // Encrypted Shared Preferences used to store user credentials to the application
        //SharedPreferences preferences = getSharedPreferences("MYPREFS", MODE_PRIVATE);

        String masterKeyAlias = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        String finalMasterKeyAlias = masterKeyAlias;

        Context context = getApplicationContext();

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
        String display = encryptedPreferences.getString("user_display", "");

        // Display the user that is logged in
        TextView displayInfo = (TextView) findViewById(R.id.textView3);
        displayInfo.setText(display);

        logOut = (Button) findViewById(R.id.btnLogOut);
        newEmail = (Button) findViewById(R.id.btnCreateEmail);
        recyclerViewOne = (RecyclerView) findViewById(R.id.shoppingRecyclerView);
        recyclerViewTwo = (RecyclerView) findViewById(R.id.toDoRecyclerView);

        ervArrayListOne = populateList();
        ervArrayListTwo = populateList();

        customAdapterOne = new CustomAdapter(this, ervArrayListOne);
        customAdapterTwo = new CustomAdapter(this, ervArrayListTwo);

        recyclerViewOne.setAdapter(customAdapterOne);
        recyclerViewTwo.setAdapter(customAdapterTwo);

        recyclerViewOne.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        recyclerViewTwo.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeScreen = new Intent(NotePage.this, MainActivity.class);
                // Start Main Activity
                startActivity(homeScreen);
                // Finish Note Page
                finish();
                Toast.makeText(context, "Goodbye!", Toast.LENGTH_LONG).show();
            }
        });

        newEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createEmail = new Intent(NotePage.this, Email.class);
                startActivity(createEmail);
                finish();
            }
        });


    }

    private ArrayList<EditRecyclerView> populateList(){

        ArrayList<EditRecyclerView> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            EditRecyclerView editRecyclerView = new EditRecyclerView();
            editRecyclerView.setEditTextValue("Item " + (i+1));
            list.add(editRecyclerView);
        }
        return list;
    }


}

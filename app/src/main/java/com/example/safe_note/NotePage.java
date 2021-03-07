package com.example.safe_note;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.format.DateTimeFormatter;
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
    EditText etTimeHour, etTimeMin, etTitle, etBody;
    TextView tvCount, tvTitle, tvBody;
    Button logOut, createNote, delete;
    SharedPreferences spTimedNote;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_page);

        logOut = (Button) findViewById(R.id.btnLogOut);
        delete = (Button) findViewById(R.id.btnDelete);
        recyclerViewOne = (RecyclerView) findViewById(R.id.shoppingRecyclerView);
        recyclerViewTwo = (RecyclerView) findViewById(R.id.toDoRecyclerView);

        ervArrayListOne = populateList();
        ervArrayListTwo = populateList();

        customAdapterOne = new CustomAdapter(this, ervArrayListOne);
        customAdapterTwo = new CustomAdapter(this, ervArrayListTwo);

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

        recyclerViewOne.setAdapter(customAdapterOne);
        recyclerViewTwo.setAdapter(customAdapterTwo);

        recyclerViewOne.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        recyclerViewTwo.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        /** the following code is for the functionality of the main note section, just got
         * the SharedPreferences working so that is saves state between activities.**/

        createNote = (Button) findViewById(R.id.btnCreate);
        etTimeHour = (EditText) findViewById(R.id.etTimeHour);
        etTimeMin = (EditText) findViewById(R.id.etTimeMin);
        etTitle = (EditText) findViewById(R.id.etTitle);
        etBody = (EditText) findViewById(R.id.etBody);
        tvCount = (TextView) findViewById(R.id.tvCountDown);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvBody = (TextView) findViewById(R.id.tvBody);

        LocalTime now = LocalTime.now();

        spTimedNote = getSharedPreferences("TimedNote", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spTimedNote.edit();

        createNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int timeHr = Integer.parseInt(etTimeHour.getText().toString());
                int timeMin = Integer.parseInt(etTimeMin.getText().toString());
                LocalTime refTime = LocalTime.of(timeHr, timeMin);
                editor.putString("EndTime", String.valueOf(refTime));
                editor.putString("Title", etTitle.getText().toString());
                editor.putString("Body", etBody.getText().toString());
                editor.apply();
                tvCount.setText("End time " + etTimeHour.getText().toString() + ":" + etTimeMin.getText().toString());
                tvBody.setText(etBody.getText().toString());
                tvTitle.setText(etTitle.getText().toString());

                if (now.isAfter(refTime)) {
                    tvCount.setText("Safe-Note has deleted this note, create a new one.");
                    tvBody.setText("");
                    tvTitle.setText("");
                    editor.remove("EndTime");
                    editor.remove("Title");
                    editor.remove("Body");
                    editor.apply();
                }

            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvCount.setText("");
                tvTitle.setText("");
                tvBody.setText("");
                editor.remove("EndTime");
                editor.remove("Title");
                editor.remove("Body");
                editor.apply();
            }
        });

         String title = spTimedNote.getString("Title", "");
         String body = spTimedNote.getString("Body", "");
         String endTime = spTimedNote.getString("EndTime", "0:0:0");
         tvCount.setText("Note will delete at: " + endTime);
         tvTitle.setText(title);
         tvBody.setText(body);

         if (endTime == "0:0:0"){
             tvCount.setText("Time is up");
             tvBody.setText("");
             tvTitle.setText("");
         } else {
             if (now.isAfter(LocalTime.parse(endTime))) {
                 tvCount.setText("Time is up");
                 tvBody.setText("");
                 tvTitle.setText("");
                 editor.remove("EndTime");
                 editor.remove("Title");
                 editor.remove("Body");
                 editor.apply();
             }
         }

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

package com.example.safe_note;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.time.LocalTime;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;


public class NotePage extends AppCompatActivity {

    RecyclerView recyclerViewOne; // Recycler view for Shopping list
    RecyclerView recyclerViewTwo; // Recycler view for To-Do list

    public ArrayList<EditRecyclerView> ervArrayListOne; // Recycler view for shopping list
    public ArrayList<EditRecyclerView> ervArrayListTwo; // Recycler view for to-do list

    CustomAdapter customAdapterOne; // Bridge between adapter view and data for shopping list
    CustomAdapter customAdapterTwo; // Bridge between adapter view and data for to-do list

    EditText etTimeHour; // Hour for note to be destroyed (24hr)
    EditText etTimeMin; // Minute for note to be destroyed
    EditText etTitle; // Title of temp note
    EditText etBody; // Main message of temp note

    TextView tvCount; // Display time at which note will be destroyed
    TextView tvTitle; // Display temp note title
    TextView tvBody; // Display main body of temp note
    TextView displayInfo; // Stores current active user

    Button logOut; // Button to log out and close app
    Button createNote; // Creates temp note
    Button delete; // Deletes temp note before timer is finished

    SharedPreferences spTimedNote; // Shared preference which stores, log-in/temp note info

    TextClock clock; // Displays local machine time in 24hr form

    String endTime; // Stores the time that the temp note will be destroyed
    String display; // Stores username+email
    String title; // Store message title
    String body; // Store message body

    Context context; // Application context

    LocalTime now; // Local machine time

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_page);

        /**
         * The first part of onCreate handles the encrypted shared preferences which handle the
         * Login credentials. It also contains the functionality for recyclerViewOne (shopping list)
         * and recyclerView2 (To-Do list)
         */

        context = getApplicationContext();
        logOut = (Button) findViewById(R.id.btnLogOut);
        delete = (Button) findViewById(R.id.btnDelete);
        recyclerViewOne = (RecyclerView) findViewById(R.id.shoppingRecyclerView);
        recyclerViewTwo = (RecyclerView) findViewById(R.id.toDoRecyclerView);
        ervArrayListOne = populateList();
        ervArrayListTwo = populateList();
        customAdapterOne = new CustomAdapter(this, ervArrayListOne);
        customAdapterTwo = new CustomAdapter(this, ervArrayListTwo);
        displayInfo = (TextView) findViewById(R.id.textView3);

        String masterKeyAlias = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC); // Encrypted key
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        String finalMasterKeyAlias = masterKeyAlias;

        SharedPreferences encryptedPreferences = null;
        try {
            // Creation of Encrypted Preference for login credentials.
            encryptedPreferences = EncryptedSharedPreferences.create (
                    "encrypted_credentials",
                    finalMasterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        assert encryptedPreferences != null;
        display = encryptedPreferences.getString("user_display", ""); // Current user
        displayInfo.setText(display); // Display logged in user

        recyclerViewOne.setAdapter(customAdapterOne);
        recyclerViewTwo.setAdapter(customAdapterTwo);
        recyclerViewOne.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false)); // Shopping Recycler view
        recyclerViewTwo.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false)); // To-do recycler view


        /**
         * The second part of onCreate handles the functionality for the temporary note in
         * constraintLayout3
         */

        createNote = (Button) findViewById(R.id.btnCreate);
        etTimeHour = (EditText) findViewById(R.id.etTimeHour);
        etTimeMin = (EditText) findViewById(R.id.etTimeMin);
        etTitle = (EditText) findViewById(R.id.etTitle);
        etBody = (EditText) findViewById(R.id.etBody);
        tvCount = (TextView) findViewById(R.id.tvCountDown);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvBody = (TextView) findViewById(R.id.tvBody);
        clock = (TextClock) findViewById(R.id.textClock);

        // Shared preferences for temporary note
        spTimedNote = getSharedPreferences("TimedNote", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spTimedNote.edit();

        now = LocalTime.now();
        createNote.setOnClickListener(new View.OnClickListener() {
            /**
             * This onClickListener once clicked, will store the 'burn' time (refTime) of the note
             *  in shared preference, to save state between activities and compute a variable millis
             *  which converts the time till the note is destroyed into milliseconds.
             *  It will also start a CountDownTimer(milliseconds, timeInterval) with millis and
             *  1000 (1 second).
             */

            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                int timeHr = Integer.parseInt(etTimeHour.getText().toString());
                int timeMin = Integer.parseInt(etTimeMin.getText().toString());
                LocalTime refTime; // Time that note will be destroyed
                String localHr; // Local time hour
                String localMin; // Local time minute

                int millis = 0;
                if (timeHr < 24 && timeHr > 0 && timeMin > 0 && timeMin < 60){
                    refTime = LocalTime.of(timeHr, timeMin); // LocalTime.of(13, 24) = 13:24
                    String[] parts = now.toString().split(":");
                    localHr = parts[0];
                    localMin = parts[1];
                    if (timeHr > Integer.parseInt(localHr) && timeMin > Integer.parseInt(localMin))
                    {
                        millis = (timeHr - Integer.parseInt(localHr))*3600000 +
                                (timeMin - Integer.parseInt(localMin))*60000;
                    }
                    if (timeHr < Integer.parseInt(localHr)) {
                        Toast.makeText(context, "Invalid Time, try again",
                                Toast.LENGTH_LONG).show();
                        tvCount.setText("");
                        tvBody.setText("");
                        tvTitle.setText("");
                        editor.remove("EndTime");
                        editor.remove("Title");
                        editor.remove("Body");
                        editor.apply();
                    }
                    if (timeHr > Integer.parseInt(localHr) && timeMin < Integer.parseInt(localMin))
                    {
                        millis = (timeHr - Integer.parseInt(localHr))*3600000 -
                                (timeMin - Integer.parseInt(localMin))*60000;
                    }
                    if (timeHr == Integer.parseInt(localHr) && timeMin > Integer.parseInt(localMin))
                    {
                        millis = (timeMin - Integer.parseInt(localMin))*60000;
                    }

                    editor.putString("EndTime", String.valueOf(refTime)); // Store in shared pref
                    editor.putString("Title", etTitle.getText().toString());
                    editor.putString("Body", etBody.getText().toString());
                    editor.apply();

                    endTime = spTimedNote.getString("EndTime", "0:0:0");
                    tvCount.setText(endTime);
                    tvBody.setText(etBody.getText().toString());
                    tvTitle.setText(etTitle.getText().toString());

                    new CountDownTimer((millis), 1000) {
                        public void onTick(long millisUntilFinished) {} // Do nothing
                        public void onFinish() {
                            Toast.makeText(context, "Time Up! Note Destroyed.",
                                    Toast.LENGTH_LONG).show();
                            tvCount.setText("");
                            tvBody.setText("");
                            tvTitle.setText("");
                            editor.remove("EndTime"); // Clear Shared Preferences
                            editor.remove("Title");
                            editor.remove("Body");
                            editor.apply();
                        }
                    }.start();

                } else {
                    Toast.makeText(context, "Invalid Time, try again",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            /**
             * This on click listener will reset all shared preferences and text views regarding
             * temp note once delete is clicked.
             */
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

         title = spTimedNote.getString("Title", "");
         body = spTimedNote.getString("Body", "");
         // Default value matches required input for LocalTime
         endTime = spTimedNote.getString("EndTime", "0:0:0");
         tvCount.setText(endTime);
         tvTitle.setText(title);
         tvBody.setText(body);

         if (endTime.equals("0:0:0")) { // If no time set
             tvCount.setText("");
             tvBody.setText("");
             tvTitle.setText("");
             Toast.makeText(this, "Timer up, Note destroyed!",
                     Toast.LENGTH_LONG).show();
         } else {
             if (now.isAfter(LocalTime.parse(endTime))) { // If end time has passed local time
                 tvCount.setText("");
                 tvBody.setText("");
                 tvTitle.setText("");
                 editor.remove("EndTime");
                 editor.remove("Title");
                 editor.remove("Body");
                 editor.apply();
             }
         }

        logOut.setOnClickListener(new View.OnClickListener() {
            /**
             * Once clicked, logOut will change activities via implicit intent
             */
            @Override
            public void onClick(View v) {
                Intent homeScreen = new Intent(NotePage.this, MainActivity.class);
                startActivity(homeScreen); // Start Main Activity
                finish(); // Calls method onDestroy() on activity
                Toast.makeText(context, "Goodbye!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private ArrayList<EditRecyclerView> populateList() {
        /**
         * Populates each element of each recycler view with Item + index i
         * @return list - list of items
         */
        ArrayList<EditRecyclerView> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            EditRecyclerView editRecyclerView = new EditRecyclerView();
            editRecyclerView.setEditTextValue("Item " + (i+1));
            list.add(editRecyclerView);
        }
        return list;
    }
}

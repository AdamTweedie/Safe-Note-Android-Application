package com.example.safe_note;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Email extends AppCompatActivity {

    private EditText emailTo, emailSubject, emailMessage;
    private Button send;
    private ImageButton undoPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_email);

        emailTo = (EditText) findViewById(R.id.etRecipitantEmail);
        emailSubject = (EditText) findViewById(R.id.etNewEmailSubject);
        emailMessage = (EditText) findViewById(R.id.etNewEmailMessage);
        send = (Button) findViewById(R.id.btnSendEmail);
        undoPage = (ImageButton) findViewById((R.id.btnBackToNotePage));
        Context context = getApplicationContext();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailTo.getText().toString()});
                intent.putExtra(Intent.EXTRA_SUBJECT, new String[]{emailSubject.getText().toString()});
                intent.putExtra(Intent.EXTRA_TEXT, new String[]{emailMessage.getText().toString()});
                intent.setType("message/rfc822");
                startActivity(Intent.createChooser(intent, "Choose Mail App"));
            }
        });

        undoPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent undoPage = new Intent(Email.this, NotePage.class);
                startActivity(undoPage);
                finish();
                Toast.makeText(context, "Email sent", Toast.LENGTH_LONG).show();
            }
        });
    }
}

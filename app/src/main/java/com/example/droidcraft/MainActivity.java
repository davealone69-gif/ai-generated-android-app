package com.example.droidcraft;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText inputEmail;
    private EditText inputKey;
    private Button btnConnect;
    private TextView titleHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputEmail = findViewById(R.id.inputEmail);
        inputKey = findViewById(R.id.inputKey);
        btnConnect = findViewById(R.id.btnConnect);
        titleHeader = findViewById(R.id.titleHeader);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                String key = inputKey.getText().toString();

                if (email.isEmpty() || key.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Authentication values cannot be empty!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Access Granted! Logging in as: " + email, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
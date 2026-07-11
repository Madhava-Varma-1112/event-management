package com.example.eventmanagementapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.eventmanagementapp.database.DatsbaseHelper;

import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etUsername, etResetCode, etNewPassword;
    private RadioGroup rgRole;
    private Button btnSendCode, btnResetPassword;
    private LinearLayout layoutStep1, layoutStep2;
    private TextView tvBackToLogin;

    private DatsbaseHelper dbHelper;
    private int generatedCode;
    private String currentResetUsername;
    private String currentResetRole;

    private static final String CHANNEL_ID = "password_reset_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        dbHelper = DatsbaseHelper.getInstance(this);

        etUsername = findViewById(R.id.etForgotUsername);
        rgRole = findViewById(R.id.rgForgotRole);
        btnSendCode = findViewById(R.id.btnSendCode);

        layoutStep1 = findViewById(R.id.layoutStep1);
        layoutStep2 = findViewById(R.id.layoutStep2);

        etResetCode = findViewById(R.id.etResetCode);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnSendCode.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedId = rgRole.getCheckedRadioButtonId();
            String role = "user";
            if (selectedId == R.id.rbForgotOrganizer) role = "organizer";
            else if (selectedId == R.id.rbForgotAdmin) role = "admin";

            if (dbHelper.userExists(username, role)) {
                currentResetUsername = username;
                currentResetRole = role;
                generateAndSendCode();
                layoutStep1.setVisibility(View.GONE);
                layoutStep2.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "User not found with this role", Toast.LENGTH_SHORT).show();
            }
        });

        btnResetPassword.setOnClickListener(v -> {
            String inputCodeStr = etResetCode.getText().toString().trim();
            String newPass = etNewPassword.getText().toString().trim();

            if (inputCodeStr.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int inputCode = Integer.parseInt(inputCodeStr);
            if (inputCode == generatedCode) {
                if (dbHelper.updatePassword(currentResetUsername, currentResetRole, newPass)) {
                    Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Error updating password", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Invalid reset code", Toast.LENGTH_SHORT).show();
            }
        });

        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void generateAndSendCode() {
        Random random = new Random();
        generatedCode = 1000 + random.nextInt(9000); // 4 digit code

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Password Reset", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Password Reset Code")
                .setContentText("Your 4-digit reset code for " + currentResetUsername + " is: " + generatedCode)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(101, builder.build());
        Toast.makeText(this, "Code sent to your notifications", Toast.LENGTH_LONG).show();
    }
}

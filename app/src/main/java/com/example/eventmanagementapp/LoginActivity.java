package com.example.eventmanagementapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventmanagementapp.database.DatsbaseHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private RadioGroup rgLoginRole;
    private Button btnLogin;
    private TextView tvRegisterLink, tvForgotPassword;
    private DatsbaseHelper dbHelper;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = DatsbaseHelper.getInstance(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        rgLoginRole = findViewById(R.id.rgLoginRole);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter credentials", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedId = rgLoginRole.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
                return;
            }
            
            RadioButton rb = findViewById(selectedId);
            String role;
            String rbText = rb.getText().toString();
            if (rbText.equalsIgnoreCase("Admin")) {
                role = "admin";
            } else if (rbText.equalsIgnoreCase("Organizer")) {
                role = "organizer";
            } else {
                role = "user";
            }

            if (dbHelper.validateLogin(user, pass, role)) {
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", user);
                editor.putString("role", role);
                editor.apply();

                Toast.makeText(this, "Login Successful as " + role, Toast.LENGTH_SHORT).show();
                
                Intent intent;
                if (role.equals("admin")) {
                    intent = new Intent(LoginActivity.this, AdminActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, HomeActivity.class);
                }
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid Username, Password or Role", Toast.LENGTH_SHORT).show();
            }
        });

        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Double tap to exit logic
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finish();
                    return;
                }

                doubleBackToExitPressedOnce = true;
                Toast.makeText(LoginActivity.this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
            }
        });
    }
}

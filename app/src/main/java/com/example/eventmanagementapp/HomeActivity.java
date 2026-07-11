package com.example.eventmanagementapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventmanagementapp.database.DatsbaseHelper;
import com.example.eventmanagementapp.models.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private DatsbaseHelper dbHelper;
    private FloatingActionButton fabAddEvent;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerViewEvents);
        fabAddEvent = findViewById(R.id.fabAddEvent);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = DatsbaseHelper.getInstance(this);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", null);
        String role = sharedPreferences.getString("role", "user");

        if (role.equals("organizer") || role.equals("admin")) {
            fabAddEvent.setVisibility(View.VISIBLE);
        } else {
            fabAddEvent.setVisibility(View.GONE);
        }

        loadEvents();

        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddEventActivity.class);
            startActivity(intent);
        });

        // Smart Back Navigation
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitDialog();
            }
        });
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Would you like to logout or just close the app?")
                .setPositiveButton("Logout", (dialog, which) -> logoutWithConfirmation())
                .setNegativeButton("Close App", (dialog, which) -> finishAffinity())
                .setNeutralButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logoutWithConfirmation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutWithConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logout())
                .setNegativeButton("No", null)
                .show();
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().remove("username").remove("role").apply();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    private void loadEvents() {
        List<Event> approvedEvents = dbHelper.getEventsByStatus(1);
        if (adapter == null) {
            adapter = new EventAdapter(approvedEvents, currentUsername);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(approvedEvents);
        }
    }
}

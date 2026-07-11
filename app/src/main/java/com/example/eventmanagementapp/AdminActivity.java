package com.example.eventmanagementapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventmanagementapp.database.DatsbaseHelper;
import com.example.eventmanagementapp.models.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPending, recyclerViewDelete, recyclerViewUpdates;
    private EventAdapter pendingAdapter, deleteAdapter;
    private UpdateAdapter updatesAdapter;
    private DatsbaseHelper dbHelper;
    private TextView tvNoPending, tvNoDelete, tvNoUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = DatsbaseHelper.getInstance(this);
        
        tvNoPending = findViewById(R.id.tvNoPending);
        recyclerViewPending = findViewById(R.id.recyclerViewPendingEvents);
        recyclerViewPending.setLayoutManager(new LinearLayoutManager(this));

        tvNoDelete = findViewById(R.id.tvNoDelete); 
        recyclerViewDelete = findViewById(R.id.recyclerViewDeleteRequests);
        recyclerViewDelete.setLayoutManager(new LinearLayoutManager(this));

        tvNoUpdates = findViewById(R.id.tvNoUpdates);
        recyclerViewUpdates = findViewById(R.id.recyclerViewUpdateRequests);
        recyclerViewUpdates.setLayoutManager(new LinearLayoutManager(this));

        loadData();

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

    private void loadData() {
        loadPendingEvents();
        loadDeleteRequests();
        loadUpdateRequests();
    }

    private void loadPendingEvents() {
        List<Event> pendingEvents = dbHelper.getEventsByStatus(0);
        if (pendingEvents.isEmpty()) {
            tvNoPending.setVisibility(View.VISIBLE);
            recyclerViewPending.setVisibility(View.GONE);
        } else {
            tvNoPending.setVisibility(View.GONE);
            recyclerViewPending.setVisibility(View.VISIBLE);
        }
        pendingAdapter = new EventAdapter(pendingEvents, true, new EventAdapter.OnEventActionListener() {
            @Override
            public void onApprove(Event event) {
                dbHelper.approveEvent(event.getId());
                Toast.makeText(AdminActivity.this, "Event Approved", Toast.LENGTH_SHORT).show();
                scheduleReminder(event);
                loadData();
            }
            @Override
            public void onReject(Event event) {
                dbHelper.deleteEvent(event.getId());
                Toast.makeText(AdminActivity.this, "Event Rejected", Toast.LENGTH_SHORT).show();
                loadData();
            }
        });
        recyclerViewPending.setAdapter(pendingAdapter);
    }

    private void loadDeleteRequests() {
        List<Event> deleteRequests = dbHelper.getDeleteRequests();
        if (deleteRequests.isEmpty()) {
            tvNoDelete.setVisibility(View.VISIBLE);
            recyclerViewDelete.setVisibility(View.GONE);
        } else {
            tvNoDelete.setVisibility(View.GONE);
            recyclerViewDelete.setVisibility(View.VISIBLE);
        }
        deleteAdapter = new EventAdapter(deleteRequests, true, new EventAdapter.OnEventActionListener() {
            @Override
            public void onApprove(Event event) {
                dbHelper.deleteEvent(event.getId());
                Toast.makeText(AdminActivity.this, "Event Deleted", Toast.LENGTH_SHORT).show();
                loadData();
            }
            @Override
            public void onReject(Event event) {
                dbHelper.cancelDeleteRequest(event.getId());
                Toast.makeText(AdminActivity.this, "Delete Request Canceled", Toast.LENGTH_SHORT).show();
                loadData();
            }
        });
        recyclerViewDelete.setAdapter(deleteAdapter);
    }

    private void loadUpdateRequests() {
        Cursor cursor = dbHelper.getUpdateRequests();
        if (cursor == null || cursor.getCount() == 0) {
            if (cursor != null) cursor.close();
            tvNoUpdates.setVisibility(View.VISIBLE);
            recyclerViewUpdates.setVisibility(View.GONE);
        } else {
            tvNoUpdates.setVisibility(View.GONE);
            recyclerViewUpdates.setVisibility(View.VISIBLE);
            if (updatesAdapter == null) {
                updatesAdapter = new UpdateAdapter(cursor, new UpdateAdapter.OnUpdateActionListener() {
                    @Override
                    public void onApprove(int updateId) {
                        dbHelper.approveEventUpdate(updateId);
                        Toast.makeText(AdminActivity.this, "Update Approved", Toast.LENGTH_SHORT).show();
                        loadData();
                    }
                    @Override
                    public void onReject(int updateId) {
                        dbHelper.rejectEventUpdate(updateId);
                        Toast.makeText(AdminActivity.this, "Update Rejected", Toast.LENGTH_SHORT).show();
                        loadData();
                    }
                });
                recyclerViewUpdates.setAdapter(updatesAdapter);
            } else {
                updatesAdapter.swapCursor(cursor);
            }
        }
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
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void scheduleReminder(Event event) {
        try {
            String dateTimeStr = event.getDate() + " " + event.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy H:mm", Locale.getDefault());
            Date date = sdf.parse(dateTimeStr);
            if (date != null) {
                NotificationUtils.scheduleEventReminder(this, event.getTitle(), "Starting soon!", date.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}

package com.example.eventmanagementapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventmanagementapp.database.DatsbaseHelper;
import com.example.eventmanagementapp.models.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventDetailsActivity extends AppCompatActivity {

    private TextView tvTitle, tvCategory, tvDateTime, tvVenue, tvDesc, tvRewards, tvOrganizer, tvFaculty, labelRewards, tvInterestCount;
    private Button btnContact, btnWhatsapp, btnRequestDelete, btnInterested, btnNotInterested, btnSetReminder, btnUpdateEvent;
    private DatsbaseHelper dbHelper;
    private String currentUser, currentRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        dbHelper = DatsbaseHelper.getInstance(this);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        currentUser = prefs.getString("username", "");
        currentRole = prefs.getString("role", "");

        tvTitle = findViewById(R.id.tvDetailTitle);
        tvCategory = findViewById(R.id.tvDetailCategory);
        tvDateTime = findViewById(R.id.tvDetailDateTime);
        tvVenue = findViewById(R.id.tvDetailVenue);
        tvDesc = findViewById(R.id.tvDetailDesc);
        tvRewards = findViewById(R.id.tvDetailRewards);
        labelRewards = findViewById(R.id.labelRewards);
        tvOrganizer = findViewById(R.id.tvDetailOrganizer);
        tvFaculty = findViewById(R.id.tvDetailFaculty);
        tvInterestCount = findViewById(R.id.tvInterestCount);

        btnContact = findViewById(R.id.btnContact);
        btnWhatsapp = findViewById(R.id.btnWhatsapp);
        btnRequestDelete = findViewById(R.id.btnRequestDelete);
        btnInterested = findViewById(R.id.btnInterested);
        btnNotInterested = findViewById(R.id.btnNotInterested);
        btnSetReminder = findViewById(R.id.btnSetReminder);
        btnUpdateEvent = findViewById(R.id.btnUpdateEvent);

        Event event = (Event) getIntent().getSerializableExtra("event");

        if (event != null) {
            displayDetails(event);
            setupInterests(event);
            setupOrganizerActions(event);
            setupReminders(event);
        }
    }

    private void displayDetails(Event event) {
        tvTitle.setText(event.getTitle());
        tvCategory.setText(event.getCategory());
        tvDateTime.setText(event.getDate() + " | " + event.getTime());
        tvVenue.setText(event.getVenue());
        tvDesc.setText(event.getDescription());
        tvOrganizer.setText("Organizer: " + event.getOrganizerName());
        tvFaculty.setText("Faculty Coordinator: " + event.getFacultyCoordinator());

        if (event.getRewards() == null || event.getRewards().isEmpty()) {
            tvRewards.setVisibility(View.GONE);
            labelRewards.setVisibility(View.GONE);
        } else {
            tvRewards.setText(event.getRewards());
        }

        btnContact.setOnClickListener(v -> {
            String contact = event.getContact();
            if (contact.contains("@")) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + contact));
                startActivity(intent);
            } else {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + contact));
                startActivity(intent);
            }
        });

        if (event.getWhatsapp() != null && !event.getWhatsapp().isEmpty()) {
            btnWhatsapp.setVisibility(View.VISIBLE);
            btnWhatsapp.setOnClickListener(v -> {
                String url = event.getWhatsapp();
                if (!url.startsWith("http")) url = "https://wa.me/" + url;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            });
        }
    }

    private void setupInterests(Event event) {
        refreshInterestUI(event.getId());

        if ("user".equals(currentRole)) {
            findViewById(R.id.layoutUserActions).setVisibility(View.VISIBLE);
            btnInterested.setOnClickListener(v -> {
                dbHelper.setInterest(event.getId(), currentUser, true);
                refreshInterestUI(event.getId());
                btnSetReminder.setVisibility(View.VISIBLE);
            });
            btnNotInterested.setOnClickListener(v -> {
                dbHelper.setInterest(event.getId(), currentUser, false);
                refreshInterestUI(event.getId());
                btnSetReminder.setVisibility(View.GONE);
            });

            if (dbHelper.isUserInterested(event.getId(), currentUser)) {
                btnSetReminder.setVisibility(View.VISIBLE);
            }
        } else {
            findViewById(R.id.layoutUserActions).setVisibility(View.GONE);
        }
    }

    private void refreshInterestUI(int eventId) {
        int count = dbHelper.getInterestCount(eventId);
        tvInterestCount.setText(count + " Interested");
    }

    private void setupOrganizerActions(Event event) {
        if ("organizer".equals(currentRole) && event.getCreatorUsername() != null && event.getCreatorUsername().equals(currentUser)) {
            // Delete Request
            btnRequestDelete.setVisibility(View.VISIBLE);
            if (event.getIsDeleteRequested() == 1) {
                btnRequestDelete.setEnabled(false);
                btnRequestDelete.setText("Deletion Pending Approval");
            } else {
                btnRequestDelete.setOnClickListener(v -> {
                    dbHelper.requestDelete(event.getId());
                    Toast.makeText(this, "Deletion request sent", Toast.LENGTH_SHORT).show();
                    btnRequestDelete.setEnabled(false);
                    btnRequestDelete.setText("Deletion Pending");
                });
            }

            // Update Request
            btnUpdateEvent.setVisibility(View.VISIBLE);
            btnUpdateEvent.setOnClickListener(v -> showUpdateDialog(event));
        } else {
            btnRequestDelete.setVisibility(View.GONE);
            btnUpdateEvent.setVisibility(View.GONE);
        }
    }

    private void showUpdateDialog(Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_event, null);
        builder.setView(dialogView);

        EditText etDate = dialogView.findViewById(R.id.etNewDate);
        EditText etTime = dialogView.findViewById(R.id.etNewTime);
        EditText etVenue = dialogView.findViewById(R.id.etNewVenue);

        etDate.setText(event.getDate());
        etTime.setText(event.getTime());
        etVenue.setText(event.getVenue());

        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> 
                etDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year), 
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        etTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view, hourOfDay, minute) -> 
                etTime.setText(hourOfDay + ":" + String.format(Locale.getDefault(), "%02d", minute)), 
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        builder.setTitle("Request Update")
                .setPositiveButton("Submit Request", (dialog, which) -> {
                    String newDate = etDate.getText().toString();
                    String newTime = etTime.getText().toString();
                    String newVenue = etVenue.getText().toString();
                    dbHelper.requestUpdate(event.getId(), newDate, newTime, newVenue);
                    Toast.makeText(this, "Update request sent to Admin", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupReminders(Event event) {
        btnSetReminder.setOnClickListener(v -> {
            String[] options = {"10 Minutes Before", "30 Minutes Before", "1 Hour Before", "Custom Time"};
            new AlertDialog.Builder(this)
                    .setTitle("Set Reminder for " + event.getTitle())
                    .setItems(options, (dialog, which) -> {
                        if (which == 3) {
                            showCustomTimePicker(event);
                        } else {
                            long offset = 0;
                            if (which == 0) offset = 10 * 60 * 1000;
                            else if (which == 1) offset = 30 * 60 * 1000;
                            else offset = 60 * 60 * 1000;
                            scheduleReminder(event, offset);
                        }
                    }).show();
        });
    }

    private void showCustomTimePicker(Event event) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                Date eventDate = sdf.parse(event.getDate());
                if (eventDate != null) {
                    Calendar triggerCal = Calendar.getInstance();
                    triggerCal.setTime(eventDate);
                    triggerCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    triggerCal.set(Calendar.MINUTE, minute);
                    triggerCal.set(Calendar.SECOND, 0);

                    if (triggerCal.getTimeInMillis() > System.currentTimeMillis()) {
                        NotificationUtils.scheduleCustomReminder(this, event.getTitle(), triggerCal.getTimeInMillis());
                        Toast.makeText(this, "Reminder for '" + event.getTitle() + "' set!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Selected time is in the past!", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void scheduleReminder(Event event, long offsetMillis) {
        try {
            String dateTimeStr = event.getDate() + " " + event.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy H:mm", Locale.getDefault());
            Date date = sdf.parse(dateTimeStr);
            if (date != null) {
                long triggerTime = date.getTime() - offsetMillis;
                if (triggerTime > System.currentTimeMillis()) {
                    NotificationUtils.scheduleCustomReminder(this, event.getTitle(), triggerTime);
                    Toast.makeText(this, "Reminder for '" + event.getTitle() + "' set!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Selected reminder time has already passed!", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}

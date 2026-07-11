package com.example.eventmanagementapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventmanagementapp.database.DatsbaseHelper;
import com.example.eventmanagementapp.models.Event;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

public class AddEventActivity extends AppCompatActivity {

    private EditText etTitle, etOrganizer, etFaculty, etDescription, etVenue, etRewards, etContact, etWhatsapp, etOtherCategory;
    private TextInputLayout tilOtherCategory;
    private AutoCompleteTextView spinnerCategory;
    private Button btnPickDate, btnPickTime, btnSaveEvent;
    private TextView tvSelectedDateTime;

    private String selectedDate = "";
    private String selectedTime = "";

    private int year, month, day, hour, minute;

    private DatsbaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        databaseHelper = DatsbaseHelper.getInstance(this);

        // Initialize UI components
        etTitle = findViewById(R.id.etEventTitle);
        etVenue = findViewById(R.id.etVenue);
        etOrganizer = findViewById(R.id.etOrganizerName);
        etFaculty = findViewById(R.id.etFacultyCoordinator);
        etContact = findViewById(R.id.etContact);
        etWhatsapp = findViewById(R.id.etWhatsapp);
        etRewards = findViewById(R.id.etRewards);
        etDescription = findViewById(R.id.etDescription);
        etOtherCategory = findViewById(R.id.etOtherCategory);
        tilOtherCategory = findViewById(R.id.tilOtherCategory);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);
        tvSelectedDateTime = findViewById(R.id.tvSelectedDateTime);

        // Setup Dropdown categories
        String[] categories = {"Seminar", "Workshop", "Cultural", "Sports", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);
        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
            String selection = (String) parent.getItemAtPosition(position);
            if (selection.equals("Other")) {
                tilOtherCategory.setVisibility(View.VISIBLE);
            } else {
                tilOtherCategory.setVisibility(View.GONE);
            }
        });

        final Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        // Date Picker logic
        btnPickDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(AddEventActivity.this,
                    (view, year1, month1, dayOfMonth) -> {
                        year = year1;
                        month = month1;
                        day = dayOfMonth;
                        selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                        updateDateTimeText();
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Time Picker logic
        btnPickTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(AddEventActivity.this,
                    (view, hourOfDay, minute1) -> {
                        hour = hourOfDay;
                        minute = minute1;
                        selectedTime = hourOfDay + ":" + String.format(Locale.getDefault(), "%02d", minute1);
                        updateDateTimeText();
                    }, hour, minute, true);
            timePickerDialog.show();
        });

        // Save Event logic
        btnSaveEvent.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String venue = etVenue.getText().toString().trim();
            String organizer = etOrganizer.getText().toString().trim();
            String faculty = etFaculty.getText().toString().trim();
            String contact = etContact.getText().toString().trim();
            String whatsapp = etWhatsapp.getText().toString().trim();
            String rewards = etRewards.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();
            
            String category = spinnerCategory.getText().toString();
            if (category.equals("Other")) {
                category = etOtherCategory.getText().toString().trim();
                if (category.isEmpty()) category = "Other";
            }

            if (title.isEmpty() || venue.isEmpty() || organizer.isEmpty() || contact.isEmpty() || desc.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty() || category.isEmpty()) {
                Toast.makeText(AddEventActivity.this, "Please fill all required fields (*)", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String currentUser = sharedPreferences.getString("username", "unknown");

            Event newEvent = new Event(title, selectedDate, selectedTime, category, desc, organizer, faculty, 0, venue, rewards, contact, whatsapp, currentUser, 0);

            long result = databaseHelper.addEvent(newEvent);

            if (result != -1) {
                Toast.makeText(AddEventActivity.this, "Event Submitted for Admin Approval!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(AddEventActivity.this, "Error saving event.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDateTimeText() {
        tvSelectedDateTime.setText("Date: " + (selectedDate.isEmpty() ? "Not set" : selectedDate) +
                " | Time: " + (selectedTime.isEmpty() ? "Not set" : selectedTime));
    }
}

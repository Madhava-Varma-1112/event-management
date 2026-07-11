package com.example.eventmanagementapp.models;

import java.io.Serializable;

public class Event implements Serializable {
    private int id;
    private String title;
    private String date;
    private String time;
    private String category;
    private String description;
    private String organizerName;
    private String facultyCoordinator;
    private int isApproved; // 0 = Pending, 1 = Approved
    private String venue;
    private String rewards;
    private String contact;
    private String whatsapp;
    private String creatorUsername;
    private int isDeleteRequested; // 0 = No, 1 = Requested

    public Event(String title, String date, String time, String category, String description,
                 String organizerName, String facultyCoordinator, int isApproved,
                 String venue, String rewards, String contact, String whatsapp, 
                 String creatorUsername, int isDeleteRequested) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.category = category;
        this.description = description;
        this.organizerName = organizerName;
        this.facultyCoordinator = facultyCoordinator;
        this.isApproved = isApproved;
        this.venue = venue;
        this.rewards = rewards;
        this.contact = contact;
        this.whatsapp = whatsapp;
        this.creatorUsername = creatorUsername;
        this.isDeleteRequested = isDeleteRequested;
    }

    public Event(int id, String title, String date, String time, String category,
                 String description, String organizerName, String facultyCoordinator, int isApproved,
                 String venue, String rewards, String contact, String whatsapp,
                 String creatorUsername, int isDeleteRequested) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.time = time;
        this.category = category;
        this.description = description;
        this.organizerName = organizerName;
        this.facultyCoordinator = facultyCoordinator;
        this.isApproved = isApproved;
        this.venue = venue;
        this.rewards = rewards;
        this.contact = contact;
        this.whatsapp = whatsapp;
        this.creatorUsername = creatorUsername;
        this.isDeleteRequested = isDeleteRequested;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getOrganizerName() { return organizerName; }
    public String getFacultyCoordinator() { return facultyCoordinator; }
    public int getIsApproved() { return isApproved; }
    public String getVenue() { return venue; }
    public String getRewards() { return rewards; }
    public String getContact() { return contact; }
    public String getWhatsapp() { return whatsapp; }
    public String getCreatorUsername() { return creatorUsername; }
    public int getIsDeleteRequested() { return isDeleteRequested; }

    public void setId(int id) { this.id = id; }
    public void setIsApproved(int isApproved) { this.isApproved = isApproved; }
    public void setIsDeleteRequested(int isDeleteRequested) { this.isDeleteRequested = isDeleteRequested; }
}

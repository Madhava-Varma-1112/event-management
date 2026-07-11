package com.example.eventmanagementapp.database;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.StrictMode;

import com.example.eventmanagementapp.models.Event;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DatsbaseHelper extends SQLiteOpenHelper {

    private static DatsbaseHelper instance;

    private static final String DATABASE_NAME = "EventManager_v35.db";
    private static final int DATABASE_VERSION = 1;

    // Production Render Backend URL
    private static final String BASE_URL = "https://event-management-mu5b.onrender.com";

    public static synchronized DatsbaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatsbaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatsbaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // Relax strict mode thread policy to allow synchronous HTTP requests on the main thread
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // No longer creating local SQLite tables since we use remote MongoDB
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No longer managing local SQLite upgrades
    }

    // Generic HTTP Request Handler
    private String sendRequest(String endpoint, String method, String jsonBody) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(BASE_URL + endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            if (jsonBody != null && (method.equals("POST") || method.equals("PUT") || method.equals("PATCH") || method.equals("DELETE"))) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
            }

            int code = conn.getResponseCode();
            if (code >= 200 && code < 300) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return response.toString();
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public boolean validateLogin(String username, String password, String role) {
        try {
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", password);
            body.put("role", role);
            String response = sendRequest("/api/users/login", "POST", body.toString());
            if (response != null) {
                JSONObject resObj = new JSONObject(response);
                return resObj.optBoolean("success", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean userExists(String username, String role) {
        try {
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("role", role);
            String response = sendRequest("/api/users/exists", "POST", body.toString());
            if (response != null) {
                JSONObject resObj = new JSONObject(response);
                return resObj.optBoolean("exists", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePassword(String username, String role, String newPassword) {
        try {
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("role", role);
            body.put("newPassword", newPassword);
            String response = sendRequest("/api/users/update-password", "POST", body.toString());
            if (response != null) {
                JSONObject resObj = new JSONObject(response);
                return resObj.optBoolean("success", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean registerUser(String username, String password, String role) {
        try {
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", password);
            body.put("role", role);
            String response = sendRequest("/api/users/register", "POST", body.toString());
            if (response != null) {
                JSONObject resObj = new JSONObject(response);
                return resObj.optBoolean("success", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public long addEvent(Event event) {
        try {
            JSONObject body = new JSONObject();
            body.put("title", event.getTitle());
            body.put("date", event.getDate());
            body.put("time", event.getTime());
            body.put("category", event.getCategory());
            body.put("description", event.getDescription());
            body.put("organizerName", event.getOrganizerName());
            body.put("facultyCoordinator", event.getFacultyCoordinator());
            body.put("isApproved", event.getIsApproved());
            body.put("venue", event.getVenue());
            body.put("rewards", event.getRewards());
            body.put("contact", event.getContact());
            body.put("whatsapp", event.getWhatsapp());
            body.put("creatorUsername", event.getCreatorUsername());
            body.put("isDeleteRequested", event.getIsDeleteRequested());

            String response = sendRequest("/api/events", "POST", body.toString());
            if (response != null) {
                JSONObject resObj = new JSONObject(response);
                return resObj.optLong("id", -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<Event> getEventsByStatus(int approvalStatus) {
        List<Event> eventList = new ArrayList<>();
        try {
            String response = sendRequest("/api/events/status/" + approvalStatus, "GET", null);
            if (response != null) {
                JSONArray array = new JSONArray(response);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    eventList.add(new Event(
                            obj.getInt("id"),
                            obj.getString("title"),
                            obj.getString("date"),
                            obj.getString("time"),
                            obj.getString("category"),
                            obj.getString("description"),
                            obj.getString("organizerName"),
                            obj.getString("facultyCoordinator"),
                            obj.getInt("isApproved"),
                            obj.getString("venue"),
                            obj.getString("rewards"),
                            obj.getString("contact"),
                            obj.getString("whatsapp"),
                            obj.getString("creatorUsername"),
                            obj.getInt("isDeleteRequested")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return eventList;
    }

    public void approveEvent(int eventId) {
        sendRequest("/api/events/" + eventId + "/approve", "POST", "{}");
    }

    public void deleteEvent(int eventId) {
        sendRequest("/api/events/" + eventId, "DELETE", "{}");
    }

    public void requestDelete(int eventId) {
        sendRequest("/api/events/" + eventId + "/request-delete", "POST", "{}");
    }

    public void cancelDeleteRequest(int eventId) {
        sendRequest("/api/events/" + eventId + "/cancel-delete-request", "POST", "{}");
    }

    public List<Event> getDeleteRequests() {
        List<Event> eventList = new ArrayList<>();
        try {
            String response = sendRequest("/api/events/delete-requests", "GET", null);
            if (response != null) {
                JSONArray array = new JSONArray(response);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    eventList.add(new Event(
                            obj.getInt("id"),
                            obj.getString("title"),
                            obj.getString("date"),
                            obj.getString("time"),
                            obj.getString("category"),
                            obj.getString("description"),
                            obj.getString("organizerName"),
                            obj.getString("facultyCoordinator"),
                            obj.getInt("isApproved"),
                            obj.getString("venue"),
                            obj.getString("rewards"),
                            obj.getString("contact"),
                            obj.getString("whatsapp"),
                            obj.getString("creatorUsername"),
                            obj.getInt("isDeleteRequested")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return eventList;
    }

    public void requestUpdate(int eventId, String date, String time, String venue) {
        try {
            JSONObject body = new JSONObject();
            body.put("date", date);
            body.put("time", time);
            body.put("venue", venue);
            sendRequest("/api/events/" + eventId + "/request-update", "POST", body.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Cursor getUpdateRequests() {
        String[] columns = {"up_event_id", "new_date", "new_time", "new_venue", "title"};
        MatrixCursor matrixCursor = new MatrixCursor(columns);
        try {
            String response = sendRequest("/api/events/update-requests", "GET", null);
            if (response != null) {
                JSONArray array = new JSONArray(response);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    matrixCursor.addRow(new Object[]{
                            obj.getInt("up_event_id"),
                            obj.getString("new_date"),
                            obj.getString("new_time"),
                            obj.getString("new_venue"),
                            obj.getString("title")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return matrixCursor;
    }

    public void approveEventUpdate(int eventId) {
        sendRequest("/api/events/" + eventId + "/approve-update", "POST", "{}");
    }

    public void rejectEventUpdate(int eventId) {
        sendRequest("/api/events/" + eventId + "/reject-update", "POST", "{}");
    }

    public boolean isEventUnread(int eventId, String username) {
        try {
            String response = sendRequest("/api/events/" + eventId + "/unread/" + username, "GET", null);
            if (response != null) {
                JSONObject resObj = new JSONObject(response);
                return resObj.optBoolean("unread", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public void markAsRead(int eventId, String username) {
        try {
            JSONObject body = new JSONObject();
            body.put("username", username);
            sendRequest("/api/events/" + eventId + "/mark-read", "POST", body.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setInterest(int eventId, String username, boolean interested) {
        try {
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("interested", interested);
            sendRequest("/api/events/" + eventId + "/interest", "POST", body.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isUserInterested(int eventId, String username) {
        try {
            String response = sendRequest("/api/events/" + eventId + "/interested/" + username, "GET", null);
            if (response != null) {
                JSONObject resObj = new JSONObject(response);
                return resObj.optBoolean("interested", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getInterestCount(int eventId) {
        try {
            String response = sendRequest("/api/events/" + eventId + "/interest-count", "GET", null);
            if (response != null) {
                JSONObject resObj = new JSONObject(response);
                return resObj.optInt("count", 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}

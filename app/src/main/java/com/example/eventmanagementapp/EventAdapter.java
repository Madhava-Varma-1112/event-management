package com.example.eventmanagementapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventmanagementapp.database.DatsbaseHelper;
import com.example.eventmanagementapp.models.Event;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private boolean isAdmin;
    private OnEventActionListener listener;
    private String currentUsername;

    public interface OnEventActionListener {
        void onApprove(Event event);
        void onReject(Event event);
    }

    public EventAdapter(List<Event> eventList, boolean isAdmin, OnEventActionListener listener) {
        this.eventList = eventList;
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    public EventAdapter(List<Event> eventList, String currentUsername) {
        this.eventList = eventList;
        this.isAdmin = false;
        this.listener = null;
        this.currentUsername = currentUsername;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.tvTitle.setText(event.getTitle());
        holder.tvDate.setText(event.getDate());
        holder.tvTime.setText(event.getTime());
        holder.tvCategory.setText(event.getCategory());

        // Indicator Dot for new/updated events
        if (currentUsername != null) {
            DatsbaseHelper db = DatsbaseHelper.getInstance(holder.itemView.getContext());
            if (db.isEventUnread(event.getId(), currentUsername)) {
                holder.viewUnreadDot.setVisibility(View.VISIBLE);
            } else {
                holder.viewUnreadDot.setVisibility(View.GONE);
            }
        } else {
            holder.viewUnreadDot.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (currentUsername != null) {
                DatsbaseHelper db = DatsbaseHelper.getInstance(v.getContext());
                db.markAsRead(event.getId(), currentUsername);
                holder.viewUnreadDot.setVisibility(View.GONE);
            }
            Intent intent = new Intent(v.getContext(), EventDetailsActivity.class);
            intent.putExtra("event", event);
            v.getContext().startActivity(intent);
        });

        if (isAdmin) {
            holder.adminButtons.setVisibility(View.VISIBLE);
            holder.btnApprove.setOnClickListener(v -> {
                if (listener != null) listener.onApprove(event);
            });
            holder.btnReject.setOnClickListener(v -> {
                if (listener != null) listener.onReject(event);
            });
        } else {
            holder.adminButtons.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void updateData(List<Event> newEvents) {
        this.eventList = newEvents;
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvTime, tvCategory;
        LinearLayout adminButtons;
        Button btnApprove, btnReject;
        View viewUnreadDot;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            adminButtons = itemView.findViewById(R.id.adminButtons);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
        }
    }
}

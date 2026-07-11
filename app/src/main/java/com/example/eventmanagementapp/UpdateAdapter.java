package com.example.eventmanagementapp;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UpdateAdapter extends RecyclerView.Adapter<UpdateAdapter.UpdateViewHolder> {

    private Cursor cursor;
    private OnUpdateActionListener listener;

    public interface OnUpdateActionListener {
        void onApprove(int updateId);
        void onReject(int updateId);
    }

    public UpdateAdapter(Cursor cursor, OnUpdateActionListener listener) {
        this.cursor = cursor;
        this.listener = listener;
    }

    public void swapCursor(Cursor newCursor) {
        if (this.cursor != null) {
            this.cursor.close();
        }
        this.cursor = newCursor;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UpdateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_update_request, parent, false);
        return new UpdateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UpdateViewHolder holder, int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            int updateId = cursor.getInt(0);
            String newDate = cursor.getString(1);
            String newTime = cursor.getString(2);
            String newVenue = cursor.getString(3);
            String eventTitle = cursor.getString(4);

            holder.tvTitle.setText("Update for: " + eventTitle);
            holder.tvDetails.setText("New Date: " + newDate + "\nNew Time: " + newTime + "\nNew Venue: " + newVenue);

            holder.btnApprove.setOnClickListener(v -> listener.onApprove(updateId));
            holder.btnReject.setOnClickListener(v -> listener.onReject(updateId));
        }
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    public static class UpdateViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDetails;
        Button btnApprove, btnReject;

        public UpdateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvUpdateEventTitle);
            tvDetails = itemView.findViewById(R.id.tvUpdateDetails);
            btnApprove = itemView.findViewById(R.id.btnApproveUpdate);
            btnReject = itemView.findViewById(R.id.btnRejectUpdate);
        }
    }
}

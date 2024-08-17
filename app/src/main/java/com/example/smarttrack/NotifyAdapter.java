package com.example.smarttrack;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotifyAdapter extends RecyclerView.Adapter<NotifyAdapter.ViewHolder>{
    private List<NotifyModel> dataList;
    private Context context;

    public NotifyAdapter(Context context,List<NotifyModel> dataList) {
        this.context = context;
        this.dataList = dataList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notify_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NotifyModel item = dataList.get(position);

        // Bind data to views
        holder.senderNameTextView.setText(item.getSenderName());
        holder.messageTextView.setText(item.getMessage());



        // Handle item click

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView senderNameTextView, messageTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            senderNameTextView = itemView.findViewById(R.id.senderName);
            messageTextView = itemView.findViewById(R.id.message);

        }
    }
}

package com.example.project;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private ArrayList<HistoryItem> historyList;

    public HistoryAdapter(ArrayList<HistoryItem> historyList) {
        this.historyList = (historyList != null) ? historyList : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        HistoryItem item = historyList.get(position);

        holder.tvTopic.setText(item.getTopic());
        holder.tvScore.setText(item.getScore());
        holder.tvDate.setText(item.getDate());

        // 🔥 กดที่รายการประวัติเพื่อดูเฉลยย้อนหลัง
        holder.itemView.setOnClickListener(v -> {
            String details = item.getDetails();
            if (details != null && !details.isEmpty()) {
                Intent intent = new Intent(v.getContext(), ReviewActivity.class);
                // ส่งข้อมูลเฉลยผ่าน Static (ใช้เมธอดที่เราทำไว้ก่อนหน้านี้เพื่อความเสถียร)
                // เนื่องจากใน ReviewActivity เราใช้ระบบ Static เพื่อกันแอปค้าง
                ReviewActivity.dataStore = null; 
                intent.putExtra("data", details); 
                v.getContext().startActivity(intent);
            } else {
                Toast.makeText(v.getContext(), "รายการนี้ไม่มีข้อมูลเฉลย (ข้อมูลเก่า)", Toast.LENGTH_SHORT).show();
            }
        });

        // ลบจ้าาา
        holder.btnDelete.setOnClickListener(v -> {

            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            HistoryItem currentItem = historyList.get(pos);

            DBHelper dbHelper = new DBHelper(v.getContext());

            // ลบจากSQLite
            dbHelper.deleteHistory(currentItem.getId());

            // ลบจากlist
            historyList.remove(pos);

            // อัปเดตRecyclerView
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, historyList.size());
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    //ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTopic, tvScore, tvDate;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTopic = itemView.findViewById(R.id.tvTopic);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvDate = itemView.findViewById(R.id.tvDate);

            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
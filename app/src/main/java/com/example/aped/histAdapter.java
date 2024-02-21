package com.example.aped;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class histAdapter extends RecyclerView.Adapter<histVH> {

    List<String> items;

    public histAdapter(List<String> items){
        this.items = items;
    }
    @NonNull
    @Override
    public histVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_hist,parent,false);

        return new histVH(view).linkAdapter(this);
    }

    @Override
    public void onBindViewHolder(@NonNull histVH holder, int position) {
        holder.histTV.setText(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

class histVH extends RecyclerView.ViewHolder{

    TextView histTV;
    private histAdapter adapter;
    public histVH(@NonNull View itemView) {
        super(itemView);
        histTV= itemView.findViewById(R.id.histTV);
        itemView.findViewById(R.id.del).setOnClickListener(view -> {
        adapter.items.remove(getAdapterPosition());
        adapter.notifyItemRemoved(getAdapterPosition());
        });
    }

    public histVH linkAdapter(histAdapter adapter){
        this.adapter = adapter;
        return this;
    }

}

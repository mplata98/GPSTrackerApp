package com.smim.plata.gpstracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.smim.plata.gpstracker.ui.main.HistoryFragment;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

    ArrayList<DataModel> mlist;
    Context context;
    private int selected;
    HistoryFragment historyFragment;


    public Adapter(Context context, ArrayList<DataModel> mList){
        this.mlist = mList;
        this.context = context;
        selected =-1;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_history,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DataModel model = mlist.get(position);
        holder.date.setText(model.getDateB());
        holder.distance.setText( (model.getDistanceRounded())+" km");

        holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.white));

        if(selected == position) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.teal_200));
        }

        holder.itemView.setOnClickListener(v -> {
            int previous = selected;
            selected = position;
            notifyItemChanged(previous);
            notifyItemChanged(position);
            historyFragment.setMap(selected);
        });
    }


    @Override
    public int getItemCount() {
        return mlist.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView date,distance;
        CardView cardView;
        View itemView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            date = itemView.findViewById(R.id.Date);
            distance = itemView.findViewById(R.id.Distance);
            cardView = itemView.findViewById(R.id.cardView);

        }
    }

    public void setHistoryFragment(HistoryFragment historyFragment){
        this.historyFragment=historyFragment;
    }

}

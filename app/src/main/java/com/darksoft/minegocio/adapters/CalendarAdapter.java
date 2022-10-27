package com.darksoft.minegocio.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.darksoft.minegocio.R;
import com.darksoft.minegocio.activities.DiaVentaActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private final ArrayList<String> diasDelMes;
    private Context context;

    public CalendarAdapter(ArrayList<String> daysOfMonth, Context context) {
        this.diasDelMes = daysOfMonth;
        this.context = context;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        holder.diaDelMes.setText(diasDelMes.get(position));

        holder.itemView.setOnClickListener(v -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
            LocalDate selectedDate = LocalDate.now();


            String dia = holder.diaDelMes.getText().toString();
            System.out.println(selectedDate.format(formatter));

//            if (!dia.equals("")){
//                Intent intent = new Intent(context, DiaVentaActivity.class);
//                intent.putExtra("dia", dia);
//
//                context.startActivity(intent);
//            }


        });
    }

    @Override
    public int getItemCount()
    {
        return diasDelMes.size();
    }

    public interface  OnItemListener {
        void onItemClick(int position, String dayText);
    }

    public class CalendarViewHolder extends RecyclerView.ViewHolder {

        public final TextView diaDelMes;

        public CalendarViewHolder(@NonNull View view) {
            super(view);
            diaDelMes = view.findViewById(R.id.cellDayText);
        }

    }

}

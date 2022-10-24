package com.darksoft.minegocio.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.darksoft.minegocio.R;
import com.darksoft.minegocio.models.NegocioModel;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

public class AdapterNegocio extends RecyclerView.Adapter<AdapterNegocio.NegocioViewHolder> {

    private NegocioModel negocioModel;
    private ArrayList<NegocioModel> listaNegocio;
    private Context context;


    public AdapterNegocio(ArrayList<NegocioModel> lista, Context context){
        this.listaNegocio = lista;
        this.context = context;
    }

    @NonNull
    @Override
    public NegocioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_row, parent, false);
        return new NegocioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NegocioViewHolder holder, int position) {
        //Obtenemos la posicion
        negocioModel = listaNegocio.get(position);

        //Damos el formato de los numeros
        double monto = Double.parseDouble(negocioModel.getMonto());
        DecimalFormat myFormatter = new DecimalFormat("###,###,###.##", DecimalFormatSymbols.getInstance(Locale.GERMANY));
        String montoFormato = myFormatter.format(monto);

        //cargamos los datos en la vista
        holder.monto.setText(montoFormato);
        holder.descripcion.setText(negocioModel.getDescripcion());
        holder.tipoVenta.setText(negocioModel.getTipo());

        //verificamos el tipo de negocio para cambiar el color
        if (negocioModel.getTipoNegocio().equals("ingreso"))
            holder.tipoVenta.setTextColor(Color.parseColor("#249C0E"));

        if (negocioModel.getTipoNegocio().equals("egreso"))
            holder.tipoVenta.setTextColor(Color.parseColor("#D81010"));

    }

    @Override
    public int getItemCount() {
        return listaNegocio.size();
    }

    //ViewHolder
    public static class NegocioViewHolder extends RecyclerView.ViewHolder {

        TextView monto;
        TextView descripcion;
        TextView tipoVenta;

        public NegocioViewHolder(@NonNull View view) {
            super(view);

            monto = view.findViewById(R.id.montoItemRow);
            descripcion = view.findViewById(R.id.descripcionItemRow);
            tipoVenta = view.findViewById(R.id.tipoVentaItemRow);

        }

    }
}

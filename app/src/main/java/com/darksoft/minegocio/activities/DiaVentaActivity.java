package com.darksoft.minegocio.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.darksoft.minegocio.R;
import com.darksoft.minegocio.adapters.AdapterNegocio;
import com.darksoft.minegocio.databinding.ActivityDiaVentaBinding;
import com.darksoft.minegocio.models.NegocioModel;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class DiaVentaActivity extends AppCompatActivity {

    private ActivityDiaVentaBinding binding;

    private RecyclerView listaVentas;
    private String fecha = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDiaVentaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Setup RecyclerView
        listaVentas = binding.fechaRecyclerView;
        listaVentas.setLayoutManager(new LinearLayoutManager(this));
        listaVentas.setHasFixedSize(true);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            fecha = extras.getString("fecha");
        }

        binding.fecha.setText(fecha);
        cargarNegocio();

    }

    private void cargarNegocio() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Solo obtenemos los documentos de acuerdo a la fecha actual
        db.collection("Negocio").document(fecha)
                .collection("ventas")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {

                        ArrayList<NegocioModel> lista = new ArrayList<>();
                        int totalCaja = 0, totalTarjeta = 0, totalInvertido = 0, totalDia = 0;

                        for (QueryDocumentSnapshot doc : value) {
                            NegocioModel model = doc.toObject(NegocioModel.class);

                            //Caja
                            if (doc.getData().get("tipo").equals("Caja")) {

                                //Sumamos todos los ingresos
                                if (doc.getData().get("tipoNegocio").equals("ingreso"))
                                    totalCaja += Integer.parseInt(model.getMonto());

                                //Sumamos todos los egresos
                                if (doc.getData().get("tipoNegocio").equals("egreso"))
                                    totalInvertido += Integer.parseInt(model.getMonto());
                            }

                            //Tarjeta
                            if (doc.getData().get("tipo").equals("Tarjeta")) {

                                //Sumamos todos los ingresos
                                if (doc.getData().get("tipoNegocio").equals("ingreso"))
                                    totalTarjeta += Integer.parseInt(model.getMonto());

                            }

                            model.setDescripcion(model.getDescripcion());
                            model.setMonto(model.getMonto());
                            model.setFecha(model.getFecha());
                            model.setTipo(model.getTipo());
                            lista.add(model);

                        }

                        totalDia = (totalCaja + totalTarjeta) - totalInvertido;

                        //Damos formato a los numeros
                        double tTotalDia = Double.parseDouble(String.valueOf(totalDia));
                        DecimalFormat myFormatter =
                                new DecimalFormat("###,###,###.##", DecimalFormatSymbols.getInstance(Locale.GERMANY));

                        String montoTotalDia = myFormatter.format(tTotalDia);

                        //pintamos los valores
                        binding.totalDiaTextView.setText("$ " + montoTotalDia);

                        if (lista.size() == 0){
                            binding.sinDatos.setVisibility(View.VISIBLE);
                            binding.content.setVisibility(View.INVISIBLE);
                        }else {
                            binding.sinDatos.setVisibility(View.INVISIBLE);
                            binding.content.setVisibility(View.VISIBLE);
                        }

//                        else
//                            binding.sinDatos.setVisibility(View.INVISIBLE);

                        //Llenamos el ReciclerView
                        listaVentas.setAdapter(new AdapterNegocio(lista, DiaVentaActivity.this));
                    }
                });

    }
}
package com.darksoft.minegocio.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.darksoft.minegocio.R;
import com.darksoft.minegocio.adapters.AdapterNegocio;
import com.darksoft.minegocio.databinding.ActivityDiaVentaBinding;
import com.darksoft.minegocio.models.NegocioModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView listaVentas;
    private String fecha = "";
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDiaVentaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        linearLayout = binding.sinDatos;

        //Setup RecyclerView
        listaVentas = binding.fechaRecyclerView;
        listaVentas.setLayoutManager(new LinearLayoutManager(this));
        listaVentas.setHasFixedSize(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            fecha = extras.getString("fecha");
        }

        binding.fecha.setText(fecha);

        if (user != null) {
            cargarNegocio();
        }


        binding.btnAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(this, DiaVentaAtrasado.class);
            intent.putExtra("fecha", fecha);
            startActivity(intent);
        });
    }

    private void cargarNegocio() {

        //Solo obtenemos los documentos de acuerdo a la fecha actual
        db.collection(user.getEmail()).document("Negocio").collection("fechas")
                .document(fecha).collection("ventas")
                .addSnapshotListener((value, e) -> {

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

                    // Pintar la UI
                    if (value.isEmpty()) {
                        linearLayout.setVisibility(View.VISIBLE);
                        listaVentas.setAdapter(null);
                    } else {
                        linearLayout.setVisibility(View.INVISIBLE);
                        listaVentas.setAdapter(new AdapterNegocio(lista, DiaVentaActivity.this));
                    }

                    // Total de ventas
                    totalDia = (totalCaja + totalTarjeta) - totalInvertido;

                    //Damos formato a los numeros
                    double tTotalDia = Double.parseDouble(String.valueOf(totalDia));
                    DecimalFormat myFormatter =
                            new DecimalFormat("###,###,###.##", DecimalFormatSymbols.getInstance(Locale.GERMANY));

                    String montoTotalDia = myFormatter.format(tTotalDia);

                    //pintamos los valores
                    binding.totalDiaTextView.setText("$ " + montoTotalDia);

                });

    }
}
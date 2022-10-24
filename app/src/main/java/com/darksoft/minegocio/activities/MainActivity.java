package com.darksoft.minegocio.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.darksoft.minegocio.R;
import com.darksoft.minegocio.adapters.AdapterNegocio;
import com.darksoft.minegocio.databinding.ActivityMainBinding;
import com.darksoft.minegocio.dialogs.PopUpNuevoGasto;
import com.darksoft.minegocio.dialogs.PopUpNuevoIngreso;
import com.darksoft.minegocio.models.NegocioModel;
import com.darksoft.minegocio.utilities.FechaActual;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static ActivityMainBinding binding;

    private FechaActual fechaActual = new FechaActual();

    private RecyclerView listaVentas;
    private AlertDialog dialog;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Setup RecyclerView
        listaVentas = findViewById(R.id.listaGlobalRecyclerView);
        listaVentas.setLayoutManager(new LinearLayoutManager(this));
        listaVentas.setHasFixedSize(true);

        cargarNegocio();
        botones();
    }

    private void cargarNegocio() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Solo obtenemos los documentos de acuerdo a la fecha actual
        db.collection("Negocio").document(fechaActual.fechaActual())
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
                        double tCaja = Double.parseDouble(String.valueOf(totalCaja));
                        double tTarjeta = Double.parseDouble(String.valueOf(totalTarjeta));
                        double tTotalInvertido = Double.parseDouble(String.valueOf(totalInvertido));
                        double tTotalDia = Double.parseDouble(String.valueOf(totalDia));

                        DecimalFormat myFormatter = new DecimalFormat("###,###,###.##", DecimalFormatSymbols.getInstance(Locale.GERMANY));

                        String montoCajaFormato = myFormatter.format(tCaja);
                        String montoTarjetaFormato = myFormatter.format(tTarjeta);
                        String montoInvertidoFormato = myFormatter.format(tTotalInvertido);
                        String montoTotalDia = myFormatter.format(tTotalDia);

                        //pintamos los valores
                        binding.bottomSheet.totalCajaTextView.setText("$ " + montoCajaFormato);
                        binding.bottomSheet.totalTarjetaTextView.setText("$ " + montoTarjetaFormato);
                        binding.bottomSheet.totalInvertidoTextView.setText("$ " + montoInvertidoFormato);
                        binding.bottomSheet.totalDiaTextView.setText("$ " + montoTotalDia);

                        final int dia = totalDia;

                        //Cerrar dia
                        binding.bottomSheet.cerrarDiaButton.setOnClickListener(v -> {

                            HashMap<String, String> datosDia = new HashMap<>();
                            datosDia.put("fecha", fechaActual.fechaActual());
                            datosDia.put("total", String.valueOf(dia));

                            cargarDialog(datosDia);

                        });

                        //Llenamos el ReciclerView
                        listaVentas.setAdapter(new AdapterNegocio(lista, MainActivity.this));
                    }
                });

    }

    private void cargarDialog(HashMap<String, String> datosDia) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Setup Dialog
        builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("¿Deseas terminar el dia?");

        builder.setPositiveButton("Sí", (dialogInterface, i) -> {
            db.collection("Ganancias").document(fechaActual.fechaActual()).set(datosDia);
            Toast.makeText(MainActivity.this, "Día Cerrado", Toast.LENGTH_SHORT).show();
            finish();
        });

        builder.setNegativeButton("Cancelar", (dialogInterface, i) -> {

        });

        dialog = builder.create();
        dialog.show();
    }

    private void botones() {
        binding.toolbar.setOnClickListener(v -> {
            binding.drawerlayout.openDrawer(GravityCompat.START);
        });

        binding.btnNuevoIngreso.setOnClickListener(v -> {
            DialogFragment pop = new PopUpNuevoIngreso();
            pop.show(getSupportFragmentManager(), "Nuevo Ingreso");
            return;
        });

        binding.btnNuevoGasto.setOnClickListener(v -> {
            DialogFragment pop = new PopUpNuevoGasto();
            pop.show(getSupportFragmentManager(), "Nuevo Gasto");
            return;
        });
    }
}
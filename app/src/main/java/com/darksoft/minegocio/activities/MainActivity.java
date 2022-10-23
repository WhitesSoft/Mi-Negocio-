package com.darksoft.minegocio.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.darksoft.minegocio.R;
import com.darksoft.minegocio.adapters.AdapterNegocio;
import com.darksoft.minegocio.databinding.ActivityMainBinding;
import com.darksoft.minegocio.dialogs.PopUpNuevoGasto;
import com.darksoft.minegocio.dialogs.PopUpNuevoIngreso;
import com.darksoft.minegocio.models.NegocioModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static ActivityMainBinding binding;
    private RecyclerView listaVentas;
    private AlertDialog dialog;
    private AlertDialog.Builder builder;

    //Fecha actual
    Calendar c = Calendar.getInstance();
    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    String fechaactual = df.format(c.getTime());

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

        db.collection("Negocio")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {

                        ArrayList<NegocioModel> lista = new ArrayList<>();
                        int totalCaja = 0, totalTarjeta = 0, totalInvertido = 0, totalDia = 0;

                        for (QueryDocumentSnapshot doc : value) {
                            NegocioModel model = doc.toObject(NegocioModel.class);

                            //Solo obtenemos los documentos de acuerdo a la fecha actual
                            if(doc.getData().get("fecha").equals(fechaactual)){

                                //Caja
                                if(doc.getData().get("tipo").equals("Caja")){

                                    //Sumamos todos los ingresos
                                    if(doc.getData().get("tipoNegocio").equals("ingreso"))
                                        totalCaja += Integer.parseInt(model.getMonto());

                                    //Sumamos todos los egresos
                                    if(doc.getData().get("tipoNegocio").equals("egreso"))
                                        totalInvertido += Integer.parseInt(model.getMonto());
                                }

                                //Tarjeta
                                if(doc.getData().get("tipo").equals("Tarjeta")){

                                    //Sumamos todos los ingresos
                                    if(doc.getData().get("tipoNegocio").equals("ingreso"))
                                        totalTarjeta += Integer.parseInt(model.getMonto());

                                }


                                model.setDescripcion(model.getDescripcion());
                                model.setMonto(model.getMonto());
                                model.setFecha(model.getFecha());
                                model.setTipo(model.getTipo());
                                lista.add(model);
                            }

                        }

                        //pintamos los valores

                        totalDia = (totalCaja + totalTarjeta) - totalInvertido;

                        binding.bottomSheet.totalCajaTextView.setText("$ " + totalCaja);
                        binding.bottomSheet.totalTarjetaTextView.setText("$ " + totalTarjeta);
                        binding.bottomSheet.totalInvertidoTextView.setText("$ " + totalInvertido);
                        binding.bottomSheet.totalDiaTextView.setText("$ " + totalDia);

                        final int dia = totalDia;

                        //Cerrar dia
                        binding.bottomSheet.cerrarDiaButton.setOnClickListener(v -> {

                            HashMap<String, String> datosDia = new HashMap<>();
                            datosDia.put("fecha", fechaactual);
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
            db.collection("Ganancias").document(fechaactual).set(datosDia);
            Toast.makeText(MainActivity.this, "Día Cerrado", Toast.LENGTH_SHORT).show();
            finish();
        });

        builder.setNegativeButton("Cancelar", (dialogInterface, i) -> {

        });

        dialog = builder.create();
        dialog.show();
    }

    private void botones(){
        binding.toolbar.setOnClickListener(v -> {
            binding.drawerlayout.openDrawer(GravityCompat.START);
        });

        binding.btnNuevoIngreso.setOnClickListener(v -> {
            DialogFragment pop = new PopUpNuevoIngreso();
            pop.show(getSupportFragmentManager(), "Nuevo Ingreso");
            return;
        });

        binding.btnNuevoGasto.setOnClickListener( v -> {
            DialogFragment pop = new PopUpNuevoGasto();
            pop.show(getSupportFragmentManager(), "Nuevo Gasto");
            return;
        });
    }
}
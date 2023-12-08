package com.darksoft.minegocio.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.darksoft.minegocio.R;
import com.darksoft.minegocio.adapters.AdapterNegocio;
import com.darksoft.minegocio.databinding.ActivityDiaVentaAtrasadoBinding;
import com.darksoft.minegocio.dialogs.PopUpNuevoGasto;
import com.darksoft.minegocio.dialogs.PopUpNuevoGastoAtrasado;
import com.darksoft.minegocio.dialogs.PopUpNuevoIngreso;
import com.darksoft.minegocio.dialogs.PopUpNuevoIngresoAtrasado;
import com.darksoft.minegocio.models.NegocioModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class DiaVentaAtrasado extends AppCompatActivity {

    private ActivityDiaVentaAtrasadoBinding binding;

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView listaVentas;
    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private String fecha = "";
    private Boolean estado = false;
    private ArrayList<String> listaGlobal = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDiaVentaAtrasadoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            fecha = extras.getString("fecha");
        }

        //Setup RecyclerView
        listaVentas = binding.listaGlobalRecyclerView;
        listaVentas.setLayoutManager(new LinearLayoutManager(this));
        listaVentas.setHasFixedSize(true);

        cargarNegocio();
        botones();

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
                        listaGlobal.add(model.toString());

                    }

                    totalDia = (totalCaja + totalTarjeta) - totalInvertido;

                    //Damos formato a los numeros
                    double tCaja = Double.parseDouble(String.valueOf(totalCaja));
                    double tTarjeta = Double.parseDouble(String.valueOf(totalTarjeta));
                    double tTotalInvertido = Double.parseDouble(String.valueOf(totalInvertido));
                    double tTotalDia = Double.parseDouble(String.valueOf(totalDia));

                    DecimalFormat myFormatter =
                            new DecimalFormat("###,###,###.##", DecimalFormatSymbols.getInstance(Locale.GERMANY));

                    String montoCajaFormato = myFormatter.format(tCaja);
                    String montoTarjetaFormato = myFormatter.format(tTarjeta);
                    String montoInvertidoFormato = myFormatter.format(tTotalInvertido);
                    String montoTotalDia = myFormatter.format(tTotalDia);

                    // pintamos los valores
                    binding.bottomSheet.totalCajaTextView.setText("$ " + montoCajaFormato);
                    binding.bottomSheet.totalTarjetaTextView.setText("$ " + montoTarjetaFormato);
                    binding.bottomSheet.totalInvertidoTextView.setText("$ " + montoInvertidoFormato);
                    binding.bottomSheet.totalDiaTextView.setText("$ " + montoTotalDia);

                    final int dia = totalDia;

                    // Cerrar dia
                    binding.bottomSheet.cerrarDiaButton.setOnClickListener(v -> {

                        HashMap<String, String> datosDia = new HashMap<>();
                        datosDia.put("fecha", fecha);
                        datosDia.put("total", String.valueOf(dia));

                        cargarDialog(datosDia);

                    });

                    // Llenamos el ReciclerView
                    listaVentas.setAdapter(new AdapterNegocio(lista, this));
                });

    }


    private void cargarDialog(HashMap<String, String> datosDia) {


        // Setup Dialog
        builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setMessage("¿Deseas terminar el dia?");

        builder.setPositiveButton("Sí", (dialogInterface, i) -> {

            db.collection(user.getEmail()).document("Ganancias")
                    .collection("fechas").document(fecha).set(datosDia);

            Toast.makeText(this, "Día Cerrado", Toast.LENGTH_SHORT).show();
            finish();
        });

        builder.setNegativeButton("Cancelar", (dialogInterface, i) -> {

        });

        dialog = builder.create();
        dialog.show();

        // Cambiar el color del texto y los botones del AlertDialog
        int textColor = ContextCompat.getColor(this, R.color.white);
        Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setTextColor(textColor);
        Button negative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negative.setTextColor(textColor);
    }

    private void botones() {

        binding.btnNuevoIngreso.setOnClickListener(v -> {
            DialogFragment pop = new PopUpNuevoIngresoAtrasado();
            Bundle args = new Bundle();
            args.putString("fecha", fecha);
            pop.setArguments(args);
            pop.show(getSupportFragmentManager(), "Nuevo Ingreso Atrasado");
            return;
        });

        binding.btnNuevoGasto.setOnClickListener(v -> {
            DialogFragment pop = new PopUpNuevoGastoAtrasado();
            Bundle args = new Bundle();
            args.putString("fecha", fecha);
            pop.setArguments(args);
            pop.show(getSupportFragmentManager(), "Nuevo Gasto Atrasado");
            return;
        });
    }

    @Override
    public void onBackPressed() {

        if (!listaGlobal.isEmpty()) {
            builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
            builder.setMessage("Tiene registros realizado en el dia, seguro que quieres salir sin cerrar el dia");
            builder.setPositiveButton("Sí", (dialogInterface, i) -> {

                db.collection(user.getEmail()).document("Negocio").collection("fechas")
                        .document(fecha).collection("ventas").get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    document.getReference().delete();
                                }

                                // Una vez que todas las subcolecciones han sido borradas, borra el documento padre
                                db.collection(user.getEmail()).document("Negocio").collection("fechas")
                                        .document(fecha).delete().addOnSuccessListener(aVoid -> {
                                            finish();
                                        }).addOnFailureListener(e -> {
                                            Log.e("Firestore", "Error al eliminar el documento", e);
                                        });
                            } else {
                                Log.e("Firestore", "Error al obtener los documentos de la subcoleccion", task.getException());
                            }
                        });


            });
            builder.setNegativeButton("Cancelar", (dialogInterface, i) -> {

            });
            dialog = builder.create();
            dialog.show();

            // Cambiar el color del texto y los botones del AlertDialog
            int textColor = ContextCompat.getColor(this, R.color.white);
            Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positive.setTextColor(textColor);
            Button negative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            negative.setTextColor(textColor);
        } else {
            super.onBackPressed();
        }

    }
}
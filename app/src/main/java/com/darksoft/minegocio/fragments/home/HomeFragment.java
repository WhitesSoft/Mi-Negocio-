package com.darksoft.minegocio.fragments.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.darksoft.minegocio.R;
import com.darksoft.minegocio.adapters.AdapterNegocio;
import com.darksoft.minegocio.databinding.FragmentHomeBinding;
import com.darksoft.minegocio.dialogs.PopUpNuevoGasto;
import com.darksoft.minegocio.dialogs.PopUpNuevoIngreso;
import com.darksoft.minegocio.models.NegocioModel;
import com.darksoft.minegocio.utilities.FechaActual;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private final FechaActual fechaActual = new FechaActual();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView listaVentas;
    private AlertDialog dialog;
    private AlertDialog.Builder builder;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        //Setup RecyclerView
        listaVentas = binding.listaGlobalRecyclerView;
        listaVentas.setLayoutManager(new LinearLayoutManager(getActivity()));
        listaVentas.setHasFixedSize(true);

        if (user != null) {
            cargarNegocio();
        }
        botones();

        return binding.getRoot();
    }

    private void cargarNegocio() {


        //Solo obtenemos los documentos de acuerdo a la fecha actual
        db.collection(user.getEmail()).document("Negocio").collection("fechas")
                .document(fechaActual.fechaActual()).collection("ventas")
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
                    listaVentas.setAdapter(new AdapterNegocio(lista, getActivity()));
                });

    }


    private void cargarDialog(HashMap<String, String> datosDia) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Setup Dialog
        builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
        builder.setMessage("¿Deseas terminar el dia?");

        builder.setPositiveButton("Sí", (dialogInterface, i) -> {

            db.collection(user.getEmail()).document("Ganancias")
                    .collection("fechas").document(fechaActual.fechaActual()).set(datosDia);

            Toast.makeText(getActivity(), "Día Cerrado", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancelar", (dialogInterface, i) -> {

        });

        dialog = builder.create();
        dialog.show();

        // Cambiar el color del texto y los botones del AlertDialog
        int textColor = ContextCompat.getColor(getActivity(), R.color.white);
        Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setTextColor(textColor);
        Button negative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negative.setTextColor(textColor);
    }

    private void botones() {

        binding.btnNuevoIngreso.setOnClickListener(v -> {
            DialogFragment pop = new PopUpNuevoIngreso();
            pop.show(getParentFragmentManager(), "Nuevo Ingreso");
            return;
        });

        binding.btnNuevoGasto.setOnClickListener(v -> {
            DialogFragment pop = new PopUpNuevoGasto();
            pop.show(getParentFragmentManager(), "Nuevo Gasto");
            return;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
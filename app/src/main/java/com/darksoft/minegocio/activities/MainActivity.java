package com.darksoft.minegocio.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;

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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static ActivityMainBinding binding;
    private RecyclerView listaVentas;

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
                        for (QueryDocumentSnapshot doc : value) {
                            NegocioModel model = doc.toObject(NegocioModel.class);
//
                            model.setDescripcion(model.getDescripcion());
                            model.setMonto(model.getMonto());
                            model.setFecha(model.getFecha());
                            model.setTipo(model.getTipo());

                            lista.add(model);
                        }
                        listaVentas.setAdapter(new AdapterNegocio(lista, R.layout.item_row, MainActivity.this));
                    }
                });

//        db.collection("Ingresos")
//                .get().addOnSuccessListener(queryDocumentSnapshots -> {
//
//                    ArrayList<NegocioModel> lista = new ArrayList<>();
//
//                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
//
//                        NegocioModel model = document.toObject(NegocioModel.class);
//
//                        model.setDescripcion(model.getDescripcion());
//                        model.setMonto(model.getMonto());
//                        model.setFecha(model.getFecha());
//                        model.setTipo(model.getTipo());
//
//                        lista.add(model);
//
//                    }
//                    listaVentas.setAdapter(new AdapterNegocio(lista, R.layout.item_row, this));
//                });

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
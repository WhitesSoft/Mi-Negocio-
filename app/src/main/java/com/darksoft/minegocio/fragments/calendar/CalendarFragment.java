package com.darksoft.minegocio.fragments.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.darksoft.minegocio.adapters.AdapterNegocio;
import com.darksoft.minegocio.databinding.FragmentCalendarBinding;
import com.darksoft.minegocio.models.NegocioModel;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCalendarBinding.inflate(inflater, container, false);

        cargarGanancias();

        return binding.getRoot();
    }

    private void cargarGanancias(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        try {
            //No da
            String fechaInicio = "01-11-2022";
            String fechaFinal = "30-11-2022";

            SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
            Date fechaInicioF = formato.parse(fechaInicio);
            Date fechaFinalF = formato.parse(fechaFinal);

            System.out.println(fechaInicioF);
            System.out.println(fechaFinalF);

            //Filtramos las ganancias de acuerdo al mes
            //whereGreaterThanOrEqualTo   >=
            //whereLessThanOrEqualTo   <=
            db.collection("Ganancias").whereGreaterThanOrEqualTo("fecha", fechaInicio)
                    .whereLessThanOrEqualTo("fecha", fechaFinal).addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                            int total = 0;

                            //Sumamos el total ganado de cada dia
                            for (QueryDocumentSnapshot doc : value) {
                                System.out.println(doc.getData());
                                total += Integer.parseInt(doc.getString("total"));
                            }

                            //Pasamos al formato de venta
                            DecimalFormat myFormatter =
                                    new DecimalFormat("###,###,###.##", DecimalFormatSymbols.getInstance(Locale.GERMANY));

                            String montoTotalFormato = myFormatter.format(total);

                            //Pintamos el valor
                            binding.total.setText("$ " + montoTotalFormato);

                        }
                    });

        } catch (ParseException e) {
            e.printStackTrace();
        }





    }

    private void validarFechas(){

        try {
            String fecha = "14-10-2022";
            String fechaInicio = "01-10-2022";
            String fechaFinal = "31-10-2022";

            SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");

            Date fechaFormato = formato.parse(fecha);
            Date fechaInicioF = formato.parse(fechaInicio);
            Date fechaFinalF = formato.parse(fechaFinal);

            if(fechaFormato.equals(fechaInicioF) || fechaFormato.equals(fechaFinalF)){
                System.out.println("La fecha [" + fecha +"] esta dentro del rango: " + " [" + fechaInicio + " - " + fechaFinal + "]");
            }else if(fechaFormato.after(fechaInicioF) && fechaFormato.before(fechaFinalF)) {
                System.out.println("La fecha [" + fecha +"] esta dentro del rango: " + " [" + fechaInicio + " - " + fechaFinal + "]");
            }else {
                System.out.println("La fecha [" + fecha +"] NO esta dentro del rango: " + " [" + fechaInicio + " - " + fechaFinal + "]");
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
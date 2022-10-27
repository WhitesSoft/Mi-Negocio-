package com.darksoft.minegocio.fragments.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.darksoft.minegocio.activities.DiaVentaActivity;
import com.darksoft.minegocio.adapters.CalendarAdapter;
import com.darksoft.minegocio.databinding.FragmentCalendarBinding;
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
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class CalendarFragment extends Fragment implements CalendarAdapter.OnItemListener{

    private FragmentCalendarBinding binding;

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    private LocalDate ultimoDiaMes;
    private ArrayList<String> diasDelMes;
    private ArrayList<String> fechas;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCalendarBinding.inflate(inflater, container, false);

        calendarRecyclerView = binding.calendarRecyclerView;
        monthYearText = binding.monthYearTV;

        selectedDate = LocalDate.now(); //Fecha Actual (2022-10-26)

        cargarMes();
        cargarGanancias();
        botones();

        return binding.getRoot();
    }


    private void cargarMes() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        monthYearText.setText(monthYearFromDate(selectedDate));
        diasDelMes = daysInMonthArray(selectedDate);
        ultimoDiaMes = selectedDate.with(TemporalAdjusters.lastDayOfMonth());

        CalendarAdapter calendarAdapter = new CalendarAdapter(diasDelMes,this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
        calendarRecyclerView.setHasFixedSize(true);

        //Obtenemos ultimo del mes y lo cambiamos de formato -> (31-10-2022)
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Date date = Date.from(ultimoDiaMes.atStartOfDay(defaultZoneId).toInstant());
        String fechaUltimoDiaMes = sdf.format(date);

        //Add todas las fechas del mes
        fechas = new ArrayList<>();
        for (int i = 1; i <= Integer.parseInt(fechaUltimoDiaMes.substring(0, 2)); i++) {
            String dia = String.valueOf(i);
            if (dia.length() == 1)
                fechas.add("0" + (dia + fechaUltimoDiaMes.substring(2)));
            else
                fechas.add(dia + fechaUltimoDiaMes.substring(2));
        }

        cargarGanancias();

    }

    private void cargarGanancias(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Mostramos el total de ganancia del mes
        db.collection(user.getEmail()).document("Ganancias")
                .collection("fechas").addSnapshotListener((value, error) -> {

                    int total = 0;

                    //Sumamos el total ganado de cada dia
                    for (QueryDocumentSnapshot doc : value) {
                        if (fechas.contains(doc.getString("fecha"))) {
                            total += Integer.parseInt(Objects.requireNonNull(doc.getString("total")));
                        }
                    }

                    //Pasamos al formato de venta
                    DecimalFormat myFormatter =
                            new DecimalFormat("###,###,###.##", DecimalFormatSymbols.getInstance(Locale.GERMANY));

                    String montoTotalFormato = myFormatter.format(total);

                    //Pintamos el valor
                    binding.total.setText("$ " + montoTotalFormato);

                });

    }

    //Obtener dias del mes actual
    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for (int i = 1; i <= 42; i++) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add("");
            } else {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }
        return daysInMonthArray;
    }

    //Formatear fecha (2022-10-26) -> (octubre 2022)
    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    private void botones() {
        binding.mesAnteriorButtom.setOnClickListener(v -> {
            selectedDate = selectedDate.minusMonths(1);
            diasDelMes = daysInMonthArray(selectedDate);
            cargarMes();
        });

        binding.mesSiguienteButtom.setOnClickListener(v -> {
            selectedDate = selectedDate.plusMonths(1);
            diasDelMes = daysInMonthArray(selectedDate);
            cargarMes();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if(!dayText.equals("")) {

            //Obtenemos el mes y lo cambiamos de formato -> (31-10-2022)
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            ZoneId defaultZoneId = ZoneId.systemDefault();
            Date date = Date.from(selectedDate.atStartOfDay(defaultZoneId).toInstant());
            String fechaMes = sdf.format(date);

            if (dayText.length() == 1)
                dayText = "0" + dayText;

            String fechaSeleccionada = dayText + fechaMes.substring(2);

            Intent intent = new Intent(getActivity(), DiaVentaActivity.class);
            intent.putExtra("fecha", fechaSeleccionada);
            startActivity(intent);

        }
    }
}
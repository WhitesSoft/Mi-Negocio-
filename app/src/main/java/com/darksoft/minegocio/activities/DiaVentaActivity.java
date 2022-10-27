package com.darksoft.minegocio.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.darksoft.minegocio.R;
import com.darksoft.minegocio.databinding.ActivityDiaVentaBinding;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DiaVentaActivity extends AppCompatActivity {

    private ActivityDiaVentaBinding binding;

    private LocalDate selectedDate;
    private String diaMes = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDiaVentaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        selectedDate = LocalDate.now();

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            diaMes = extras.getString("dia");
            if (diaMes.length() == 1)
                diaMes = "0" + diaMes;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Date date = Date.from(selectedDate.atStartOfDay(defaultZoneId).toInstant());

        String fechaUltimoDiaMes = sdf.format(date);

        System.out.println(fechaUltimoDiaMes);


        binding.diaaa.setText(diaMes);


    }
}
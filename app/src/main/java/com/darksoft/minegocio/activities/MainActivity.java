package com.darksoft.minegocio.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.DialogFragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;

import com.darksoft.minegocio.R;
import com.darksoft.minegocio.databinding.ActivityMainBinding;
import com.darksoft.minegocio.dialogs.PopUpNuevoIngreso;

public class MainActivity extends AppCompatActivity {

    private static ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setOnClickListener( v -> {
            binding.drawerlayout.openDrawer(GravityCompat.START);
        });

        binding.btnNuevoIngreso.setOnClickListener( v -> {
            DialogFragment pop = new PopUpNuevoIngreso();
            pop.show(getSupportFragmentManager(), "Nuevo Ingreso");
            return;
        });
    }
}
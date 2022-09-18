package com.darksoft.minegocio.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.darksoft.minegocio.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PopUpNuevoIngreso extends DialogFragment {

    private Button aceptarButton;
    private Spinner tipoSpinner;
    private EditText fechaEditText, montoEditTex, descripcionEditText;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_nuevo_ingreso, null);

        builder.setView(view);

        aceptarButton = view.findViewById(R.id.aceptarButton);
        tipoSpinner = view.findViewById(R.id.tipoEditText);
        montoEditTex = view.findViewById(R.id.montoEditText);
        descripcionEditText = view.findViewById(R.id.descripcionEditText);
        fechaEditText = view.findViewById(R.id.fechaEditText);

        fecha();
        datosSpinner();
        subirDB();

        Dialog dialog = builder.create();
        if (dialog.getWindow() != null){
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    private void fecha() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c.getTime());
        fechaEditText.setText(formattedDate);
    }

    private void datosSpinner() {
        String[] opciones = getResources().getStringArray(R.array.opciones);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.lista_opciones, opciones);
        tipoSpinner.setAdapter(adapter);
    }

    public void subirDB(){
        aceptarButton.setOnClickListener(view -> {

            FirebaseFirestore bd = FirebaseFirestore.getInstance();

            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            String id = df.format(c.getTime());

            String tipo = "";
            tipoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int posicion, long id) {
                    //tipo = adapterView.getItemAtPosition(posicion).toString();
                    Toast.makeText(getActivity(), "s: " + adapterView.getItemAtPosition(posicion).toString(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            String tipoo = (String) tipoSpinner.getSelectedItem();

            Map<String, String> datos = new HashMap<>();
            datos.put("id", id);
            datos.put("monto", montoEditTex.getText().toString());
            datos.put("descripcion", descripcionEditText.getText().toString());
            datos.put("fecha", fechaEditText.getText().toString());
            datos.put("tipo", tipoo);

            bd.collection("Ingresos").document().set(datos);
            Toast.makeText(getActivity(), "Subido", Toast.LENGTH_SHORT).show();
            dismiss();

        });
    }

}

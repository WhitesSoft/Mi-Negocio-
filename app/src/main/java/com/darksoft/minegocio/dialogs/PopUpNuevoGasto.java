package com.darksoft.minegocio.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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

public class PopUpNuevoGasto extends DialogFragment {

    private Button aceptarButton;
    private EditText fechaEditText, montoEditTex, descripcionEditText;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_nuevo_gasto, null);

        builder.setView(view);

        aceptarButton = view.findViewById(R.id.aceptarButton);
        montoEditTex = view.findViewById(R.id.montoEditText);
        descripcionEditText = view.findViewById(R.id.descripcionEditText);
        fechaEditText = view.findViewById(R.id.fechaEditText);

        fecha();
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


    public void subirDB(){
        aceptarButton.setOnClickListener(view -> {

            FirebaseFirestore bd = FirebaseFirestore.getInstance();

            if(validar()){

                Map<String, String> datos = new HashMap<>();
                datos.put("monto", montoEditTex.getText().toString());
                datos.put("descripcion", descripcionEditText.getText().toString());
                datos.put("fecha", fechaEditText.getText().toString());
                datos.put("tipo", "Caja");
                datos.put("tipoNegocio", "egreso");

                bd.collection("Negocio").document().set(datos);
                Toast.makeText(getActivity(), "Subido", Toast.LENGTH_SHORT).show();
                dismiss();
            }


        });
    }

    //Validamos los campos
    private boolean validar() {

        String montoEditTexValidar = montoEditTex.getText().toString();
        String descripcionEditTextValidar = descripcionEditText.getText().toString();

        if (montoEditTexValidar.isEmpty()){
            montoEditTex.setError("Debe ingresar un monto.");
            return false;
        }
        if (descripcionEditTextValidar.isEmpty()){
            descripcionEditText.setError("Debe ingresar una descripccion del producto.");
            return false;
        }
        return true;

    }

}

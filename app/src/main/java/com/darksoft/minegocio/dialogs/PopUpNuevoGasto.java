package com.darksoft.minegocio.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.darksoft.minegocio.R;
import com.darksoft.minegocio.utilities.FechaActual;
import com.darksoft.minegocio.utilities.NumberTextWatcher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PopUpNuevoGasto extends DialogFragment {

    private FechaActual fechaActual = new FechaActual();

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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

        //Separador de miles
        montoEditTex.addTextChangedListener(new NumberTextWatcher(montoEditTex));

        fecha();
        subirDB();

        Dialog dialog = builder.create();
        if (dialog.getWindow() != null){
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        return dialog;
    }

    private void fecha() {
        fechaEditText.setText(fechaActual.fechaActual());
    }


    public void subirDB(){
        aceptarButton.setOnClickListener(view -> {

            FirebaseFirestore bd = FirebaseFirestore.getInstance();

            String quitar = montoEditTex.getText().toString();
            String monto = "";

            //Obtenemos solo los numeros
            for (int i = 0; i < quitar.length(); i++){
                if(Character.isDigit(quitar.charAt(i)))
                    monto += quitar.charAt(i);
            }

            if(validar()){

                Map<String, String> datos = new HashMap<>();
                datos.put("monto", monto);
                datos.put("descripcion", descripcionEditText.getText().toString());
                datos.put("fecha", fechaEditText.getText().toString());
                datos.put("tipo", "Caja");
                datos.put("tipoNegocio", "egreso");

                bd.collection(user.getEmail()).document("Negocio")
                        .collection("fechas").document(fechaActual.fechaActual())
                        .collection("ventas").document().set(datos);

                Toast.makeText(getActivity(), "Gasto agregado", Toast.LENGTH_SHORT).show();
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


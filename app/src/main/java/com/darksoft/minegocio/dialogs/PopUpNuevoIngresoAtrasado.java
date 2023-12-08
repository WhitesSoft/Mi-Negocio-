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
import com.darksoft.minegocio.utilities.NumberTextWatcher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PopUpNuevoIngresoAtrasado extends DialogFragment {

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Button aceptarButton;
    private Spinner tipoSpinner;
    private EditText fechaEditText, montoEditTex, descripcionEditText;
    private String fecha;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fecha = getArguments().getString("fecha");
        }
    }

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

        //Separador de miles
        montoEditTex.addTextChangedListener(new NumberTextWatcher(montoEditTex));

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
        fechaEditText.setText(fecha);
    }

    private void datosSpinner() {
        String[] opciones = getResources().getStringArray(R.array.opciones);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.lista_opciones, opciones);
        tipoSpinner.setAdapter(adapter);
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
                String tipo = (String) tipoSpinner.getSelectedItem();

                Map<String, String> datos = new HashMap<>();
                datos.put("monto", monto);
                datos.put("descripcion", descripcionEditText.getText().toString());
                datos.put("fecha", fechaEditText.getText().toString());
                datos.put("tipo", tipo);
                datos.put("tipoNegocio", "ingreso");

                bd.collection(user.getEmail()).document("Negocio")
                        .collection("fechas").document(fecha)
                        .collection("ventas").document().set(datos);

                Toast.makeText(getActivity(), "Ingreso agregado", Toast.LENGTH_SHORT).show();
                dismiss();
            }


        });
    }

    // Validamos los campos
    private boolean validar() {

        String tipoSpinnerValidar = tipoSpinner.getSelectedItem().toString();
        String montoEditTexValidar = montoEditTex.getText().toString();
        String descripcionEditTextValidar = descripcionEditText.getText().toString();

        if (tipoSpinnerValidar.isEmpty() || tipoSpinnerValidar.equals("Tipo")){
            Toast.makeText(getActivity(), "Debe seleccionar un tipo.", Toast.LENGTH_SHORT).show();
            return false;
        }
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

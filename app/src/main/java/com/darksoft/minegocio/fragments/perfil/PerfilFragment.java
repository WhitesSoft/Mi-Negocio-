package com.darksoft.minegocio.fragments.perfil;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.darksoft.minegocio.R;
import com.darksoft.minegocio.activities.LoginActivity;
import com.darksoft.minegocio.databinding.FragmentPerfilBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PerfilFragment extends Fragment {

    private FragmentPerfilBinding binding;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        
        binding = FragmentPerfilBinding.inflate(inflater, container, false);

        //Google Autentificacion
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        //Cargamos los datos del usuario
        cargarInformacion();

        //cerrar sesion
        binding.cerrar.setOnClickListener(view -> eliminarCuenta());
        
        return binding.getRoot();
    }

    private void cargarInformacion() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            binding.nombre.setText(user.getDisplayName());
            binding.email.setText(user.getEmail());
            Glide.with(this).load(user.getPhotoUrl()).into(binding.perfil);
        }
    }

    private void eliminarCuenta() {

        binding.progressBarPerfil.setVisibility(View.VISIBLE);
        binding.cerrar.setVisibility(View.INVISIBLE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()){
                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                irPantallaLogin();
                            }
                        });
                    }else{
                        binding.progressBarPerfil.setVisibility(View.GONE);
                        binding.cerrar.setVisibility(View.VISIBLE);
                    }
                });

    }

    private void irPantallaLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
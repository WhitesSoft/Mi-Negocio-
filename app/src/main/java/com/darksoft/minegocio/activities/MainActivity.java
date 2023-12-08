package com.darksoft.minegocio.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.darksoft.minegocio.R;
import com.darksoft.minegocio.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AppBarConfiguration mAppBarConfiguration;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Setup NavigationDrawer
        DrawerLayout drawer = binding.drawerlayout;
        NavigationView navigationView = binding.navigationView;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_calendar)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.main);
        NavigationUI.setupWithNavController(navigationView, navController);


        // Accedemos al NavViewHeader
        View headerView = navigationView.getHeaderView(0);
        TextView userEmail = headerView.findViewById(R.id.email);
        ImageView imageViewPerfil = headerView.findViewById(R.id.imageViewPerfil);


        // Cargamos los datos
        if (user != null) {
            Glide.with(this).load(user.getPhotoUrl()).into(imageViewPerfil);
            userEmail.setText(user.getEmail());
        }


        // cerrar sesion
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Button btnCerrarSesion = navigationView.findViewById(R.id.btnCerrarSesion);
        ProgressBar progressBar = navigationView.findViewById(R.id.cargandoNav);

        btnCerrarSesion.setOnClickListener(v -> {

            progressBar.setVisibility(View.VISIBLE);
            btnCerrarSesion.setVisibility(View.INVISIBLE);

            mGoogleSignInClient.revokeAccess().addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    user.delete().addOnCompleteListener(task1 -> {
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finishAffinity();
                    });
                } else {
                    btnCerrarSesion.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "No se pudo cerrar sesión, intenta nuevamente más tarde.", Toast.LENGTH_SHORT).show();
                }
            });

        });

        botones();
    }


    private void botones() {
        binding.toolbar.setOnClickListener(v -> {
            binding.drawerlayout.openDrawer(GravityCompat.START);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
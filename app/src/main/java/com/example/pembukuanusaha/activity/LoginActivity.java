package com.example.pembukuanusaha.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.session.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    MaterialButton btnLogin;
    TextView txtDaftar; // TOMBOL BARU
    ProgressBar progressBar;

    FirebaseAuth auth;
    FirebaseFirestore firestore;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        // Cek jika user sudah login
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Init View
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtDaftar = findViewById(R.id.txtDaftar); // Init Tombol Daftar
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> prosesLogin());

        // 🔥 LOGIKA PINDAH KE HALAMAN DAFTAR
        txtDaftar.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void prosesLogin() {
        String email = edtEmail.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Email dan Password wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        loadUserSession(user.getUid(), email);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Login Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserSession(String uid, String email) {
        firestore.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            String role = doc.getString("role");
                            String usahaId = doc.getString("usaha_id");
                            String cabangId = doc.getString("cabang_id");

                            sessionManager.createLoginSession(uid, email, role, usahaId, cabangId);

                            Toast.makeText(LoginActivity.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            // User terdaftar di Auth tapi belum ada data di Firestore (Kasus langka karena sekarang sudah auto-create di Register)
                            Toast.makeText(LoginActivity.this, "Data user tidak ditemukan di database.", Toast.LENGTH_LONG).show();
                            auth.signOut();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Gagal mengambil data user.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
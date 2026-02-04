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
import com.example.pembukuanusaha.session.SessionManager; // 🔥 PENTING: Import SessionManager
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText edtNama, edtEmail, edtPassword, edtKonfirmasiPass;
    MaterialButton btnDaftar;
    TextView txtMasuk;
    ProgressBar progressBar;

    FirebaseAuth auth;
    FirebaseFirestore firestore;
    SessionManager sessionManager; // 🔥 Tambahkan variabel SessionManager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Init SessionManager
        sessionManager = new SessionManager(this); // 🔥 Inisialisasi Sesi

        // Init View
        edtNama = findViewById(R.id.edtNama);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtKonfirmasiPass = findViewById(R.id.edtKonfirmasiPass);
        btnDaftar = findViewById(R.id.btnDaftar);
        txtMasuk = findViewById(R.id.txtMasuk);
        progressBar = findViewById(R.id.progressBar);

        // Listener Tombol
        btnDaftar.setOnClickListener(v -> prosesDaftar());
        txtMasuk.setOnClickListener(v -> finish()); // Kembali ke Login
    }

    private void prosesDaftar() {
        String nama = edtNama.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();
        String confirmPass = edtKonfirmasiPass.getText().toString().trim();

        // Validasi Input
        if (nama.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirmPass)) {
            edtKonfirmasiPass.setError("Password tidak sama");
            return;
        }

        if (pass.length() < 6) {
            edtPassword.setError("Password minimal 6 karakter");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnDaftar.setEnabled(false);

        // 1. Buat Akun di Auth
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        simpanDataUserKeFirestore(user.getUid(), nama, email);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnDaftar.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Gagal Daftar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void simpanDataUserKeFirestore(String uid, String nama, String email) {
        // Data User Baru (Default jadi ADMIN)
        String usahaId = "usaha_" + uid.substring(0, 5); // ID Usaha Unik
        String role = "admin";
        String cabangId = "pusat";

        Map<String, Object> userData = new HashMap<>();
        userData.put("nama", nama);
        userData.put("email", email);
        userData.put("role", role);
        userData.put("usaha_id", usahaId);
        userData.put("cabang_id", cabangId);
        userData.put("created_at", System.currentTimeMillis());

        // 2. Simpan ke Firestore (Koleksi 'users')
        firestore.collection("users").document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // 🔥 PERBAIKAN UTAMA DI SINI:
                    // Simpan sesi login sebelum pindah halaman agar status ADMIN terbaca
                    sessionManager.createLoginSession(uid, email, role, usahaId, cabangId);

                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show();

                    // Langsung masuk ke Dashboard
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finishAffinity(); // Hapus semua activity sebelumnya
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnDaftar.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Gagal simpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
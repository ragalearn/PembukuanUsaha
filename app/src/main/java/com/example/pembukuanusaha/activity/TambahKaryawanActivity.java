package com.example.pembukuanusaha.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.session.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TambahKaryawanActivity extends AppCompatActivity {

    EditText edtNama, edtEmail, edtPassword;
    MaterialButton btnSimpan;
    ProgressBar progressBar;

    SessionManager sessionManager;
    FirebaseFirestore firestore;

    // Variabel ID Usaha milik Admin (Bos)
    String adminUsahaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_karyawan);

        // 1. Ambil Data Sesi Admin
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            finish(); // Jika belum login, tutup
            return;
        }

        // KUNCI UTAMA: Ambil ID Usaha Admin agar Karyawan masuk ke 'Toko' yang sama
        adminUsahaId = sessionManager.getUsahaId();

        // Init Firestore utama
        firestore = FirebaseFirestore.getInstance();

        // Init View
        edtNama = findViewById(R.id.edtNama);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnSimpan = findViewById(R.id.btnSimpan);
        progressBar = findViewById(R.id.progressBar);

        btnSimpan.setOnClickListener(v -> validasiInput());
    }

    private void validasiInput() {
        String nama = edtNama.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(nama) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pass.length() < 6) {
            edtPassword.setError("Password minimal 6 karakter");
            return;
        }

        tambahKaryawan(nama, email, pass);
    }

    // ====================================================================
    // 🧠 LOGIKA PENTING: Membuat User tanpa Logout Admin (Secondary App)
    // ====================================================================
    private void tambahKaryawan(String nama, String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        btnSimpan.setEnabled(false);

        // 1. Buat Instance Firebase "Bayangan" (Secondary)
        FirebaseOptions firebaseOptions = FirebaseApp.getInstance().getOptions();
        FirebaseApp secondaryApp;

        try {
            secondaryApp = FirebaseApp.initializeApp(this, firebaseOptions, "Secondary");
        } catch (IllegalStateException e) {
            // Jika app secondary sudah ada, pakai yang sudah ada
            secondaryApp = FirebaseApp.getInstance("Secondary");
        }

        // 2. Ambil Auth dari App Secondary
        FirebaseAuth secondaryAuth = FirebaseAuth.getInstance(secondaryApp);

        // 3. Buat User Baru di App Secondary
        secondaryAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser newUser = authResult.getUser();
                    if (newUser != null) {
                        String newUid = newUser.getUid();
                        // Sign out user baru dari app secondary agar tidak mengganggu sesi utama
                        secondaryAuth.signOut();

                        // Lanjut simpan data ke Firestore Utama
                        simpanKeFirestore(newUid, nama, email);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSimpan.setEnabled(true);
                    Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void simpanKeFirestore(String uid, String nama, String email) {
        // Data Karyawan
        Map<String, Object> userData = new HashMap<>();
        userData.put("nama", nama);
        userData.put("email", email);
        userData.put("role", "user");  // 🔥 ROLE OTOMATIS JADI USER (KARYAWAN)
        userData.put("usaha_id", adminUsahaId); // 🔥 ID USAHA SAMA DENGAN BOS
        userData.put("cabang_id", "pusat");
        userData.put("created_at", System.currentTimeMillis());

        // Simpan ke koleksi 'users'
        firestore.collection("users").document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Sukses! Karyawan berhasil ditambahkan.", Toast.LENGTH_LONG).show();
                    finish(); // Tutup halaman
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSimpan.setEnabled(true);
                    Toast.makeText(this, "User dibuat tapi gagal simpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
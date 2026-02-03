package com.example.pembukuanusaha.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.session.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnLogin;

    FirebaseAuth auth;
    FirebaseFirestore firestore;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        session = new SessionManager(this);

        // 🔒 Jika sudah login → langsung ke Main
        if (auth.getCurrentUser() != null) {
            goToMain();
            return;
        }

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email & Password wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> loadUserSession())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Login gagal: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // =========================
    // AMBIL ROLE + USAHA + CABANG
    // =========================
    private void loadUserSession() {

        String uid = auth.getCurrentUser().getUid();

        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(this, "Data user tidak ditemukan", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String role = doc.getString("role");
                    String usahaId = doc.getString("usaha_id");
                    String cabangId = doc.getString("cabang_id");

                    // 🔥 SET SESSION GLOBAL
                    session.setRole(role);
                    session.setUsaha(usahaId, cabangId);

                    goToMain();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal ambil data user", Toast.LENGTH_LONG).show()
                );
    }

    private void goToMain() {
        Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}

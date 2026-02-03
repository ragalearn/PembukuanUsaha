package com.example.pembukuanusaha.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.database.DatabaseHelper;
import com.example.pembukuanusaha.model.Produk;
import com.example.pembukuanusaha.session.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class AyoBelanjaActivity extends AppCompatActivity {

    Spinner spinnerProduk;
    EditText edtJumlah;
    Button btnHitung;
    TextView txtHasil;

    DatabaseHelper db;
    List<Produk> produkList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // =====================
        // SESSION CHECK (ADMIN ONLY)
        // =====================
        SessionManager session = new SessionManager(this);
        if (!session.isAdmin()) {
            finish(); // kasir tidak boleh masuk
            return;
        }

        setContentView(R.layout.activity_ayo_belanja);

        // =====================
        // INIT VIEW
        // =====================
        spinnerProduk = findViewById(R.id.spinnerProduk);
        edtJumlah = findViewById(R.id.edtJumlah);
        btnHitung = findViewById(R.id.btnHitung);
        txtHasil = findViewById(R.id.txtHasil);

        db = new DatabaseHelper(this);
        loadProduk();

        btnHitung.setOnClickListener(v -> hitungModal());
    }

    // =====================
    // LOAD PRODUK KE SPINNER
    // =====================
    private void loadProduk() {
        produkList.clear();
        List<String> namaProduk = new ArrayList<>();

        Cursor c = db.getAllProduk();
        while (c.moveToNext()) {
            Produk p = new Produk(
                    c.getInt(0),
                    c.getString(1),
                    c.getInt(2),
                    c.getInt(3),
                    c.getInt(4)
            );
            produkList.add(p);
            namaProduk.add(p.getNama());
        }
        c.close();

        if (produkList.isEmpty()) {
            Toast.makeText(this, "Belum ada produk", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                namaProduk
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProduk.setAdapter(adapter);
    }

    // =====================
    // HITUNG TOTAL MODAL
    // =====================
    private void hitungModal() {

        if (edtJumlah.getText().toString().trim().isEmpty()) {
            edtJumlah.setError("Masukkan jumlah beli");
            edtJumlah.requestFocus();
            return;
        }

        int jumlah;
        try {
            jumlah = Integer.parseInt(edtJumlah.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Jumlah tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        if (jumlah <= 0) {
            edtJumlah.setError("Jumlah harus > 0");
            edtJumlah.requestFocus();
            return;
        }

        int posisi = spinnerProduk.getSelectedItemPosition();
        Produk produk = produkList.get(posisi);

        int totalModal = produk.getHargaModal() * jumlah;

        txtHasil.setText("Total Modal: Rp " + totalModal);
    }
}

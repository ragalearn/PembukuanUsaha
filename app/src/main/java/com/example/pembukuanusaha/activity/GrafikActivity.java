package com.example.pembukuanusaha.activity;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.database.DatabaseHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GrafikActivity extends AppCompatActivity {

    BarChart chartOmzet;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafik);

        chartOmzet = findViewById(R.id.chartOmzet);
        db = new DatabaseHelper(this);

        setupChartStyle();
        loadDataGrafik7Hari();
    }

    // ===========================
    // 1. SETUP TAMPILAN (STYLE)
    // ===========================
    private void setupChartStyle() {
        chartOmzet.getDescription().setEnabled(false); // Hapus label deskripsi
        chartOmzet.setDrawGridBackground(false);
        chartOmzet.setExtraBottomOffset(10f); // Jarak bawah agar label tanggal tidak kepotong

        // --- Sumbu X (Tanggal) ---
        XAxis xAxis = chartOmzet.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false); // Hapus garis vertikal
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.DKGRAY);
        xAxis.setTextSize(10f);

        // --- Sumbu Y Kiri (Omzet) ---
        YAxis leftAxis = chartOmzet.getAxisLeft();
        leftAxis.setDrawGridLines(true); // Garis bantu horizontal tipis
        leftAxis.setGridColor(Color.LTGRAY);
        leftAxis.setAxisMinimum(0f); // Mulai dari 0

        // --- Sumbu Y Kanan (Hapus biar bersih) ---
        chartOmzet.getAxisRight().setEnabled(false);

        // Animasi
        chartOmzet.animateY(1000);
    }

    // ===========================
    // 2. LOAD DATA (LOGIKA BARU)
    // ===========================
    private void loadDataGrafik7Hari() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        // Kita hitung mundur 7 hari dari hari ini
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -6); // Mundur 6 hari + hari ini = 7 hari

        SimpleDateFormat sdfQuery = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // Untuk Query DB
        SimpleDateFormat sdfLabel = new SimpleDateFormat("dd/MM", Locale.getDefault());      // Untuk Label Grafik

        // Loop 7 kali (Senin, Selasa, Rabu, dst...)
        for (int i = 0; i < 7; i++) {
            String tanggalQuery = sdfQuery.format(calendar.getTime());
            String tanggalLabel = sdfLabel.format(calendar.getTime());

            // Ambil total omzet per tanggal spesifik
            int omzet = getTotalOmzetPerTanggal(tanggalQuery);

            entries.add(new BarEntry(i, omzet));
            labels.add(tanggalLabel);

            // Maju ke hari berikutnya
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // --- Dataset ---
        BarDataSet dataSet = new BarDataSet(entries, "Omzet Harian");

        // UBAH WARNA JADI HIJAU (KONSISTENSI BRANDING)
        dataSet.setColor(ContextCompat.getColor(this, R.color.colorPrimary));

        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(11f);

        // Format Angka di atas batang (Misal: 100000 jadi 100k) biar rapi
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) { // PERBAIKAN: ubah void jadi String
                if (value >= 1000000) return String.format("%.1fjt", value / 1000000);
                if (value >= 1000) return String.format("%.0fk", value / 1000);
                return String.valueOf((int) value);
            }
        });

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f); // Lebar batang proporsional

        // Set Label Tanggal di Bawah
        chartOmzet.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        chartOmzet.setData(data);
        chartOmzet.invalidate(); // Refresh chart
    }

    // Helper: Query Manual Total Omzet per Tanggal
    private int getTotalOmzetPerTanggal(String tanggal) {
        int total = 0;
        try {
            // Pastikan nama kolom 'harga_jual', 'jumlah', 'tanggal', dan tabel 'transaksi'
            // sesuai dengan DatabaseHelper kamu. Jika beda, sesuaikan string ini.
            Cursor c = db.getReadableDatabase().rawQuery(
                    "SELECT SUM(" + DatabaseHelper.COL_HARGA_JUAL + " * " + DatabaseHelper.COL_JUMLAH + ") " +
                            "FROM " + DatabaseHelper.TABLE_TRANSAKSI +
                            " WHERE " + DatabaseHelper.COL_TANGGAL + " = ?",
                    new String[]{tanggal}
            );

            if (c.moveToFirst()) {
                total = c.getInt(0);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }
}
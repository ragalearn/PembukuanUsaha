package com.example.pembukuanusaha.activity;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.database.DatabaseHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class GrafikActivity extends AppCompatActivity {

    BarChart chartOmzet;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafik);

        chartOmzet = findViewById(R.id.chartOmzet);
        db = new DatabaseHelper(this);

        setupChart();
        loadDataGrafik();
    }

    private void setupChart() {
        chartOmzet.getDescription().setEnabled(false);
        chartOmzet.setDrawGridBackground(false);
        chartOmzet.setFitBars(true);
        chartOmzet.animateY(1000); // Animasi saat dibuka

        // Setting Sumbu X (Tanggal)
        XAxis xAxis = chartOmzet.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
    }

    private void loadDataGrafik() {
        Cursor c = db.getOmzetHarian();
        if (c == null || c.getCount() == 0) {
            Toast.makeText(this, "Belum ada data transaksi untuk grafik", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int index = 0;

        while (c.moveToNext()) {
            String tanggal = c.getString(0); // Tanggal (YYYY-MM-DD)
            int omzet = c.getInt(1);         // Total Omzet

            // Ambil tanggal/bulan saja biar pendek (misal: 2023-10-25 -> 25/10)
            String labelPendek = tanggal.substring(8) + "/" + tanggal.substring(5, 7);

            entries.add(new BarEntry(index, omzet));
            labels.add(labelPendek);
            index++;
        }
        c.close();

        // Dataset
        BarDataSet dataSet = new BarDataSet(entries, "Omzet Harian");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Warna-warni cantik
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        // Pasang Label ke Sumbu X
        chartOmzet.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chartOmzet.setData(data);
        chartOmzet.invalidate(); // Refresh chart
    }
}
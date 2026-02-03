package com.example.pembukuanusaha.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.database.DatabaseHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;

public class GrafikActivity extends AppCompatActivity {

    // =====================
    // VIEW
    // =====================
    BarChart barChart;
    ProgressBar progressLoading;
    TextView txtEmpty;

    // =====================
    // DATABASE
    // =====================
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafik);

        // =====================
        // INIT VIEW
        // =====================
        barChart = findViewById(R.id.barChart);
        progressLoading = findViewById(R.id.progressLoading);
        txtEmpty = findViewById(R.id.txtEmpty);

        db = new DatabaseHelper(this);

        // =====================
        // LOAD DATA
        // =====================
        loadChart();
    }

    // =====================
    // LOAD GRAFIK
    // =====================
    private void loadChart() {

        // LOADING ON
        progressLoading.setVisibility(View.VISIBLE);
        barChart.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.GONE);

        Cursor c = db.getOmzetHarian();

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        int index = 0;

        if (c != null) {
            while (c.moveToNext()) {
                String tanggal = c.getString(0);
                int omzet = c.getInt(1);

                labels.add(tanggal);
                entries.add(new BarEntry(index, omzet));
                index++;
            }
            c.close();
        }

        // LOADING OFF
        progressLoading.setVisibility(View.GONE);

        // EMPTY STATE
        if (entries.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            barChart.setVisibility(View.GONE);
            return;
        }

        // =====================
        // SETUP CHART
        // =====================
        BarDataSet dataSet = new BarDataSet(entries, "Omzet Harian");
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);

        barChart.setData(data);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(true);

        // =====================
        // X AXIS
        // =====================
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setDrawGridLines(false);

        barChart.getAxisRight().setEnabled(false);
        barChart.animateY(800);
        barChart.invalidate();

        barChart.setVisibility(View.VISIBLE);
    }
}

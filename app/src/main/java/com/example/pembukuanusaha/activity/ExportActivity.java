package com.example.pembukuanusaha.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView; // PENTING: Menggunakan CardView sesuai UI baru
import androidx.core.content.FileProvider;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.database.DatabaseHelper;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExportActivity extends AppCompatActivity {

    // Menggunakan ID sesuai XML baru (btnExcel, btnPdf)
    CardView btnExcel, btnPdf;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        // Init View (Sesuai ID di activity_export.xml yang baru)
        btnExcel = findViewById(R.id.btnExcel);
        btnPdf   = findViewById(R.id.btnPdf);
        db       = new DatabaseHelper(this);

        // Listener Tombol
        btnExcel.setOnClickListener(v -> exportToExcel());
        btnPdf.setOnClickListener(v -> exportToPdf());
    }

    // ===========================
    // 1. LOGIKA EXPORT EXCEL (Apache POI)
    // ===========================
    private void exportToExcel() {
        try {
            // Cek data dulu
            Cursor c = db.getAllTransaksi();
            if (c == null || c.getCount() == 0) {
                Toast.makeText(this, "Data Transaksi Kosong!", Toast.LENGTH_SHORT).show();
                return;
            }

            Workbook wb = new HSSFWorkbook();
            Sheet sheet = wb.createSheet("Laporan Transaksi");

            // Buat Header yang Rapi
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("TANGGAL");
            headerRow.createCell(1).setCellValue("NAMA PRODUK");
            headerRow.createCell(2).setCellValue("JUMLAH");
            headerRow.createCell(3).setCellValue("HARGA");
            headerRow.createCell(4).setCellValue("LABA");

            // Isi Data
            int rowIndex = 1;
            while (c.moveToNext()) {
                Row row = sheet.createRow(rowIndex++);
                // Pastikan indeks kolom sesuai dengan query database kamu
                row.createCell(0).setCellValue(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TANGGAL)));
                row.createCell(1).setCellValue(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NAMA)));
                row.createCell(2).setCellValue(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_JUMLAH)));
                row.createCell(3).setCellValue(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_HARGA_JUAL)));
                row.createCell(4).setCellValue(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_LABA)));
            }
            c.close();

            // Simpan File
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
            String fileName = "Laporan_Usaha_" + timeStamp + ".xls";

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            wb.write(fos);
            fos.close();

            Toast.makeText(this, "Sukses! Tersimpan di Download.", Toast.LENGTH_SHORT).show();

            // Coba buka file otomatis
            bukaFile(file, "application/vnd.ms-excel");

        } catch (Exception e) {
            Toast.makeText(this, "Gagal Export Excel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // ===========================
    // 2. LOGIKA EXPORT PDF (iText)
    // ===========================
    private void exportToPdf() {
        try {
            Cursor c = db.getAllTransaksi();
            if (c == null || c.getCount() == 0) {
                Toast.makeText(this, "Data Transaksi Kosong!", Toast.LENGTH_SHORT).show();
                return;
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
            String fileName = "Laporan_Usaha_" + timeStamp + ".pdf";
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Judul Dokumen
            Paragraph judul = new Paragraph("LAPORAN TRANSAKSI USAHA");
            judul.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(judul);

            document.add(new Paragraph("Tanggal Cetak: " + new SimpleDateFormat("dd MMMM yyyy HH:mm", new Locale("id", "ID")).format(new Date())));
            document.add(new Paragraph("\n"));

            // Tabel PDF (5 Kolom)
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            // Header Tabel
            table.addCell("Tanggal");
            table.addCell("Produk");
            table.addCell("Jml");
            table.addCell("Harga");
            table.addCell("Laba");

            // Isi Data
            while (c.moveToNext()) {
                table.addCell(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TANGGAL)));
                table.addCell(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NAMA)));
                table.addCell(String.valueOf(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_JUMLAH))));
                table.addCell(String.valueOf(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_HARGA_JUAL))));
                table.addCell(String.valueOf(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_LABA))));
            }
            c.close();

            document.add(table);
            document.close();

            Toast.makeText(this, "Sukses! Tersimpan di Download.", Toast.LENGTH_SHORT).show();
            bukaFile(file, "application/pdf");

        } catch (Exception e) {
            Toast.makeText(this, "Gagal Export PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // ===========================
    // UTILS: BUKA FILE
    // ===========================
    private void bukaFile(File file, String mimeType) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            // FileProvider dibutuhkan untuk Android 7.0+ (Nougat) ke atas
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "File tersimpan, tapi tidak ada aplikasi untuk membukanya.", Toast.LENGTH_LONG).show();
        }
    }
}
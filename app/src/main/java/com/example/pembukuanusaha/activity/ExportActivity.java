package com.example.pembukuanusaha.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.database.DatabaseHelper;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
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

    CardView cardExcel, cardPdf;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        cardExcel = findViewById(R.id.cardExcel);
        cardPdf   = findViewById(R.id.cardPdf);
        db        = new DatabaseHelper(this);

        cardExcel.setOnClickListener(v -> exportToExcel());
        cardPdf.setOnClickListener(v -> exportToPdf());
    }

    // ===========================
    // EXPORT EXCEL
    // ===========================
    private void exportToExcel() {
        try {
            Workbook wb = new HSSFWorkbook();
            Sheet sheet = wb.createSheet("Laporan Transaksi");

            // Header
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("TANGGAL");
            headerRow.createCell(1).setCellValue("NAMA PRODUK");
            headerRow.createCell(2).setCellValue("JUMLAH");
            headerRow.createCell(3).setCellValue("HARGA");
            headerRow.createCell(4).setCellValue("LABA");

            // Data
            Cursor c = db.getAllTransaksi();
            int rowIndex = 1;
            while (c != null && c.moveToNext()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(c.getString(0)); // Tanggal
                row.createCell(1).setCellValue(c.getString(1)); // Nama
                row.createCell(2).setCellValue(c.getInt(2));    // Jumlah
                row.createCell(3).setCellValue(c.getInt(3));    // Harga
                row.createCell(4).setCellValue(c.getInt(4));    // Laba
            }
            if (c != null) c.close();

            // Simpan File
            String fileName = "Laporan_Pembukuan_" + System.currentTimeMillis() + ".xls";
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            wb.write(fos);
            fos.close();

            Toast.makeText(this, "Berhasil! Cek folder Download: " + fileName, Toast.LENGTH_LONG).show();
            bukaFile(file, "application/vnd.ms-excel");

        } catch (Exception e) {
            Toast.makeText(this, "Gagal Export Excel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // ===========================
    // EXPORT PDF
    // ===========================
    private void exportToPdf() {
        try {
            String fileName = "Laporan_Pembukuan_" + System.currentTimeMillis() + ".pdf";
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Judul
            document.add(new Paragraph("LAPORAN TRANSAKSI"));
            document.add(new Paragraph("Dicetak pada: " + new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date())));
            document.add(new Paragraph("\n"));

            // Tabel
            PdfPTable table = new PdfPTable(5); // 5 Kolom
            table.addCell("Tanggal");
            table.addCell("Produk");
            table.addCell("Jml");
            table.addCell("Harga");
            table.addCell("Laba");

            // Data
            Cursor c = db.getAllTransaksi();
            while (c != null && c.moveToNext()) {
                table.addCell(c.getString(0));
                table.addCell(c.getString(1));
                table.addCell(String.valueOf(c.getInt(2)));
                table.addCell(String.valueOf(c.getInt(3)));
                table.addCell(String.valueOf(c.getInt(4)));
            }
            if (c != null) c.close();

            document.add(table);
            document.close();

            Toast.makeText(this, "Berhasil! Cek folder Download: " + fileName, Toast.LENGTH_LONG).show();
            bukaFile(file, "application/pdf");

        } catch (Exception e) {
            Toast.makeText(this, "Gagal Export PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Intent untuk membuka file setelah export
    private void bukaFile(File file, String mimeType) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            // Abaikan jika tidak ada aplikasi pembuka
        }
    }
}
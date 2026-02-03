package com.example.pembukuanusaha.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.database.DatabaseHelper;

// ===== iText 5 (ANDROID) =====
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

// ===== Excel =====
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;

public class ExportActivity extends AppCompatActivity {

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        db = new DatabaseHelper(this);

        Button btnPdf = findViewById(R.id.btnPdf);
        Button btnExcel = findViewById(R.id.btnExcel);

        btnPdf.setOnClickListener(v -> exportPdf());
        btnExcel.setOnClickListener(v -> exportExcel());
    }

    // ======================= EXPORT PDF =======================
    private void exportPdf() {
        try {
            File file = new File(getExternalFilesDir(null), "laporan_transaksi.pdf");

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            document.add(new Paragraph("LAPORAN TRANSAKSI USAHA\n\n"));

            PdfPTable table = new PdfPTable(5);
            table.addCell("Tanggal");
            table.addCell("Produk");
            table.addCell("Jumlah");
            table.addCell("Harga Jual");
            table.addCell("Laba");

            Cursor c = db.getAllTransaksi();
            while (c.moveToNext()) {
                table.addCell(c.getString(0));
                table.addCell(c.getString(1));
                table.addCell(String.valueOf(c.getInt(2)));
                table.addCell(String.valueOf(c.getInt(3)));
                table.addCell(String.valueOf(c.getInt(4)));
            }
            c.close();

            document.add(table);
            document.close();

            Toast.makeText(this, "PDF berhasil dibuat", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Gagal PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ======================= EXPORT EXCEL =======================
    private void exportExcel() {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Laporan Transaksi");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Tanggal");
            header.createCell(1).setCellValue("Produk");
            header.createCell(2).setCellValue("Jumlah");
            header.createCell(3).setCellValue("Harga Jual");
            header.createCell(4).setCellValue("Laba");

            Cursor c = db.getAllTransaksi();
            int rowNum = 1;

            while (c.moveToNext()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(c.getString(0));
                row.createCell(1).setCellValue(c.getString(1));
                row.createCell(2).setCellValue(c.getInt(2));
                row.createCell(3).setCellValue(c.getInt(3));
                row.createCell(4).setCellValue(c.getInt(4));
            }
            c.close();

            File file = new File(getExternalFilesDir(null), "laporan_transaksi.xlsx");
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            workbook.close();
            fos.close();

            Toast.makeText(this, "Excel berhasil dibuat", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Gagal Excel: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}

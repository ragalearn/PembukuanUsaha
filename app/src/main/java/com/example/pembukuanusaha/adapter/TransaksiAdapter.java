package com.example.pembukuanusaha.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.model.Transaksi;
import com.example.pembukuanusaha.utils.RupiahFormatter;

import java.util.List;

public class TransaksiAdapter extends RecyclerView.Adapter<TransaksiAdapter.ViewHolder> {

    private final List<Transaksi> list;

    public TransaksiAdapter(List<Transaksi> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Sesuaikan nama variabel dengan ID di XML agar tidak bingung
        TextView txtNamaProduk, txtJumlah, txtTotal, txtTanggal;

        public ViewHolder(View view) {
            super(view);
            // Hubungkan dengan ID yang BENAR di item_transaksi.xml
            txtNamaProduk = view.findViewById(R.id.txtNamaProduk);
            txtJumlah     = view.findViewById(R.id.txtJumlah);

            // PERBAIKAN DI SINI:
            // Ganti R.id.txtLaba menjadi R.id.txtTotal (Sesuai XML)
            txtTotal      = view.findViewById(R.id.txtTotal);

            // Tambahkan ini karena di XML ada txtTanggal
            txtTanggal    = view.findViewById(R.id.txtTanggal);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaksi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaksi t = list.get(position);

        // 1. Set Nama
        holder.txtNamaProduk.setText(t.getNamaProduk());

        // 2. Set Jumlah (Format: x 2 pcs)
        holder.txtJumlah.setText("x " + t.getJumlah() + " pcs");

        // 3. Set Tanggal (Fitur baru yang ada di XML)
        // Pastikan model Transaksi punya method getTanggal()
        if (t.getTanggal() != null) {
            holder.txtTanggal.setText(t.getTanggal());
        } else {
            holder.txtTanggal.setText("-");
        }

        // 4. Set Total Harga (Omzet)
        // Kita hitung total bayar: Harga Jual x Jumlah
        int totalBayar = t.getHargaJual() * t.getJumlah();
        holder.txtTotal.setText(RupiahFormatter.format(totalBayar));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
package com.example.pembukuanusaha.adapter;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.database.DatabaseHelper;
import com.example.pembukuanusaha.model.Produk;
import com.example.pembukuanusaha.session.SessionManager;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class ProdukAdapter extends RecyclerView.Adapter<ProdukAdapter.ViewHolder> {

    private final List<Produk> list;
    private final DatabaseHelper db;

    public ProdukAdapter(List<Produk> list, DatabaseHelper db) {
        this.list = list;
        this.db = db;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNama, txtHarga, txtStok, btnHapus;

        public ViewHolder(View view) {
            super(view);
            txtNama  = view.findViewById(R.id.txtNama);
            txtHarga = view.findViewById(R.id.txtHarga);
            txtStok  = view.findViewById(R.id.txtStok);
            btnHapus = view.findViewById(R.id.btnHapus);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_produk, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Produk p = list.get(position);

        holder.txtNama.setText(p.getNama());
        holder.txtHarga.setText(
                "Modal: Rp " + p.getHargaModal() + " | Jual: Rp " + p.getHargaJual()
        );
        holder.txtStok.setText("Stok: " + p.getStok());

        // =====================
        // SESSION & ROLE CHECK
        // =====================
        SessionManager session =
                new SessionManager(holder.itemView.getContext());

        // KASIR TIDAK BOLEH LIHAT TOMBOL HAPUS
        if (!session.isAdmin()) {
            holder.btnHapus.setVisibility(View.GONE);
            return;
        }

        // ADMIN BOLEH HAPUS
        holder.btnHapus.setVisibility(View.VISIBLE);

        // =====================
        // HAPUS DENGAN KONFIRMASI
        // =====================
        holder.btnHapus.setOnClickListener(v -> {

            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Hapus Produk")
                    .setMessage("Yakin ingin menghapus produk ini?")
                    .setPositiveButton("Ya", (dialog, which) -> {

                        db.deleteProduk(p.getId());
                        list.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);
                        notifyItemRangeChanged(adapterPosition, list.size());

                        Snackbar.make(
                                v,
                                "Produk berhasil dihapus",
                                Snackbar.LENGTH_LONG
                        ).show();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

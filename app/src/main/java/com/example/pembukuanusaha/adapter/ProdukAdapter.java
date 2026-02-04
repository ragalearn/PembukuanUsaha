package com.example.pembukuanusaha.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.activity.TambahProdukActivity;
import com.example.pembukuanusaha.database.DatabaseHelper;
import com.example.pembukuanusaha.model.Produk;
import com.example.pembukuanusaha.utils.RupiahFormatter;

import java.util.List;

public class ProdukAdapter extends RecyclerView.Adapter<ProdukAdapter.ViewHolder> {

    private final Context context;
    private final List<Produk> list;

    public ProdukAdapter(Context context, List<Produk> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_produk, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Produk p = list.get(position);

        holder.txtNama.setText(p.getNama());
        holder.txtStok.setText("Stok: " + p.getStok());
        holder.txtHarga.setText(RupiahFormatter.format(p.getHargaJual()));

        // 🔥 1. LOAD FOTO DENGAN GLIDE
        // Jika ada URL foto, load pakai Glide. Jika tidak, pakai icon default.
        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(p.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_inventory) // Gambar loading/default
                    .into(holder.imgProduk);
        } else {
            holder.imgProduk.setImageResource(R.drawable.ic_inventory);
        }

        // 🔥 2. TOMBOL OPSI (TITIK TIGA)
        holder.btnMoreOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnMoreOptions);
            // Tambahkan menu secara manual lewat kode (biar gak ribet bikin XML menu lagi)
            popup.getMenu().add(0, 1, 0, "Edit");
            popup.getMenu().add(0, 2, 0, "Hapus");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    // --- AKSI EDIT ---
                    Intent intent = new Intent(context, TambahProdukActivity.class);
                    intent.putExtra("extra_produk", p); // Kirim objek produk ke halaman edit
                    context.startActivity(intent);
                } else if (item.getItemId() == 2) {
                    // --- AKSI HAPUS ---
                    confirmDelete(p, position);
                }
                return true;
            });
            popup.show();
        });
    }

    private void confirmDelete(Produk produk, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Hapus Produk")
                .setMessage("Yakin ingin menghapus " + produk.getNama() + "?")
                .setPositiveButton("Ya, Hapus", (dialog, which) -> {
                    DatabaseHelper db = new DatabaseHelper(context);
                    boolean deleted = db.deleteProduk(produk.getId());
                    if (deleted) {
                        list.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, list.size());
                        Toast.makeText(context, "Produk dihapus", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Gagal menghapus", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNama, txtStok, txtHarga;
        ImageView imgProduk;
        ImageButton btnMoreOptions;

        public ViewHolder(View itemView) {
            super(itemView);
            txtNama = itemView.findViewById(R.id.txtNama);
            txtStok = itemView.findViewById(R.id.txtStok);
            txtHarga = itemView.findViewById(R.id.txtHarga);
            imgProduk = itemView.findViewById(R.id.imgProduk);
            btnMoreOptions = itemView.findViewById(R.id.btnMoreOptions);
        }
    }
}
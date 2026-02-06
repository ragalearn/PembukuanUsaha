package com.example.pembukuanusaha.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
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

        // 🔥 LOGIKA BARU: Decode Base64 ke Gambar
        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            try {
                // 1. Coba baca sebagai Base64 (Data Lokal)
                byte[] decodedString = Base64.decode(p.getImageUrl(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                // Tampilkan pakai Glide biar mulus
                Glide.with(context)
                        .load(decodedByte)
                        .centerCrop()
                        .placeholder(R.drawable.ic_inventory)
                        .into(holder.imgProduk);

            } catch (Exception e) {
                // 2. Jika gagal (mungkin data lama URL internet), load biasa
                Glide.with(context)
                        .load(p.getImageUrl())
                        .centerCrop()
                        .placeholder(R.drawable.ic_inventory)
                        .into(holder.imgProduk);
            }
        } else {
            holder.imgProduk.setImageResource(R.drawable.ic_inventory);
        }

        // TOMBOL OPSI (SAMA SEPERTI SEBELUMNYA)
        holder.btnMoreOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnMoreOptions);
            popup.getMenu().add(0, 1, 0, "Edit");
            popup.getMenu().add(0, 2, 0, "Hapus");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    Intent intent = new Intent(context, TambahProdukActivity.class);
                    intent.putExtra("extra_produk", p);
                    context.startActivity(intent);
                } else if (item.getItemId() == 2) {
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
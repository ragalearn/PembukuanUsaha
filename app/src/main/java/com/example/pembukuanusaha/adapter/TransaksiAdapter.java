package com.example.pembukuanusaha.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

        TextView txtNamaProduk, txtJumlah, txtLaba;

        public ViewHolder(View view) {
            super(view);
            txtNamaProduk = view.findViewById(R.id.txtNamaProduk);
            txtJumlah     = view.findViewById(R.id.txtJumlah);
            txtLaba       = view.findViewById(R.id.txtLaba);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaksi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Transaksi t = list.get(position);

        holder.txtNamaProduk.setText(t.getNamaProduk());
        holder.txtJumlah.setText("Jumlah: " + t.getJumlah());
        holder.txtLaba.setText("Laba: " + RupiahFormatter.format(t.getLaba()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

package com.itstep.directoriocontactos.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.itstep.directoriocontactos.R;
import com.itstep.directoriocontactos.modelo.Contacto;

import java.util.List;

public class ContactoAdapter extends BaseAdapter {

    private final Context context;
    private List<Contacto> contactos;

    public ContactoAdapter(Context context, List<Contacto> contactos) {
        this.context = context;
        this.contactos = contactos;
    }

    @Override
    public int getCount() {
        return contactos.size();
    }

    @Override
    public Contacto getItem(int position) {
        return contactos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return contactos.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_contacto, parent, false);
        }

        Contacto c = contactos.get(position);
        TextView tvNombre = convertView.findViewById(R.id.tvNombre);
        TextView tvTelefono = convertView.findViewById(R.id.tvTelefono);
        TextView tvCategoria = convertView.findViewById(R.id.tvCategoria);
        tvNombre.setText(c.getNombre());
        tvTelefono.setText(c.getTelefono());
        tvCategoria.setText(c.getCategoria());

        return convertView;
    }

    public void actualizarLista(List<Contacto> nuevos) {
        this.contactos = nuevos;
        notifyDataSetChanged();
    }
}

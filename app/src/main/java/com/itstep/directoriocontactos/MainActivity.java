package com.itstep.directoriocontactos;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.itstep.directoriocontactos.adaptadores.ContactoAdapter;
import com.itstep.directoriocontactos.datos.ContactoDBHelper;
import com.itstep.directoriocontactos.modelo.Contacto;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_ID_CONTACTO = "id_contacto";

    private final String[] categoriasFiltro = {
            ContactoDBHelper.CATEGORIA_TODAS, "Familia", "Trabajo", "Amigos"};

    private ContactoDBHelper dbHelper;
    private ContactoAdapter adapter;
    private EditText etBusqueda;
    private Spinner spFiltroCategoria;
    private ListView lvContactos;
    private TextView tvListaVacia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new ContactoDBHelper(this);
        etBusqueda = findViewById(R.id.etBusqueda);
        spFiltroCategoria = findViewById(R.id.spFiltroCategoria);
        lvContactos = findViewById(R.id.lvContactos);
        tvListaVacia = findViewById(R.id.tvListaVacia);
        Button btnAgregar = findViewById(R.id.btnAgregar);

        adapter = new ContactoAdapter(this, new ArrayList<>());
        lvContactos.setAdapter(adapter);

        ArrayAdapter<String> adapterFiltro = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoriasFiltro);
        adapterFiltro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFiltroCategoria.setAdapter(adapterFiltro);

        etBusqueda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cargarContactos();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        spFiltroCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cargarContactos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        lvContactos.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, EditarContactoActivity.class);
            intent.putExtra(EXTRA_ID_CONTACTO, (int) id);
            startActivity(intent);
        });

        lvContactos.setOnItemLongClickListener((parent, view, position, id) -> {
            Contacto c = adapter.getItem(position);
            new AlertDialog.Builder(this)
                    .setMessage("¿Eliminar a " + c.getNombre() + "?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        dbHelper.eliminarContacto(c.getId());
                        cargarContactos();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
            return true;
        });

        btnAgregar.setOnClickListener(v ->
                startActivity(new Intent(this, EditarContactoActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarContactos();
    }

    // Consulta con la búsqueda y el filtro actuales y refresca la pantalla
    private void cargarContactos() {
        String texto = etBusqueda.getText().toString().trim();
        String categoria = (String) spFiltroCategoria.getSelectedItem();
        List<Contacto> contactos = dbHelper.obtenerContactos(texto, categoria);
        adapter.actualizarLista(contactos);

        boolean vacia = contactos.isEmpty();
        lvContactos.setVisibility(vacia ? View.GONE : View.VISIBLE);
        tvListaVacia.setVisibility(vacia ? View.VISIBLE : View.GONE);
    }
}

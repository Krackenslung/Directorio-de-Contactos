package com.itstep.directoriocontactos;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.itstep.directoriocontactos.datos.ContactoDBHelper;
import com.itstep.directoriocontactos.modelo.Contacto;

public class EditarContactoActivity extends AppCompatActivity {

    private final String[] categorias = {"Familia", "Trabajo", "Amigos"};

    private ContactoDBHelper dbHelper;
    private EditText etNombre;
    private EditText etTelefono;
    private EditText etCorreo;
    private Spinner spCategoria;
    private int idContacto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_contacto);

        dbHelper = new ContactoDBHelper(this);
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etCorreo = findViewById(R.id.etCorreo);
        spCategoria = findViewById(R.id.spCategoria);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        ArrayAdapter<String> adapterCategorias = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categorias);
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoria.setAdapter(adapterCategorias);

        idContacto = getIntent().getIntExtra(MainActivity.EXTRA_ID_CONTACTO, -1);
        if (idContacto != -1) {
            cargarContacto();
        }

        btnGuardar.setOnClickListener(v -> guardar());
    }

    // Precarga los campos en modo edición
    private void cargarContacto() {
        Contacto c = dbHelper.obtenerContactoPorId(idContacto);
        if (c == null) {
            Toast.makeText(this, "El contacto ya no existe", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        etNombre.setText(c.getNombre());
        etTelefono.setText(c.getTelefono());
        etCorreo.setText(c.getCorreo());
        for (int i = 0; i < categorias.length; i++) {
            if (categorias[i].equals(c.getCategoria())) {
                spCategoria.setSelection(i);
                break;
            }
        }
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String categoria = (String) spCategoria.getSelectedItem();

        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }
        if (telefono.isEmpty()) {
            Toast.makeText(this, "El teléfono es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!telefono.matches("\\d+") || telefono.length() < 10) {
            Toast.makeText(this, "El teléfono debe tener 10 dígitos numéricos",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Contacto c = new Contacto(idContacto, nombre, telefono, correo, categoria);
        if (idContacto == -1) {
            dbHelper.insertarContacto(c);
            Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.actualizarContacto(c);
            Toast.makeText(this, "Contacto actualizado", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}

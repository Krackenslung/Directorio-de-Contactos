package com.itstep.directoriocontactos.datos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.itstep.directoriocontactos.modelo.Contacto;

import java.util.ArrayList;
import java.util.List;

public class ContactoDBHelper extends SQLiteOpenHelper {

    public static final String CATEGORIA_TODAS = "Todas";

    private static final String NOMBRE_BD = "directorio.db";
    private static final int VERSION_BD = 1;
    private static final String TABLA = "contactos";

    public ContactoDBHelper(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLA + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "telefono TEXT NOT NULL, " +
                "correo TEXT, " +
                "categoria TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA);
        onCreate(db);
    }

    public long insertarContacto(Contacto c) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nombre", c.getNombre());
        valores.put("telefono", c.getTelefono());
        valores.put("correo", c.getCorreo());
        valores.put("categoria", c.getCategoria());
        long id = db.insert(TABLA, null, valores);
        db.close();
        return id;
    }

    public int actualizarContacto(Contacto c) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nombre", c.getNombre());
        valores.put("telefono", c.getTelefono());
        valores.put("correo", c.getCorreo());
        valores.put("categoria", c.getCategoria());
        int filas = db.update(TABLA, valores, "id = ?",
                new String[]{String.valueOf(c.getId())});
        db.close();
        return filas;
    }

    public int eliminarContacto(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int filas = db.delete(TABLA, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return filas;
    }

    public Contacto obtenerContactoPorId(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLA, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        Contacto c = null;
        if (cursor.moveToFirst()) {
            c = leerContacto(cursor);
        }
        cursor.close();
        db.close();
        return c;
    }

    // Arma el WHERE según lo que el usuario haya escrito o filtrado
    public List<Contacto> obtenerContactos(String textoBusqueda, String categoria) {
        List<Contacto> lista = new ArrayList<>();
        StringBuilder where = new StringBuilder();
        List<String> args = new ArrayList<>();

        if (textoBusqueda != null && !textoBusqueda.isEmpty()) {
            where.append("nombre LIKE ?");
            args.add("%" + textoBusqueda + "%");
        }
        if (categoria != null && !categoria.equals(CATEGORIA_TODAS)) {
            if (where.length() > 0) {
                where.append(" AND ");
            }
            where.append("categoria = ?");
            args.add(categoria);
        }

        String selection = where.length() > 0 ? where.toString() : null;
        String[] selectionArgs = args.isEmpty() ? null : args.toArray(new String[0]);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLA, null, selection, selectionArgs,
                null, null, "nombre COLLATE NOCASE ASC");
        while (cursor.moveToNext()) {
            lista.add(leerContacto(cursor));
        }
        cursor.close();
        db.close();
        return lista;
    }

    private Contacto leerContacto(Cursor cursor) {
        return new Contacto(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
                cursor.getString(cursor.getColumnIndexOrThrow("correo")),
                cursor.getString(cursor.getColumnIndexOrThrow("categoria")));
    }
}

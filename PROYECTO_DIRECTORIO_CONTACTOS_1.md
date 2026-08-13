# Proyecto: Directorio de Contactos Personal (Android)

Proyecto escolar de la asignatura **Programación Móvil (DS802)** — IT STEP Academy.
Integrantes: Jesús Martín y José Luis.

> **Instrucción para Claude Code:** genera la app completa siguiendo esta especificación,
> de una sola vez y sin preguntar. Está escrita para que el resultado sea siempre el mismo.
>
> **Qué NO hacer:**
> - No levantes el emulador, no instales el APK, no hagas capturas ni demos.
> - No pruebes la app en ningún dispositivo. Compilar con `gradlew assembleDebug`
>   para verificar que no hay errores es lo único que se espera.
> - **No siembres datos de ejemplo.** Nada de "Ana Torres", "Carlos Peña" ni similares.
>   La app arranca con la tabla vacía y los contactos los agrega el usuario.
> - No agregues funciones que no estén aquí (nada de importar contactos del
>   teléfono, permisos, exportar, favoritos, etc.).
>
> **Estilo de código:** simple y didáctico, nivel escolar. Sin librerías externas,
> sin arquitecturas complejas (nada de MVVM, Room, Hilt, Coroutines).
> **Comentarios en español y al mínimo: como mucho una línea corta por método,
> solo donde el nombre no se explique solo.** Nada de bloques Javadoc largos.

---

## 0. Punto de partida

La carpeta `MyApplication/` ya existe con un proyecto de Android Studio generado.
**Puede contener código de otra app** (por ejemplo una que lee la agenda del teléfono
con `ContactsContract` y `RecyclerView`). Ese código **se reemplaza por completo**:
borra el paquete viejo y sus tests, no intentes adaptarlo.

Verifica también que `local.properties` apunte a un SDK que exista en el equipo.

---

## 1. Stack técnico (obligatorio)

- **Lenguaje:** Java
- **Nombre del proyecto:** "Directorio de Contactos Personal"
  (`rootProject.name` en `settings.gradle.kts` y `app_name` en `strings.xml`)
- **Paquete:** `com.itstep.directoriocontactos` (también `namespace` y `applicationId`)
- **compileSdk:** 36 · **minSdk:** 24 · **targetSdk:** 34
- **Base de datos:** SQLite con `SQLiteOpenHelper` (nada de Room)
- **UI:** Layouts XML clásicos, `ListView` con adaptador personalizado
- **Navegación:** `Intent` explícito + `startActivity()`; la lista se refresca en `onResume()`
- **Sin permisos:** el manifest no declara ningún `uses-permission`
- **Sin** ViewBinding, sin Jetpack Compose, sin RecyclerView, sin FloatingActionButton

El tema (`Theme.MyApplication`) es **NoActionBar**: no hay barra de título, así que
las pantallas no llevan título visible y no se usa `setTitle()`.

---

## 2. Estructura de archivos esperada

```
app/src/main/java/com/itstep/directoriocontactos/
├── MainActivity.java            // Lista, búsqueda, filtro y borrado
├── EditarContactoActivity.java  // Alta y edición
├── modelo/
│   └── Contacto.java            // POJO
├── datos/
│   └── ContactoDBHelper.java    // SQLiteOpenHelper + CRUD
└── adaptadores/
    └── ContactoAdapter.java     // BaseAdapter para el ListView

app/src/main/res/layout/
├── activity_main.xml
├── activity_editar_contacto.xml
└── item_contacto.xml
```

Los tests de ejemplo (`ExampleUnitTest`, `ExampleInstrumentedTest`) se mueven al paquete
nuevo; el instrumentado debe afirmar `"com.itstep.directoriocontactos"`.

**Regla importante de la rúbrica:** ninguna sentencia SQL debe aparecer dentro de las
Activities. Todo el SQL vive en `ContactoDBHelper`.

---

## 3. Base de datos

- Nombre del archivo: `directorio.db`
- Versión: `1`
- Tabla: `contactos`

| Campo       | Tipo    | Restricción                       |
|-------------|---------|-----------------------------------|
| `id`        | INTEGER | PRIMARY KEY AUTOINCREMENT         |
| `nombre`    | TEXT    | NOT NULL                          |
| `telefono`  | TEXT    | NOT NULL                          |
| `correo`    | TEXT    | puede ser vacío                   |
| `categoria` | TEXT    | NOT NULL (Familia/Trabajo/Amigos) |

`onUpgrade`: `DROP TABLE IF EXISTS contactos` y volver a llamar `onCreate`.

---

## 4. Clases y métodos requeridos

### `Contacto.java`
Atributos privados: `id` (int), `nombre`, `telefono`, `correo`, `categoria`.
Constructor vacío, constructor completo, getters y setters.

### `ContactoDBHelper.java` (extiende `SQLiteOpenHelper`)
Usa `ContentValues` para insertar/actualizar y `Cursor` para leer. Cierra cursores y BD.

```java
long insertarContacto(Contacto c);
int actualizarContacto(Contacto c);
int eliminarContacto(int id);
Contacto obtenerContactoPorId(int id);          // null si no existe
List<Contacto> obtenerContactos(String textoBusqueda, String categoria);
```

Expón la constante pública `CATEGORIA_TODAS = "Todas"` para que la Activity no
escriba ese literal a mano.

`obtenerContactos` construye una **consulta dinámica con WHERE**:
- Si `textoBusqueda` no está vacío → `nombre LIKE ?` con `%texto%`
- Si `categoria` no es `"Todas"` → se añade `AND categoria = ?`
- Si no hay ninguna condición → `selection` y `selectionArgs` van en `null`
- Ordenar por `nombre ASC`
- Usar `selectionArgs` (nunca concatenar valores en el SQL)

### `ContactoAdapter.java` (extiende `BaseAdapter`)
Recibe `Context` y `List<Contacto>`. Infla `item_contacto.xml` y muestra nombre,
teléfono y categoría. `getItem()` devuelve el `Contacto` y `getItemId()` su id real.
Método público `actualizarLista(List<Contacto>)` que reemplaza los datos y llama a
`notifyDataSetChanged()`.

---

## 5. Pantalla 1 — `MainActivity`

**Layout `activity_main.xml`** (LinearLayout vertical, padding 16dp):
- `EditText` de búsqueda (hint: "Buscar por nombre")
- `Spinner` de filtro: **Todas, Familia, Trabajo, Amigos**
- `ListView` de contactos (`layout_weight="1"`)
- `TextView` de mensaje vacío, mismo peso, `visibility="gone"`: "No hay contactos que mostrar"
- `Button` "Agregar contacto" a todo el ancho, abajo

**Comportamiento:**
1. La primera vez la tabla está vacía, así que al abrir se ve el mensaje de lista vacía.
2. En `onResume()` recargar la lista para que se refresque al volver de la edición.
3. `addTextChangedListener` en el `EditText` → en `onTextChanged` volver a consultar
   (búsqueda en tiempo real, sin botón).
4. `setOnItemSelectedListener` en el Spinner → volver a consultar aplicando texto +
   categoría **combinados**.
5. Método privado `cargarContactos()` que lee el texto y la categoría actuales, llama a
   `obtenerContactos(texto, categoria)`, actualiza el adapter y alterna la visibilidad
   entre el `ListView` y el `TextView` vacío.
6. Click corto en un ítem → `Intent` a `EditarContactoActivity` con
   `putExtra("id_contacto", id)`. Define la clave como constante pública
   `EXTRA_ID_CONTACTO` y reutilízala en la otra Activity.
7. Click largo → `AlertDialog` "¿Eliminar a [nombre]?" con botones **Eliminar / Cancelar**.
   Solo al confirmar se llama a `eliminarContacto` y se recarga. Devolver `true`.
8. Botón agregar → `Intent` a `EditarContactoActivity` **sin** extras.

Las categorías del filtro se definen como `String[]` en la Activity y se cargan con
`ArrayAdapter` sobre `android.R.layout.simple_spinner_item`.

---

## 6. Pantalla 2 — `EditarContactoActivity`

**Layout `activity_editar_contacto.xml`** (LinearLayout vertical, padding 16dp), con una
etiqueta `TextView` en negrita encima de cada campo:
- `EditText` Nombre (`inputType="textPersonName"`)
- `EditText` Teléfono (`inputType="phone"`)
- `EditText` Correo (`inputType="textEmailAddress"`)
- `Spinner` Categoría con `ArrayAdapter` sobre un `String[]`: Familia, Trabajo, Amigos
- `Button` Guardar

**Comportamiento:**
1. En `onCreate`, leer `getIntent().getIntExtra(EXTRA_ID_CONTACTO, -1)`.
   - Si es `-1` → modo **alta** (campos vacíos).
   - Si no → modo **edición**: cargar con `obtenerContactoPorId`, precargar los campos y
     seleccionar la posición correcta del Spinner. Si devuelve `null`, mostrar Toast
     "El contacto ya no existe" y `finish()`.
2. Al pulsar Guardar, validar en este orden y mostrar `Toast` en el primer error:
   - Nombre vacío → "El nombre es obligatorio"
   - Teléfono vacío → "El teléfono es obligatorio"
   - Teléfono con caracteres no numéricos **o** con menos de 10 dígitos →
     "El teléfono debe tener 10 dígitos numéricos"
3. Si todo es válido: `insertarContacto` o `actualizarContacto` según el modo,
   `Toast` de confirmación y `finish()` para regresar a la lista.

El correo es opcional y no se valida.

---

## 7. Criterios de aceptación

Los prueba **el estudiante** a mano antes de entregar. Claude no ejecuta la app.

- [ ] Puedo crear, ver, editar y eliminar un contacto sin que la app crashee.
- [ ] Al eliminar aparece primero el `AlertDialog`; Cancelar no borra nada.
- [ ] Escribir en el buscador filtra la lista letra por letra, sin presionar ningún botón.
- [ ] El filtro de categoría funciona solo y **también combinado** con la búsqueda.
- [ ] Al instalar por primera vez la lista sale vacía, con su mensaje.
- [ ] Si no hay resultados aparece el mensaje de lista vacía y desaparece al limpiar.
- [ ] Al volver de editar, la lista principal ya muestra el cambio.
- [ ] Guardar con nombre o teléfono vacío muestra Toast y **no** guarda.
- [ ] Guardar con teléfono de 5 dígitos o con letras muestra Toast y **no** guarda.
- [ ] La app se cierra y se reabre sin perder los datos (están en SQLite).

---

## 8. Orden sugerido de implementación

1. `Contacto.java` y `ContactoDBHelper.java` (crear tabla + CRUD).
2. `item_contacto.xml` y `ContactoAdapter`.
3. `MainActivity` con la lista y la navegación por Intents.
4. `EditarContactoActivity` con alta, edición y validaciones.
5. Búsqueda por nombre y filtro por categoría combinados.
6. Eliminación con `AlertDialog` y mensaje de lista vacía.
7. `gradlew assembleDebug` y corregir lo que salga. Fin.

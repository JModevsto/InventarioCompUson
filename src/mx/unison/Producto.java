package mx.unison;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Producto {
    
    // Usamos StringProperty para que JavaFX pueda observar y actualizar la TableView
    private final StringProperty id;
    private final StringProperty nombre;
    private final StringProperty precio;
    private final StringProperty cantidad;
    private final StringProperty departamento;
    private final StringProperty almacen; // Almacena el ID del almacén (FK) o el nombre legible
    private final StringProperty fechaCreacion;
    private final StringProperty fechaModificacion;
    private final StringProperty ultimoUsuario;

    public Producto(String id, String nombre, String precio, String cantidad, String departamento, String almacen, String fechaCreacion, String fechaModificacion, String ultimoUsuario) {
        this.id = new SimpleStringProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.precio = new SimpleStringProperty(precio);
        this.cantidad = new SimpleStringProperty(cantidad);
        this.departamento = new SimpleStringProperty(departamento);
        this.almacen = new SimpleStringProperty(almacen);
        this.fechaCreacion = new SimpleStringProperty(fechaCreacion);
        this.fechaModificacion = new SimpleStringProperty(fechaModificacion);
        this.ultimoUsuario = new SimpleStringProperty(ultimoUsuario);
    }

    // --- GETTERS (Estos son los que tu controlador busca) ---
    public String getId() {
        return id.get();
    }
    public String getNombre() {
        return nombre.get();
    }
    // ... (Otros getters necesarios, aunque no causaron error explícito)
    public String getPrecio() {
        return precio.get();
    }
    public String getCantidad() {
        return cantidad.get();
    }
    public String getDepartamento() {
        return departamento.get();
    }
    public String getAlmacen() {
        return almacen.get();
    }
    public String getFechaCreacion() {
        return fechaCreacion.get();
    }
    public String getFechaModificacion() {
        return fechaModificacion.get();
    }
    public String getUltimoUsuario() {
        return ultimoUsuario.get();
    }

    // --- PROPERTY GETTERS (Usados por TableColumn.setCellValueFactory) ---
    public StringProperty idProperty() {
        return id;
    }
    public StringProperty nombreProperty() {
        return nombre;
    }
    public StringProperty precioProperty() {
        return precio;
    }
    public StringProperty cantidadProperty() {
        return cantidad;
    }
    public StringProperty departamentoProperty() {
        return departamento;
    }
    public StringProperty almacenProperty() {
        return almacen;
    }
    public StringProperty fechaCreacionProperty() {
        return fechaCreacion;
    }
    public StringProperty fechaModificacionProperty() {
        return fechaModificacion;
    }
    public StringProperty ultimoUsuarioProperty() {
        return ultimoUsuario;
    }
}
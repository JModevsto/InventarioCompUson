package mx.unison;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Clase modelo para representar un Almacén en la interfaz de usuario.
 * Utiliza JavaFX Properties para permitir la actualización automática en la TableView.
 */
public class Almacen {
    
    // Propiedades
    private final StringProperty id;
    private final StringProperty nombre;
    private final StringProperty fechaCreacion;
    private final StringProperty fechaModificacion;
    private final StringProperty ultimoUsuario;
    
    // Constructor con los 5 campos de la tabla 'almacenes' (ID, Nombre y Auditoría)
    public Almacen(String id, String nombre, String fechaCreacion, String fechaModificacion, String ultimoUsuario) {
        this.id = new SimpleStringProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.fechaCreacion = new SimpleStringProperty(fechaCreacion);
        this.fechaModificacion = new SimpleStringProperty(fechaModificacion);
        this.ultimoUsuario = new SimpleStringProperty(ultimoUsuario);
    }
    
    // Getters
    public String getId() { return id.get(); } // Devuelve String
    public String getNombre() { return nombre.get(); }
    public String getFechaCreacion() { return fechaCreacion.get(); }
    public String getFechaModificacion() { return fechaModificacion.get(); }
    public String getUltimoUsuario() { return ultimoUsuario.get(); }
    
    // Setters
    public void setNombre(String nombre) { this.nombre.set(nombre); }
    
    // Property Getters (Necesarios para la TableView)
    public StringProperty idProperty() { return id; }
    public StringProperty nombreProperty() { return nombre; }
    public StringProperty fechaCreacionProperty() { return fechaCreacion; }
    public StringProperty fechaModificacionProperty() { return fechaModificacion; }
    public StringProperty ultimoUsuarioProperty() { return ultimoUsuario; }
    
    /**
     * Devuelve el ID como Integer, útil para el controlador de formulario. (Método de utilidad)
     */
    public int getIdAsInt() {
        try {
            // Convierte el ID (String) a Integer
            return Integer.parseInt(id.get());
        } catch (NumberFormatException e) {
            return -1; 
        }
    }
}
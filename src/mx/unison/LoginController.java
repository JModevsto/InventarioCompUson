package mx.unison;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.event.ActionEvent;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.IOException;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LoginController {
    
    // CLASE INTERNA: Para devolver el hash y el rol de la BD
    private class CredencialInfo {
        String hashAlmacenado;
        String rolUsuario;

        public CredencialInfo(String hash, String rol) {
            this.hashAlmacenado = hash;
            this.rolUsuario = rol;
        }
    }

    // 1. Inyectar los componentes de la vista (deben coincidir con el fx:id)
    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtPassword;
    
    @FXML
    private Button btnLogin; 

    // 2. Método de acción para el botón de Iniciar Sesión (debe coincidir con On Action)
    @FXML
    private void handleLogin(ActionEvent event) {
        
        // a) Obtener los datos ingresados por el usuario
        String nombreUsuario = txtUsuario.getText();
        String passwordPlana = txtPassword.getText();

        if (nombreUsuario.isEmpty() || passwordPlana.isEmpty()) {
            mostrarAlertaError("Campos Vacíos", "Por favor, ingrese usuario y contraseña.");
            return;
        }

        // b) Aquí irá la lógica de conexión a la BD y verificación
        try {
            // 3. LLAMAR A LA LÓGICA DE VERIFICACIÓN
            CredencialInfo info = obtenerCredencialInfo(nombreUsuario, passwordPlana); 

            if (info != null) {
                
                // ASIGNACIÓN CLAVE: Usamos SessionContext para guardar el nombre y el rol
                SessionContext.setCurrentUserName(nombreUsuario); 
                SessionContext.setCurrentUserRole(info.rolUsuario); 
                
                // Actualizamos la hora de inicio (Fix Issue 1 & 3)
                actualizarUltimoInicio(nombreUsuario); 
                
                // Redirigir a la vista principal
                mx.unison.InventarioApp.mostrarVista("Inicio.fxml"); 

            } else {
                mostrarAlertaError("Credenciales Incorrectas", "Usuario o contraseña incorrecta. Por favor, intente de nuevo.");
            }
            
        } catch (SQLException e) {
            System.err.println("Error de base de datos durante el login: " + e.getMessage());
             mostrarAlertaError("Error de Base de Datos", "Ocurrió un error al intentar iniciar sesión.");
            
        } catch (IOException e) { 
            System.err.println("Error al cargar la vista de Inicio: " + e.getMessage());
             mostrarAlertaError("Error de Sistema", "No se pudo cargar la ventana principal.");
        }
    }
    
    private CredencialInfo obtenerCredencialInfo(String usuario, String password) throws SQLException {
        CredencialInfo info = obtenerHashYRolDeBD(usuario); 
        
        if (info != null) {
            if (Seguridad.verificarPassword(password, info.hashAlmacenado)) {
                return info; 
            }
        }
        return null; 
    }
    
    private CredencialInfo obtenerHashYRolDeBD(String usuario) throws SQLException {
        CredencialInfo info = null;
        
        // SELECCIONAMOS 'password' Y 'rol'
        String sql = "SELECT password, rol FROM usuarios WHERE LOWER(nombre) = LOWER(?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario); 
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashAlmacenado = rs.getString("password");
                    String rolUsuario = rs.getString("rol");
                    info = new CredencialInfo(hashAlmacenado, rolUsuario);
                }
            }
        }
        
        return info;
    }
    
    /**
     * Actualiza la fecha y hora de último inicio de sesión para el usuario.
     * Implementa la lógica de la zona horaria y búsqueda case-insensitive.
     * @param nombreUsuario El nombre del usuario que ha iniciado sesión.
     */
    private void actualizarUltimoInicio(String nombreUsuario) throws SQLException {
        
        // --- Fix Issue 3: Usar Zona Horaria Local (Sonora/Arizona: GMT-7/MST) ---
        ZoneId sonoranZone = ZoneId.of("America/Phoenix");
        LocalDateTime now = LocalDateTime.now(sonoranZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String localizedNow = now.format(formatter);
        
        // --- Fix Issue 1: Usar LOWER() en la búsqueda para evitar problemas de mayúsculas/minúsculas ---
        String sql = "UPDATE usuarios SET fecha_hora_ultimo_inicio = ? WHERE LOWER(nombre) = LOWER(?)";

        try (Connection conn = mx.unison.DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, localizedNow); // Hora localizada
            pstmt.setString(2, nombreUsuario); // Usuario que inició sesión
            
            int filasActualizadas = pstmt.executeUpdate(); 
            
            if (filasActualizadas > 0) {
                System.out.println("DEBUG: Se actualizó la hora de último inicio para el usuario: " + nombreUsuario + " (" + localizedNow + ")");
            } else {
                System.err.println("ADVERTENCIA: No se pudo actualizar la hora de último inicio. Usuario '" + nombreUsuario + "' no encontrado.");
            }
        }
    }
    
    private void mostrarAlertaError(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null); 
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
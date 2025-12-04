package mx.unison;

/**
 * Clase estática para manejar el contexto de la sesión actual, 
 * como el nombre y el rol del usuario que ha iniciado sesión.
 */
public class SessionContext {

    // Variable estática para el nombre de usuario (usado en auditoría)
    // Inicializado con un valor que indica que nadie ha iniciado sesión correctamente.
    private static String currentUserName = "SIN SESIÓN (Admin Default)"; 
    
    // Variable estática para el rol de usuario (usado para permisos)
    private static String currentUserRole = "INVITADO"; 

    /**
     * Obtiene el nombre del usuario actual.
     * @return El nombre o rol del usuario.
     */
    public static String getCurrentUserName() {
        return currentUserName;
    }

    /**
     * Establece el nombre del usuario actual, típicamente después del login.
     * Este valor se usa para los campos de auditoría ('ultimo_usuario_en_modificar').
     * @param userName El nuevo nombre/rol del usuario.
     */
    public static void setCurrentUserName(String userName) {
        if (userName != null && !userName.trim().isEmpty()) {
            SessionContext.currentUserName = userName;
        } else {
            SessionContext.currentUserName = "Usuario Desconocido";
        }
    }
    
    /**
     * Obtiene el rol del usuario actual.
     * @return El rol (e.g., "Admin", "Productos").
     */
    public static String getCurrentUserRole() {
        return currentUserRole;
    }

    /**
     * Establece el rol del usuario actual, típicamente después del login.
     * @param userRole El rol del usuario.
     */
    public static void setCurrentUserRole(String userRole) {
        if (userRole != null && !userRole.trim().isEmpty()) {
            SessionContext.currentUserRole = userRole;
        } else {
            SessionContext.currentUserRole = "INVITADO";
        }
    }
}
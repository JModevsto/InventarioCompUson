package mx.unison;
import org.mindrot.jbcrypt.BCrypt;

public class GeneradorHash {
    public static void main(String[] args) {
        // --- Usuario 1 ---
        String passU1 = "admin23"; // <--- CAMBIA ESTO por la clave real que usarás
        String hashU1 = Seguridad.generarHash(passU1);
        System.out.println("Usuario 1 (ADMIN):");
        System.out.println("Hash a guardar: " + hashU1);
        System.out.println("----------------------------------------");

        // --- Usuario 2 ---
        String passU2 = "productos19"; // <--- CAMBIA ESTO
        String hashU2 = Seguridad.generarHash(passU2);
        System.out.println("Usuario 2 (PRODUCTOS):");
        System.out.println("Hash a guardar: " + hashU2);
        System.out.println("----------------------------------------");

        // --- Usuario 3 ---
        String passU3 = "almacenes11"; // <--- CAMBIA ESTO
        String hashU3 = Seguridad.generarHash(passU3);
        System.out.println("Usuario 3 (ALMACENES):");
        System.out.println("Hash a guardar: " + hashU3);
        System.out.println("----------------------------------------");
    }
}

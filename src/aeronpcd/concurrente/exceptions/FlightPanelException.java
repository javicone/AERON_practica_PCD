package aeronpcd.concurrente.exceptions;

/**
 * Excepción lanzada cuando no se puede leer o escribir el panel de vuelos JSON.
 * REQUISITO PRÁCTICA 6: Lectura del panel de vuelos.
 */
public class FlightPanelException extends Exception {
    
    private final String filePath;
    
    public FlightPanelException() {
        super("No se ha actualizado el panel de vuelos. Fichero JSON no encontrado");
        this.filePath = null;
    }
    
    public FlightPanelException(String filePath) {
        super("No se ha actualizado el panel de vuelos. Fichero JSON no encontrado");
        this.filePath = filePath;
    }
    
    public FlightPanelException(String filePath, Throwable cause) {
        super("No se ha actualizado el panel de vuelos. Fichero JSON no encontrado", cause);
        this.filePath = filePath;
    }
    
    public String getFilePath() {
        return filePath;
    }
}

package aeronpcd.concurrente.exceptions;

/**
 * Excepción lanzada cuando no se puede crear o escribir en el archivo de log.
 * REQUISITO PRÁCTICA 6: Escritura de log incorrecta.
 */
public class LogWriteException extends Exception {
    
    private final String fileName;
    
    public LogWriteException(String fileName) {
        super("No se ha encontrado el archivo de log " + fileName + ".log");
        this.fileName = fileName;
    }
    
    public LogWriteException(String fileName, Throwable cause) {
        super("No se ha encontrado el archivo de log " + fileName + ".log", cause);
        this.fileName = fileName;
    }
    
    public String getFileName() {
        return fileName;
    }
}

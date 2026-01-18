package aeronpcd.concurrente.exceptions;

/**
 * Excepción lanzada cuando no se puede escribir el resumen de simulación CSV.
 * REQUISITO PRÁCTICA 6: Escritura de CSV.
 */
public class CSVWriteException extends Exception {
    
    private final String fileName;
    
    public CSVWriteException(String fileName) {
        super("Error al escribir el resumen de la simulación. No se ha podido guardar en el fichero " + fileName + ".csv");
        this.fileName = fileName;
    }
    
    public CSVWriteException(String fileName, Throwable cause) {
        super("Error al escribir el resumen de la simulación. No se ha podido guardar en el fichero " + fileName + ".csv", cause);
        this.fileName = fileName;
    }
    
    public String getFileName() {
        return fileName;
    }
}

package aeronpcd.secuencial.util;

import aeronpcd.concurrente.exceptions.LogWriteException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    /**
     * PrintWriter para escribir en el archivo de log.
     * Se inicializa en el método setup().
     */
    private static PrintWriter writer;
    
    /**
     * Bandera que indica si el sistema de logs ha sido inicializado.
     */
    private static boolean isInitialized = false;

    /**
     * Inicializa el sistema de logs para la simulación.
     * Crea los directorios necesarios y abre el archivo de log con el formato
     * establecido: aeron-{MODE}-{nAV}-{nPIS}-{nPUE}-{nOPE}-{timestamp}.log
     * 
     * La carpeta de logs se divide en "secuencial/" o "concurrent/" según el modo.
     * Este método debe llamarse una única vez al inicio de la simulación.
     * 
     * @param mode Modo de ejecución: "CONCURRENT" o "SEQUENTIAL".
     * @param nAviones Número de aviones en la simulación.
     * @param nPistas Número de pistas del aeropuerto.
     * @param nPuertas Número de puertas de embarque.
     * @param nOperarios Número de operarios en la simulación.
     * @throws LogWriteException Si no se puede crear o escribir en el archivo de log.
     */
    public static void setup(String mode, int nAviones, int nPistas, int nPuertas, int nOperarios) throws LogWriteException {
        if (isInitialized) return;

        // Estructura de carpetas según la organización de logs
        String folderPath = "logs/" + (mode.equalsIgnoreCase("SEQUENTIAL") ? "secuencial/" : "concurrent/");
        
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs(); 
        }

        // Formato de nombre de archivo requerido para trazabilidad
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("aeron-%s-%dAV-%dPIS-%dPUE-%dOPE-%s",
                mode.toUpperCase(), nAviones, nPistas, nPuertas, nOperarios, timeStamp);

        File logFile = new File(folderPath + fileName + ".log");

        // Captura de excepción específica para errores de escritura
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
            isInitialized = true;
            System.out.println("LOG INICIADO EN: " + logFile.getAbsolutePath());
        } catch (IOException e) {
            throw new LogWriteException(fileName, e);
        }
    }

    /**
     * Cierra el flujo de escritura del sistema de logs.
     * Debe llamarse al finalizar la simulación para liberar recursos.
     */
    public static void close() {
        if (writer != null) {
            writer.close();
            isInitialized = false;
        }
    }

    // =========================================================================
    // MÉTODOS DE LOGGING (Sección de métodos sincronizados para escritura segura)
    // =========================================================================

    /**
     * Escribe una línea cruda en el archivo de log.
     * Método sincronizado para garantizar thread-safety.
     * 
     * @param message Mensaje a escribir en el log.
     */
    public static synchronized void log(String message) {
        if (!isInitialized || writer == null) return;
        writer.println(message);
        writer.flush();
    }

    /**
     * Escribe un encabezado simple en el archivo de log.
     * Formato limpio sin cajas ASCII, utilizado para seccionar el log.
     * 
     * @param header Texto del encabezado a escribir.
     */
    public static synchronized void logHeader(String header) {
        if (!isInitialized || writer == null) return;
        writer.println(header); // Solo el texto, más limpio según el PDF
        writer.flush();
    }

    /**
     * Registra un evento de un avión en el log.
     * Formato: Avión [ID - ESTADO] Mensaje
     * 
     * @param id ID único del avión (ej. "IBE-001").
     * @param state Estado actual del avión (ej. "IN_FLIGHT", "BOARDING").
     * @param message Descripción del evento que ocurre.
     */
    public static synchronized void logAirplane(String id, String state, String message) {
        // Ejemplo PDF: Avión [IBE-001 - IN_FLIGHT] Inicia ciclo
        String formatted = String.format("Avión [%s - %s] %s", id, state, message);
        log(formatted);
    }

    /**
     * Registra un evento de un operario en el log.
     * Formato: Operario [OP-ID] Mensaje
     * 
     * @param operarioId ID único del operario (ej. "OP-001").
     * @param message Descripción del evento que realiza el operario.
     */
    public static synchronized void logTower(String operarioId, String message) {
        // Ejemplo PDF: Operario [OP-001] esperando nueva petición...
        String formatted = String.format("Operario [%s] %s", operarioId, message);
        log(formatted);
    }
    
    /**
     * Sobrecarga para registrar mensajes generales de la Torre de Control.
     * Utiliza cuando el evento no está asociado a un operario específico.
     * 
     * @param message Mensaje de evento general de la torre.
     */
    public static synchronized void logTowerGen(String message) {
        log(message);
    }

    /**
     * Registra una actualización del Panel de Vuelos en el log.
     * Escribe el estado de un vuelo en formato CSV para trazabilidad.
     * 
     * @param idPlane ID único del avión a registrar.
     * @param state Estado actual del avión a registrar.
     */
    public static synchronized void logFlightPanel(String idPlane, String state) {
        // El PDF muestra que cada vez que se actualiza, se imprime esto:
        log("Panel de vuelos");
        log("\"Flight\", \"Status\""); // Cabecera CSV
        log(String.format("\"%s\", \"%s\"", idPlane, state)); // Datos CSV
        log(""); // Espacio extra para legibilidad
    }
}
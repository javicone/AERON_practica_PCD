package aeronpcd.concurrente.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static PrintWriter writer;
    private static boolean isInitialized = false;

    /**
     * Inicializa el sistema de logs (Cumple Práctica 1 y 6).
     */
    public static void setup(String mode, int nAviones, int nPistas, int nPuertas, int nOperarios) throws Exception {
        if (isInitialized) return;

        // Estructura de carpetas según Práctica 1 [cite: 210, 211]
        String folderPath = "logs/" + (mode.equalsIgnoreCase("SEQUENTIAL") ? "secuencial/" : "concurrent/");
        
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs(); 
        }

        // Formato de nombre de archivo obligatorio 
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("aeron-%s-%dAV-%dPIS-%dPUE-%dOPE-%s.log",
                mode.toUpperCase(), nAviones, nPistas, nPuertas, nOperarios, timeStamp);

        File logFile = new File(folderPath + fileName);

        // Captura de excepción específica según Práctica 6 
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
            isInitialized = true;
            System.out.println("LOG INICIADO EN: " + logFile.getAbsolutePath());
        } catch (IOException e) {
            throw new Exception("No se ha encontrado el archivo de log " + fileName);
        }
    }

    /**
     * Cierra el flujo de escritura.
     */
    public static void close() {
        if (writer != null) {
            writer.close();
            isInitialized = false;
        }
    }

    // =========================================================================
    // MÉTODOS DE FORMATO ESPECÍFICO (Adaptación al PDF formato-logs-aeron)
    // =========================================================================

    /**
     * Escribe una línea cruda en el log.
     */
    public static synchronized void log(String message) {
        if (!isInitialized || writer == null) return;
        writer.println(message);
        writer.flush();
    }

    /**
     * Escribe un encabezado simple (Ej: "Aviones", "Torre de control").
     * El PDF muestra encabezados limpios, sin cajas ASCII grandes.
     */
    public static synchronized void logHeader(String header) {
        if (!isInitialized || writer == null) return;
        writer.println(header); // Solo el texto, más limpio según el PDF
        writer.flush();
    }

    /**
     * Formatea eventos de AVIONES según el PDF[cite: 245].
     * Formato: Avión [ID - ESTADO] Mensaje
     */
    public static synchronized void logAirplane(String id, String state, String message) {
        // Ejemplo PDF: Avión [IBE-001 - IN_FLIGHT] Inicia ciclo
        String formatted = String.format("Avión [%s - %s] %s", id, state, message);
        log(formatted);
    }

    /**
     * Formatea eventos de la TORRE/OPERARIO según el PDF[cite: 248].
     * Formato: Operario [OP-ID] Mensaje
     */
    public static synchronized void logTower(String operarioId, String message) {
        // Ejemplo PDF: Operario [OP-001] esperando nueva petición...
        String formatted = String.format("Operario [%s] %s", operarioId, message);
        log(formatted);
    }
    
    /**
     * Sobrecarga para mensajes generales de la Torre sin operario específico.
     * Ejemplo PDF: "Recibida la solicitud..." [cite: 264]
     */
    public static synchronized void logTowerGen(String message) {
        log(message);
    }

    /**
     * Escribe una línea del Panel de Vuelos en formato CSV según el PDF[cite: 251].
     */
    public static synchronized void logFlightPanel(String idPlane, String state) {
        // El PDF muestra que cada vez que se actualiza, se imprime esto:
        log("Panel de vuelos");
        log("\"Flight\", \"Status\""); // Cabecera CSV
        log(String.format("\"%s\", \"%s\"", idPlane, state)); // Datos CSV
        log(""); // Espacio extra para legibilidad
    }
}
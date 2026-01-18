package aeronpcd.concurrente.util;

import aeronpcd.concurrente.exceptions.FlightPanelException;
import aeronpcd.concurrente.model.Airplane;
import aeronpcd.concurrente.model.AirplaneState;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Sistema de actualización en tiempo real del Panel de Vuelos mediante JSON.
 * 
 * OBJETIVO: Cuando la torre de control cambie el estado de un avión,
 * el panel de vuelos debe reflejarlo AL INSTANTE.
 * 
 * IMPLEMENTACIÓN: Archivo JSON que actúa como interfaz entre la torre
 * y el panel de vuelos externo.
 * 
 * Formato JSON:
 * {
 *   "IBE-001": "LANDING",
 *   "IBE-002": "BOARDING",
 *   ...
 * }
 */
public class FlightPanelJSON {

    /**
     * Ruta del archivo JSON del panel de vuelos (se configura dinámicamente).
     */
    private String jsonFilePath;
    
    /**
     * Mapa concurrente para almacenar estados de vuelos.
     * Garantiza thread-safety sin usar sincronización explícita.
     */
    private final ConcurrentHashMap<String, AirplaneState> flightStates;
    
    /**
     * Lock para escritura segura al archivo JSON.
     * Permite múltiples lectores simultáneos pero acceso exclusivo en escritura.
     */
    private final ReentrantReadWriteLock fileLock;
    
    /**
     * Instancia única del gestor del panel JSON (Singleton).
     */
    private static FlightPanelJSON instance;
    
    /**
     * Bandera que indica si el panel ha sido configurado correctamente.
     */
    private boolean isConfigured = false;
    
    /**
     * Constructor privado del Singleton.
     * Inicializa las estructuras de datos para almacenar y gestionar estados de vuelos.
     */
    private FlightPanelJSON() {
        this.flightStates = new ConcurrentHashMap<>();
        this.fileLock = new ReentrantReadWriteLock();
    }
    
    /**
     * Obtiene la instancia única del gestor del panel JSON.
     * Implementa el patrón Singleton con sincronización thread-safe.
     * 
     * @return Instancia única de FlightPanelJSON.
     */
    public static synchronized FlightPanelJSON getInstance() {
        if (instance == null) {
            instance = new FlightPanelJSON();
        }
        return instance;
    }
    
    /**
     * Configura el panel de vuelos con los parámetros de simulación.
     * Crea la ruta del archivo JSON y genera el nombre siguiendo el mismo patrón que Logger.
     * 
     * Debe llamarse antes de usar cualquier otro método de esta clase.
     * 
     * @param mode Modo de ejecución: "CONCURRENT" o "SEQUENTIAL" para determinar carpeta.
     * @param nAviones Número de aviones en la simulación.
     * @param nPistas Número de pistas del aeropuerto.
     * @param nPuertas Número de puertas de embarque.
     * @param nOperarios Número de operarios en la simulación.
     */
    public void configure(String mode, int nAviones, int nPistas, int nPuertas, int nOperarios) {
        // Misma estructura de carpetas que Logger
        String folderPath = "logs/" + (mode.equalsIgnoreCase("SEQUENTIAL") ? "secuencial/" : "concurrent/");
        
        // Mismo formato de nombre que Logger
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("aeron-%s-%dAV-%dPIS-%dPUE-%dOPE-%s.json",
                mode.toUpperCase(), nAviones, nPistas, nPuertas, nOperarios, timeStamp);
        
        this.jsonFilePath = folderPath + fileName;
        this.isConfigured = true;
        
        initializeFile();
    }
    
    /**
     * Inicializa el archivo JSON y crea los directorios necesarios.
     * Si la configuración no está completa, este método no hace nada.
     * Maneja excepciones internamente registrando en el Logger.
     */
    private void initializeFile() {
        if (!isConfigured) return;
        
        try {
            File folder = new File(jsonFilePath).getParentFile();
            if (!folder.exists()) {
                folder.mkdirs();
            }
            
            // Crear archivo vacío inicial
            writeToFile("{\n}");
            Logger.log("[PANEL JSON] Archivo inicializado: " + jsonFilePath);
            
        } catch (FlightPanelException e) {
            Logger.log("[PANEL JSON] " + e.getMessage());
        }
    }
    
    /**
     * Registra todos los aviones con su estado inicial en el panel de vuelos.
     * Debe llamarse exactamente una vez al inicio de la simulación para sincronizar
     * los aviones con el archivo JSON.
     * 
     * @param airplanes Lista de aviones de la simulación.
     * @throws FlightPanelException Si no se puede escribir en el archivo JSON.
     */
    public void registerAirplanes(List<Airplane> airplanes) throws FlightPanelException {
        for (Airplane plane : airplanes) {
            flightStates.put(plane.getAirplaneId(), plane.getAirplaneState());
        }
        writeJSON();
        Logger.log("[PANEL JSON] Registrados " + airplanes.size() + " aviones");
    }
    
    /**
     * Actualización instantánea del estado de un avión en el panel de vuelos.
     * Este método es llamado por la Torre de Control cuando procesa una petición
     * y necesita reflejar el cambio de estado en tiempo real.
     * 
     * La operación es thread-safe: actualiza el mapa en memoria de forma atómica
     * y luego escribe inmediatamente al archivo JSON.
     * 
     * @param airplaneId ID único del avión (ej. "IBE-001").
     * @param newState Nuevo estado del avión.
     * @throws FlightPanelException Si no se puede escribir en el archivo JSON.
     */
    public void updateFlightState(String airplaneId, AirplaneState newState) throws FlightPanelException {
        // Actualizar en memoria (operación atómica del ConcurrentHashMap)
        AirplaneState oldState = flightStates.put(airplaneId, newState);
        
        // Escribir al archivo JSON inmediatamente
        writeJSON();
        
        // Log de la actualización
        if (oldState != newState) {
            Logger.log("[PANEL JSON] " + airplaneId + ": " + oldState + " -> " + newState);
        }
    }
    
    /**
     * Escribe el estado actual de todos los vuelos al archivo JSON.
     * Usa un lock de escritura (ReentrantReadWriteLock) para garantizar consistencia
     * entre múltiples actualizaciones concurrentes.
     * 
     * El archivo JSON resultante contiene todas las entradas ordenadas alfabéticamente
     * por ID de avión.
     * 
     * @throws FlightPanelException Si no se puede escribir en el archivo JSON.
     */
    private void writeJSON() throws FlightPanelException {
        fileLock.writeLock().lock();
        try {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            
            // Construir JSON manualmente para control de comas
            var entries = flightStates.entrySet().stream()
                .sorted((a, b) -> a.getKey().compareTo(b.getKey()))
                .toArray();
            
            for (int i = 0; i < entries.length; i++) {
                @SuppressWarnings("unchecked")
                var entry = (java.util.Map.Entry<String, AirplaneState>) entries[i];
                json.append("  \"").append(entry.getKey()).append("\": \"")
                    .append(entry.getValue().toString()).append("\"");
                
                if (i < entries.length - 1) {
                    json.append(",");
                }
                json.append("\n");
            }
            
            json.append("}");
            
            writeToFile(json.toString());
            
        } finally {
            fileLock.writeLock().unlock();
        }
    }
    
    /**
     * Escribe contenido directamente al archivo JSON del panel de vuelos.
     * Método interno utilizado por writeJSON().
     * 
     * @param content Contenido JSON a escribir en el archivo.
     * @throws FlightPanelException Si el panel no está configurado o no se puede escribir en el archivo.
     */
    private void writeToFile(String content) throws FlightPanelException {
        if (!isConfigured || jsonFilePath == null) {
            throw new FlightPanelException("Panel de vuelos no configurado");
        }
        try (FileWriter writer = new FileWriter(jsonFilePath)) {
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            throw new FlightPanelException(jsonFilePath, e);
        }
    }
    
    /**
     * Obtiene el estado actual de un vuelo desde la memoria caché del panel.
     * 
     * @param airplaneId ID único del avión.
     * @return Estado actual del avión, o null si el avión no está registrado.
     */
    public AirplaneState getFlightState(String airplaneId) {
        return flightStates.get(airplaneId);
    }
    
    /**
     * Genera una representación en texto del estado actual del panel de vuelos.
     * Útil para debugging y visualización en consola.
     * 
     * @return Cadena formateada con el estado de todos los vuelos registrados.
     */
    public String getStatusSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PANEL DE VUELOS (JSON) ===\n");
        
        flightStates.entrySet().stream()
            .sorted((a, b) -> a.getKey().compareTo(b.getKey()))
            .forEach(entry -> {
                sb.append(String.format("%-10s : %s\n", entry.getKey(), entry.getValue()));
            });
        
        return sb.toString();
    }
}

package aeronpcd.concurrente.util;

import aeronpcd.concurrente.model.Airplane;
import aeronpcd.concurrente.model.AirplaneState;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    // Ruta del archivo JSON del panel de vuelos
    private static final String JSON_FILE_PATH = "logs/concurrent/flight_panel.json";
    
    // Mapa concurrente para almacenar estados (thread-safe)
    private final ConcurrentHashMap<String, AirplaneState> flightStates;
    
    // Lock para escritura segura al archivo
    private final ReentrantReadWriteLock fileLock;
    
    // Singleton para acceso global
    private static FlightPanelJSON instance;
    
    private FlightPanelJSON() {
        this.flightStates = new ConcurrentHashMap<>();
        this.fileLock = new ReentrantReadWriteLock();
        initializeFile();
    }
    
    /**
     * Obtiene la instancia única del gestor del panel JSON.
     */
    public static synchronized FlightPanelJSON getInstance() {
        if (instance == null) {
            instance = new FlightPanelJSON();
        }
        return instance;
    }
    
    /**
     * Inicializa el archivo JSON y crea los directorios necesarios.
     */
    private void initializeFile() {
        try {
            Path path = Paths.get(JSON_FILE_PATH);
            Files.createDirectories(path.getParent());
            
            // Crear archivo vacío inicial
            writeToFile("{\n}");
            Logger.log("[PANEL JSON] Archivo inicializado: " + JSON_FILE_PATH);
            
        } catch (IOException e) {
            Logger.log("[PANEL JSON] Error al inicializar: " + e.getMessage());
        }
    }
    
    /**
     * Registra todos los aviones con su estado inicial.
     * Debe llamarse al inicio de la simulación.
     */
    public void registerAirplanes(List<Airplane> airplanes) {
        for (Airplane plane : airplanes) {
            flightStates.put(plane.getAirplaneId(), plane.getAirplaneState());
        }
        writeJSON();
        Logger.log("[PANEL JSON] Registrados " + airplanes.size() + " aviones");
    }
    
    /**
     * ACTUALIZACIÓN INSTANTÁNEA del estado de un avión.
     * Este método es llamado por la Torre de Control cuando procesa una petición.
     * 
     * @param airplaneId ID del avión (ej. "IBE-001")
     * @param newState Nuevo estado del avión
     */
    public void updateFlightState(String airplaneId, AirplaneState newState) {
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
     * Usa un lock de escritura para garantizar consistencia.
     */
    private void writeJSON() {
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
     * Escribe contenido al archivo JSON.
     */
    private void writeToFile(String content) {
        try (FileWriter writer = new FileWriter(JSON_FILE_PATH)) {
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            Logger.log("[PANEL JSON] Error de escritura: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene el estado actual de un vuelo desde la memoria.
     */
    public AirplaneState getFlightState(String airplaneId) {
        return flightStates.get(airplaneId);
    }
    
    /**
     * Genera una representación en texto del estado actual (para debug).
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

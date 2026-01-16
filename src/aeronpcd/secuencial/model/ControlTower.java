 package aeronpcd.secuencial.model;

import aeronpcd.secuencial.*;
import aeronpcd.secuencial.util.Window;
import aeronpcd.secuencial.util.AirportState;
import aeronpcd.secuencial.util.Logger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ControlTower {

    private List<Runway> runways;
    private List<Gate> gates;
    private Queue<Request> requestQueue;
    
    // Referencia a la GUI para actualizaciones visuales
    private Window window;
    
    // Necesitamos conocer TODOS los aviones para pintar el "Panel de Vuelos" completo
    private List<Airplane> registeredAirplanes; 

    /**
     * Constructor.
     * @param window Referencia a la ventana principal (puede ser null para tests sin GUI)
     */
    public ControlTower(Window window) {
        this.window = window;
        this.runways = new ArrayList<>();
        this.gates = new ArrayList<>();
        this.requestQueue = new LinkedList<>();
        this.registeredAirplanes = new ArrayList<>();

        // Inicializar 3 Pistas [cite: 16]
        for (int i = 1; i <= 3; i++) {
            runways.add(new Runway("P" + i)); 
        }

        // Inicializar 5 Puertas [cite: 17]
        for (int i = 1; i <= 5; i++) {
            gates.add(new Gate("G" + i));
        }
    }

    /**
     * Registra la lista de aviones activa para poder mostrarlos en el Panel de Vuelos.
     */
    public void registerAirplanes(List<Airplane> airplanes) {
        this.registeredAirplanes = airplanes;
    }

    /**
     * Recibe una petición de un avión y la procesa inmediatamente (Secuencial).
     */
    public void addRequest(Request req) {
        this.requestQueue.add(req);
        
        // Notificamos visualmente que llegó una petición antes de procesarla
        printStatus("NUEVA PETICIÓN: " + req.getType() + " (" + req.getAirplane().getId() + ")");
        
        processNextRequest(); 
    }

    private void processNextRequest() {
        Request req = requestQueue.poll();
        if (req == null) return;

        Airplane airplane = req.getAirplane();
        AirplaneState type = req.getType();

        Logger.log("Procesando: " + type + " -> Avión " + airplane.getId());

        boolean success = false;

        switch (type) {
            // [cite: 341] LANDING: Asigna PISTA + PUERTA
            case LANDING_REQUESTED:
                Runway freeRunway = findFreeRunway();
                Gate freeGate = findFreeGate();
                
                if (freeRunway != null && freeGate != null) {
                    freeRunway.occupy(airplane);
                    freeGate.occupy(airplane);
                    
                    airplane.setAssignedRunway(freeRunway);
                    airplane.setAssignedGate(freeGate);
                    airplane.setState(AirplaneState.LANDING_ASSIGNED); 
                    
                    Logger.log("  -> OK: Asignada " + freeRunway.getId() + " y " + freeGate.getId());
                    success = true;
                } else {
                    Logger.log("  -> ESPERA: Sin recursos disponibles.");
                }
                break;

            // [cite: 335] LANDED: Libera PISTA
            case LANDED:
                if (airplane.getAssignedRunway() != null) {
                    Logger.log("  -> OK: Liberando " + airplane.getAssignedRunway().getId());
                    airplane.getAssignedRunway().release();
                    airplane.setAssignedRunway(null);
                    success = true;
                }
                break;

            // [cite: 336] BOARDED: Libera PUERTA
            case BOARDED:
                if (airplane.getAssignedGate() != null) {
                    Logger.log("  -> OK: Liberando " + airplane.getAssignedGate().getId());
                    airplane.getAssignedGate().release();
                    airplane.setAssignedGate(null);
                    success = true;
                }
                break;

            // [cite: 337] TAKEOFF: Asigna NUEVA PISTA
            case TAKEOFF_REQUESTED:
                Runway takeoffRunway = findFreeRunway();
                if (takeoffRunway != null) {
                    takeoffRunway.occupy(airplane);
                    airplane.setAssignedRunway(takeoffRunway);
                    airplane.setState(AirplaneState.TAKEOFF_ASSIGNED);
                    
                    Logger.log("  -> OK: Asignada despegue " + takeoffRunway.getId());
                    success = true;
                }
                break;

            // [cite: 338] DEPARTED: Libera PISTA
            case DEPARTED:
                if (airplane.getAssignedRunway() != null) {
                    Logger.log("  -> FIN CICLO: Liberando " + airplane.getAssignedRunway().getId());
                    airplane.getAssignedRunway().release();
                    airplane.setAssignedRunway(null);
                    success = true;
                }
                break;
                
            default:
                break;
        }

        // Si hubo cambios, actualizamos toda la interfaz
        if (success) {
            printStatus("ESTADO ACTUALIZADO TRAS " + type);
        }
    }

    /**
     * Centraliza la actualización de Logs y Ventana.
     */
    private void printStatus(String headerMsg) {
        // 1. Log en archivo
        Logger.logHeader(headerMsg);
        String resourceMap = AirportState.showResourcesStatus(runways, gates);
        String queueMap = AirportState.showRequestQueue(new ArrayList<>(requestQueue));
        Logger.log(resourceMap);
        Logger.log(queueMap);

        // 2. Actualización de Ventana (GUI) [cite: 50-53]
        if (window != null) {
            // Columna 1: Eventos
            window.addAirplaneEvent(headerMsg);
            
            // Columna 2: Torre (Recursos y Cola)
            // Combinamos los gráficos ASCII para mostrarlos juntos
            String towerText = headerMsg + "\n\n" + resourceMap + "\n" + queueMap;
            window.updateTowerArea(towerText);
            
            // Columna 3: Panel de Vuelos (Flight Board) [cite: 370]
            window.updateFlightPanel(generateFlightPanelText());
        }
    }

    /**
     * Genera la tabla de estados para el Panel de Vuelos (Columna Derecha).
     */
    private String generateFlightPanelText() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s %-25s\n", "VUELO", "ESTADO"));
        sb.append("════════════════════════════════════\n");
        
        // Iteramos sobre TODOS los aviones registrados, no solo el actual
        if (registeredAirplanes != null) {
            for (Airplane plane : registeredAirplanes) {
                sb.append(String.format("%-10s %-25s\n", 
                    plane.getId(), 
                    plane.getState())); // toString del enum
            }
        }
        return sb.toString();
    }

    /**
     * Busca la primera pista que no tenga un avión asignado.
     * @return La pista libre o null si todas están ocupadas.
     */
    private Runway findFreeRunway() {
        for (Runway r : runways) {
            if (r.isAvailable()) { // Asume que Runway tiene este método
                return r;
            }
        }
        return null;
    }

    /**
     * Busca la primera puerta que no tenga un avión asignado.
     * @return La puerta libre o null si todas están ocupadas.
     */
    private Gate findFreeGate() {
        for (Gate g : gates) {
            if (g.isFree()) { // Asume que Gate tiene este método
                return g;
            }
        }
        return null;
    }
    
    // Getters para recursos
    public List<Runway> getRunways() { return runways; }
    public List<Gate> getGates() { return gates; }
} 
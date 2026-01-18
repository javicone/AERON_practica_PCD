 package aeronpcd.secuencial.model;

import aeronpcd.secuencial.util.AirportState;
import aeronpcd.secuencial.util.Logger;
import aeronpcd.secuencial.util.Window;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Torre de Control del Aeropuerto AERON en modo secuencial.
 * 
 * Gestiona los recursos del aeropuerto (pistas y puertas) y procesa
 * las peticiones de los aviones de forma secuencial. Coordina la asignación
 * de recursos y actualiza la interfaz gráfica.
 */
public class ControlTower {

    /**
     * Lista de pistas disponibles en el aeropuerto.
     */
    private List<Runway> runways;
    
    /**
     * Lista de puertas de embarque disponibles.
     */
    private List<Gate> gates;
    
    /**
     * Cola de peticiones de aviones esperando ser procesadas.
     */
    private Queue<Request> requestQueue;
    
    /**
     * Referencia a la ventana gráfica para actualizar la interfaz visual.
     */
    private Window window;
    
    /**
     * Lista de todos los aviones registrados para mostrar en el panel de vuelos.
     */
    private List<Airplane> registeredAirplanes; 

    /**
     * Constructor de la Torre de Control.
     * Inicializa 3 pistas y 5 puertas de embarque según especificaciones del aeropuerto.
     * 
     * @param window Referencia a la ventana principal (puede ser null para tests sin GUI).
     */
    public ControlTower(Window window) {
        this.window = window;
        this.runways = new ArrayList<>();
        this.gates = new ArrayList<>();
        this.requestQueue = new LinkedList<>();
        this.registeredAirplanes = new ArrayList<>();

        // Inicializar 3 Pistas
        for (int i = 1; i <= 3; i++) {
            runways.add(new Runway("P" + i)); 
        }

        // Inicializar 5 Puertas
        for (int i = 1; i <= 5; i++) {
            gates.add(new Gate("G" + i));
        }
    }

    /**
     * Registra la lista de aviones activos para mostrarlos en el Panel de Vuelos.
     * 
     * @param airplanes Lista de aviones de la simulación.
     */
    public void registerAirplanes(List<Airplane> airplanes) {
        this.registeredAirplanes = airplanes;
    }

    /**
     * Recibe una petición de un avión y la procesa inmediatamente en modo secuencial.
     * 
     * @param req Petición a procesar.
     */
    public void addRequest(Request req) {
        this.requestQueue.add(req);
        Logger.log("Avión " + req.getAirplane().getId() + " encolado: " + req.getType());
        
        // Notificamos visualmente que llegó una petición antes de procesarla
        printStatus("Nueva petición recibida");
        
        processNextRequest(); 
    }

    private void processNextRequest() {
        Request req = requestQueue.poll();
        if (req == null) return;

        Airplane airplane = req.getAirplane();
        AirplaneState type = req.getType();
        
        // Capturamos el estado ACTUAL antes de procesar nada
        AirplaneState oldState = airplane.getState();
        int operarioId = 1; // En secuencial solo hay un operario

        boolean success = false;

        switch (type) {
            // LANDING: Asigna PISTA + PUERTA
            case LANDING_REQUESTED:
                Runway freeRunway = findFreeRunway();
                Gate freeGate = findFreeGate();
                
                if (freeRunway != null && freeGate != null) {
                    freeRunway.occupy(airplane);
                    freeGate.occupy(airplane);
                    
                    airplane.setAssignedRunway(freeRunway);
                    airplane.setAssignedGate(freeGate);
                    airplane.setState(AirplaneState.LANDING_ASSIGNED); 
                    
                    success = true;
                }
                break;

            // LANDED: Libera PISTA
            case LANDED:
                if (airplane.getAssignedRunway() != null) {
                    airplane.getAssignedRunway().release();
                    airplane.setAssignedRunway(null);
                    success = true;
                }
                break;

            // BOARDED: Libera PUERTA
            case BOARDED:
                if (airplane.getAssignedGate() != null) {
                    airplane.getAssignedGate().release();
                    airplane.setAssignedGate(null);
                    success = true;
                }
                break;

            // TAKEOFF: Asigna NUEVA PISTA
            case TAKEOFF_REQUESTED:
                Runway takeoffRunway = findFreeRunway();
                if (takeoffRunway != null) {
                    takeoffRunway.occupy(airplane);
                    airplane.setAssignedRunway(takeoffRunway);
                    airplane.setState(AirplaneState.TAKEOFF_ASSIGNED);
                    
                    success = true;
                }
                break;

            // DEPARTED: Libera PISTA
            case DEPARTED:
                if (airplane.getAssignedRunway() != null) {
                    airplane.getAssignedRunway().release();
                    airplane.setAssignedRunway(null);
                    success = true;
                }
                break;
                
            default:
                break;
        }

        // Si hubo cambios, actualizamos toda la interfaz con el formato del concurrente
        if (success) {
            String actionName = getFriendlyActionName(type);
            String logMessage = String.format("[OP-%d] %-18s | %s (%s -> %s)", 
                operarioId, 
                actionName, 
                airplane.getId(), 
                oldState,
                type
            );
            printStatus(logMessage);
        }
    }

    /**
     * Método auxiliar para traducir enumeraciones de estado a acciones legibles en español.
     * 
     * @param state Estado del avión.
     * @return Descripción en español de la acción correspondiente.
     */
    private String getFriendlyActionName(AirplaneState state) {
        switch (state) {
            case LANDING_REQUESTED: return "AUTORIZA ATERRIZAJE";
            case LANDED:            return "CONFIRMA ATERRIZAJE";
            case BOARDING:          return "INICIA EMBARQUE";
            case BOARDED:           return "FIN EMBARQUE";
            case TAKEOFF_REQUESTED: return "AUTORIZA DESPEGUE";
            case DEPARTING:         return "CONFIRMA DESPEGUE";
            case DEPARTED:          return "VUELO COMPLETADO";
            default:                return state.toString();
        }
    }

    /**
     * Centraliza la actualización de logs y ventana gráfica.
     * Muestra el estado de recursos, la cola de peticiones y el panel de vuelos.
     * 
     * @param headerMsg Mensaje de encabezado a mostrar.
     */
    private void printStatus(String headerMsg) {
        // 1. Log en archivo
        String resourceMap = AirportState.showResourcesStatus(runways, gates);
        String queueMap = AirportState.showRequestQueue(new ArrayList<>(requestQueue));

        // 2. Actualización de Ventana (GUI)
        if (window != null) {
            // Columna 1: Eventos
            window.addAirplaneEvent(headerMsg);
            
            // Columna 2: Torre (Recursos y Cola)
            // Formato igual al concurrente
            String towerText = "ESTADO TORRE (SECUENCIAL)\n" + resourceMap + "\n" + queueMap;
            window.updateTowerArea(towerText);
            
            // Columna 3: Panel de Vuelos (Flight Board)
            window.updateFlightPanel(generateFlightPanelText());
        }
    }

    /**
     * Genera la tabla de estados para el Panel de Vuelos.
     * Muestra el ID del vuelo, estado actual, pista asignada y puerta asignada.
     * 
     * @return String con el panel de vuelos formateado.
     */
    private String generateFlightPanelText() {
        StringBuilder sb = new StringBuilder();
        // Cabecera ajustada al estilo del concurrente
        sb.append(String.format("%-10s %-20s %-8s %-8s\n", "VUELO", "ESTADO", "PISTA", "PUERTA"));
        sb.append("══════════════════════════════════════════════════\n");
        
        // Iteramos sobre TODOS los aviones registrados, no solo el actual
        if (registeredAirplanes != null) {
            for (Airplane plane : registeredAirplanes) {
                Runway r = plane.getAssignedRunway();
                Gate g = plane.getAssignedGate();
                
                sb.append(String.format("%-10s %-20s %-8s %-8s\n", 
                    plane.getId(), 
                    plane.getState(),
                    (r != null ? r.getId() : "-"),
                    (g != null ? g.getId() : "-")));
            }
        }
        return sb.toString();
    }

    
    /**
     * Busca la primera pista disponible sin avión asignado.
     * 
     * @return Pista libre, o null si todas están ocupadas.
     */
    private Runway findFreeRunway() {
        for (Runway r : runways) {
            if (r.isAvailable()) {
                return r;
            }
        }
        return null;
    }

    /**
     * Busca la primera puerta de embarque disponible sin avión asignado.
     * 
     * @return Puerta libre, o null si todas están ocupadas.
     */
    private Gate findFreeGate() {
        for (Gate g : gates) {
            if (g.isFree()) {
                return g;
            }
        }
        return null;
    }
    
    /**
     * Obtiene la lista de pistas del aeropuerto.
     * 
     * @return Lista de pistas.
     */
    public List<Runway> getRunways() {
        return runways;
    }
    
    /**
     * Obtiene la lista de puertas de embarque del aeropuerto.
     * 
     * @return Lista de puertas.
     */
    public List<Gate> getGates() {
        return gates;
    }
} 
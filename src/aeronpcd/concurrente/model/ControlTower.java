package aeronpcd.concurrente.model;

import aeronpcd.concurrente.util.AirportState;
import aeronpcd.concurrente.util.FlightPanelJSON;
import aeronpcd.concurrente.util.Logger;
import aeronpcd.concurrente.util.Window;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;

/**
 * La Torre de Control gestiona la sincronización del aeropuerto.
 * DEMOSTRACIÓN DE MECANISMOS HÍBRIDOS:
 * 1. SEMÁFOROS: Para el problema Productor-Consumidor (Cola de peticiones).
 * 2. MONITORES: Para la Gestión de Recursos Compartidos (Pistas y Puertas).
 */
public class ControlTower {

    // --- RECURSOS DEL AEROPUERTO ---
    private final List<Runway> runways;
    private final List<Gate> gates;
    private final Window window;
    private List<Airplane> registeredAirplanes;
    
    // Panel de vuelos JSON para actualización instantánea
    private final FlightPanelJSON flightPanel;

    // --- ESTRUCTURAS DE SINCRONIZACIÓN ---
    
    // 1. LA COLA COMPARTIDA (Recurso crítico para Productor-Consumidor)
    private final Queue<Request> requestQueue;

    // 2. SEMÁFOROS (Para proteger la cola)
    // Mutex: Garantiza acceso exclusivo a la lista (como un cerrojo)
    private final Semaphore queueMutex; 
    // Items: Cuenta elementos disponibles y bloquea consumidores si está vacía
    private final Semaphore requestsAvailable;

    public ControlTower(Window window, int NumRunways, int numGates) {
        this.window = window;
        
        // Inicialización de recursos
        this.runways = new ArrayList<>();
        this.gates = new ArrayList<>();
        for (int i = 1; i <= NumRunways; i++) runways.add(new Runway("P" + i));
        for (int i = 1; i <= numGates; i++) gates.add(new Gate("G" + i));

        // Inicialización de la lógica de Productor-Consumidor
        this.requestQueue = new LinkedList<>();
        this.queueMutex = new Semaphore(1); // 1 permiso = Abierto (mutex)
        this.requestsAvailable = new Semaphore(0); // 0 permisos = Cola vacía inicialmente
        
        // Inicializar el panel de vuelos JSON
        this.flightPanel = FlightPanelJSON.getInstance();
    }

    public void registerAirplanes(List<Airplane> airplanes) {
        this.registeredAirplanes = airplanes;
        // Registrar aviones en el panel JSON para actualización instantánea
        flightPanel.registerAirplanes(airplanes);
    }

    // =========================================================================
    // PARTE 1: IMPLEMENTACIÓN CON SEMÁFOROS (PRODUCTOR-CONSUMIDOR)
    // =========================================================================

    /**
     * PRODUCTOR (Avión): Añade una petición a la cola de forma segura con Semáforos.
     */
    public void addRequest(Request req) {
        try {
            // 1. Exclusión Mutua: Adquirir permiso para tocar la lista
            queueMutex.acquire();
            try {
                requestQueue.add(req);
                Logger.log("Avión " + req.getAirplane().getAirplaneId() + " encolado: " + req.getType());
            } finally {
                // 2. Liberar el cerrojo pase lo que pase
                queueMutex.release();
            }
            // 3. Coordinación: Avisar de que hay un nuevo item disponible
            requestsAvailable.release();
            
            // Actualizamos GUI (fuera de la sección crítica para no bloquear)
            printStatus("Nueva petición recibida");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * CONSUMIDOR (Operario): Extrae una petición.
     * Si la cola está vacía, se BLOQUEA en el semáforo 'requestsAvailable'.
     */
    public Request getNextRequest() throws InterruptedException {
        // 1. Espera pasiva: Si contador es 0, el hilo se duerme aquí.
        requestsAvailable.acquire();

        Request req = null;
        
        // 2. Exclusión Mutua: Entrar a la zona crítica para sacar el elemento
        queueMutex.acquire();
        try {
            req = requestQueue.poll();
        } finally {
            queueMutex.release();
        }
        
        return req;
    }
 // =========================================================================
    // ZONA 2: MONITORES (Gestión de Recursos Pistas/Puertas)
    // =========================================================================

    public synchronized boolean processRequest(Request req, int operarioId) {
        Airplane airplane = req.getAirplane();
        AirplaneState requestType = req.getType();
        
        // Capturamos el estado ACTUAL antes de procesar nada
        AirplaneState oldState = airplane.getAirplaneState(); 
        
        boolean success = false;

        switch (requestType) {
            case LANDING_REQUESTED:
                // CRÍTICO: Asignación ATÓMICA de Pista + Puerta
                Runway freeRunway = findFreeRunway();
                Gate freeGate = findFreeGate();
                
                if (freeRunway != null && freeGate != null) {
                    freeRunway.occupy(airplane);
                    freeGate.occupy(airplane);
                    airplane.setAssignedRunway(freeRunway);
                    airplane.setAssignedGate(freeGate);
                    success = true;
                }
                break;

            case LANDED:
                // Libera solo PISTA (operación idempotente: siempre éxito)
                if (airplane.getAssignedRunway() != null) {
                    airplane.getAssignedRunway().release();
                    airplane.setAssignedRunway(null);
                }
                success = true; // Liberar siempre tiene éxito
                break;

            case BOARDED:
                // Libera solo PUERTA (operación idempotente: siempre éxito)
                if (airplane.getAssignedGate() != null) {
                    airplane.getAssignedGate().release();
                    airplane.setAssignedGate(null);
                }
                success = true; // Liberar siempre tiene éxito
                break;

            case TAKEOFF_REQUESTED:
                // Pide nueva PISTA
                Runway takeoffRunway = findFreeRunway();
                if (takeoffRunway != null) {
                    takeoffRunway.occupy(airplane);
                    airplane.setAssignedRunway(takeoffRunway);
                    success = true;
                }
                break;

            case DEPARTED:
                // Libera PISTA final (operación idempotente: siempre éxito)
                if (airplane.getAssignedRunway() != null) {
                    airplane.getAssignedRunway().release();
                    airplane.setAssignedRunway(null);
                }
                success = true; // Liberar siempre tiene éxito
                break;
                
            default:
                break;
        }

        if (success) {
            // ═══════════════════════════════════════════════════════════════
            // ACTUALIZACIÓN INSTANTÁNEA DEL PANEL DE VUELOS (JSON)
            // Requisito: El panel debe reflejar el estado AL INSTANTE
            // ═══════════════════════════════════════════════════════════════
            flightPanel.updateFlightState(airplane.getAirplaneId(), airplane.getAirplaneState());
            
            // --- LOG DETALLADO ---
            // Formato: [OP-X] ACCION | AVION (ESTADO_ANT -> ESTADO_NUEVO)
            String actionName = getFriendlyActionName(requestType);
            
            // Nota: El "estado nuevo" es el que el avión solicitó (requestType)
            String logMessage = String.format("[OP-%d] %-18s | %s (%s -> %s)", 
                operarioId, 
                actionName, 
                airplane.getAirplaneId(), 
                oldState,    // Estado anterior (ej. IN_FLIGHT)
                requestType  // Transición/Acción (ej. LANDING_REQUESTED)
            );
            
            printStatus(logMessage);
        }
        
        return success;
    }

    /**
     * Método auxiliar para traducir los ENUMs a acciones legibles en español.
     * Esto hace que el log parezca mucho más profesional.
     */
    private String getFriendlyActionName(AirplaneState state) {
        switch (state) {
            case LANDING_REQUESTED: return "AUTORIZA ATERRIZAJE";
            case LANDED:            return "CONFIRMA ATERRIZAJE"; // Libera pista
            case BOARDING:          return "INICIA EMBARQUE";
            case BOARDED:           return "FIN EMBARQUE";        // Libera puerta
            case TAKEOFF_REQUESTED: return "AUTORIZA DESPEGUE";
            case DEPARTING:         return "CONFIRMA DESPEGUE";
            case DEPARTED:          return "VUELO COMPLETADO";
            default:                return state.toString();
        }
    }
    // Métodos auxiliares privados (solo accesibles desde dentro del Monitor)
    private Runway findFreeRunway() {
        for (Runway r : runways) if (r.isAvailable()) return r;
        return null;
    }

    private Gate findFreeGate() {
        for (Gate g : gates) if (g.isFree()) return g;
        return null;
    }

    // =========================================================================
    // PARTE 3: INTERFAZ GRÁFICA (LECTURA SEGURA)
    // =========================================================================

    private void printStatus(String headerMsg) {
        // Obtenemos una copia segura de la cola usando el semáforo Mutex
        // para evitar que la GUI lea mientras un hilo escribe.
        List<Request> queueSnapshot = new ArrayList<>();
        try {
            if (queueMutex.tryAcquire()) { // Intentamos cogerlo sin bloquearnos
                try {
                    queueSnapshot.addAll(requestQueue);
                } finally {
                    queueMutex.release();
                }
            } else {
                // Si está muy ocupado, pintamos lo que había (o lista vacía) para no congelar la GUI
            }
        } catch (Exception e) { }

        String resourceMap = AirportState.showResourcesStatus(runways, gates);
        String queueMap = AirportState.showRequestQueue(queueSnapshot);
        
        if (window != null) {
            window.addAirplaneEvent(headerMsg);
            String towerText = "ESTADO TORRE (SEMÁFOROS + MONITORES)\n" + resourceMap + "\n" + queueMap;
            window.updateTowerArea(towerText);
            window.updateFlightPanel(generateFlightPanelText());
        }
    }

    private String generateFlightPanelText() {
        StringBuilder sb = new StringBuilder();
        // Cabecera ajustada al estilo realista
        sb.append(String.format("%-10s %-20s %-8s %-8s\n", "VUELO", "ESTADO", "PISTA", "PUERTA"));
        sb.append("══════════════════════════════════════════════════\n");
        
        if (registeredAirplanes != null) {
            // Sincronizamos sobre la lista si fuera dinámica (aquí es fija tras inicio)
            for (Airplane plane : registeredAirplanes) {
                Runway r = plane.getAssignedRunway();
                Gate g = plane.getAssignedGate();
                
                sb.append(String.format("%-10s %-20s %-8s %-8s\n", 
                    plane.getAirplaneId(), 
                    plane.getAirplaneState(),
                    (r != null ? r.getId() : "-"),
                    (g != null ? g.getId() : "-"))); 
            }
        }
        return sb.toString();
    }
    

}
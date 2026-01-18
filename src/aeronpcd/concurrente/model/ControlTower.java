package aeronpcd.concurrente.model;

import aeronpcd.concurrente.exceptions.FlightPanelException;
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

    /**
     * Constructor de la Torre de Control.
     * Inicializa los recursos (pistas y puertas) y las estructuras de sincronización
     * (semáforos para la cola de peticiones).
     * @param window Referencia a la ventana GUI para actualizar visualización.
     * @param NumRunways Número de pistas del aeropuerto.
     * @param numGates Número de puertas de embarque del aeropuerto.
     */
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

    /**
     * Registra los aviones en la torre de control y el panel de vuelos.
     * @throws FlightPanelException si no se puede actualizar el panel de vuelos JSON
     */
    public void registerAirplanes(List<Airplane> airplanes) throws FlightPanelException {
        this.registeredAirplanes = airplanes;
        // Registrar aviones en el panel JSON para actualización instantánea
        flightPanel.registerAirplanes(airplanes);
    }

    // =========================================================================
    // PARTE 1: IMPLEMENTACIÓN CON SEMÁFOROS (PRODUCTOR-CONSUMIDOR)
    // =========================================================================

    /**
     * Añade una petición a la cola de forma segura usando semáforos (Productor).
     * Llamado por los aviones cuando desean realizar una acción (aterrizaje, despegue, etc).
     * La petición se encola y se registra en el log.
     * @param req La petición del avión a procesar.
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
     * Extrae la siguiente petición de la cola (Consumidor).
     * Llamado por los operarios de la torre. Si la cola está vacía, se bloquea
     * en el semáforo 'requestsAvailable' hasta que haya peticiones disponibles.
     * @return La siguiente petición en la cola.
     * @throws InterruptedException Si el hilo se interrumpe mientras espera.
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
    // PARTE 2: MONITORES (Gestión de Recursos Pistas/Puertas)
    // =========================================================================

    /**
     * Procesa una petición de avión de forma segura usando monitor (synchronized).
     * Asigna o libera recursos (pistas y puertas) según el tipo de petición.
     * Las asignaciones son atómicas: requieren pista Y puerta simultáneamente.
     * @param req La petición a procesar.
     * @param operarioId Identificador del operario que procesa la petición.
     * @return true si la petición se procesó exitosamente, false si no había recursos disponibles.
     */
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
            try {
                flightPanel.updateFlightState(airplane.getAirplaneId(), airplane.getAirplaneState());
            } catch (FlightPanelException e) {
                Logger.log("[ERROR] " + e.getMessage());
            }
            
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
     * Traduce un estado de avión a una acción legible en español.
     * Utilizado para generar mensajes de log más profesionales y claros.
     * @param state El estado del avión.
     * @return Una descripción de la acción en español.
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
    /**
     * Busca una pista libre disponible.
     * @return Una pista disponible, o null si todas están ocupadas.
     */
    private Runway findFreeRunway() {
        for (Runway r : runways) if (r.isAvailable()) return r;
        return null;
    }

    /**
     * Busca una puerta libre disponible.
     * @return Una puerta disponible, o null si todas están ocupadas.
     */
    private Gate findFreeGate() {
        for (Gate g : gates) if (g.isFree()) return g;
        return null;
    }

    // =========================================================================
    // PARTE 3: INTERFAZ GRÁFICA (LECTURA SEGURA)
    // =========================================================================

    /**
     * Actualiza la interfaz gráfica con el estado actual de la torre.
     * Obtiene una copia segura de la cola de peticiones y actualiza los 3 paneles
     * (eventos, estado de torre y panel de vuelos).
     * @param headerMsg Mensaje de encabezado a mostrar en el log de eventos.
     */
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

    /**
     * Genera el texto del panel de vuelos con la información de todos los aviones registrados.
     * Incluye el estado actual, pista y puerta asignada de cada avión.
     * @return Texto formateado del panel de vuelos.
     */
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
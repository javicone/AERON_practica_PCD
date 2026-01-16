package aeronpcd.concurrente.model;

import aeronpcd.concurrente.util.Window;
import aeronpcd.concurrente.util.AirportState;
import aeronpcd.concurrente.util.Logger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * La Torre de Control actúa como un Monitor para gestionar la sincronización
 * entre Aviones (Productores) y Operarios (Consumidores).
 */
public class ControlTower {

    private final List<Runway> runways;
    private final List<Gate> gates;
    private final Queue<Request> requestQueue;
    private final Window window;
    private List<Airplane> registeredAirplanes; 

    public ControlTower(Window window) {
        this.window = window;
        this.runways = new ArrayList<>();
        this.gates = new ArrayList<>();
        this.requestQueue = new LinkedList<>();

        // Inicializar recursos según enunciado [cite: 16, 17]
        for (int i = 1; i <= 3; i++) runways.add(new Runway("P" + i)); 
        for (int i = 1; i <= 5; i++) gates.add(new Gate("G" + i));
    }

    public void registerAirplanes(List<Airplane> airplanes) {
        this.registeredAirplanes = airplanes;
    }

    /**
     * Los AVIONES llaman a este método para depositar una petición.
     * Patrón Productor-Consumidor: produce una entrada en la cola[cite: 46].
     */
    public synchronized void addRequest(Request req) {
        requestQueue.add(req);
        Logger.log("Avión " + req.getAirplane().getId() + " envió petición: " + req.getType());
        
        // Notifica a los operarios que hay una nueva petición disponible 
        notifyAll(); 
    }

    /**
     * Los OPERARIOS llaman a este método para obtener trabajo.
     * Si la cola está vacía, el operario se bloquea[cite: 113].
     */
    public synchronized Request getNextRequest() throws InterruptedException {
        while (requestQueue.isEmpty()) {
            wait(); // Espera pasiva hasta que addRequest haga notifyAll
        }
        return requestQueue.poll();
    }

    /**
     * Intenta procesar una petición asignando o liberando recursos.
     * Este método gestiona la exclusión mutua de pistas y puertas[cite: 41, 42].
     * * @return true si la petición se procesó con éxito, 
     * false si no había recursos disponibles (para peticiones de asignación).
     */
    public synchronized boolean processRequest(Request req, int operarioId) {
        Airplane airplane = req.getAirplane();
        AirplaneState type = req.getType();
        boolean success = false;

        switch (type) {
            case LANDING_REQUESTED:
                // Requiere PISTA Y PUERTA simultáneamente 
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
                // Libera la PISTA [cite: 127, 133]
                if (airplane.getAssignedRunway() != null) {
                    airplane.getAssignedRunway().release();
                    airplane.setAssignedRunway(null);
                    success = true;
                }
                break;

            case BOARDED:
                // Libera la PUERTA [cite: 128, 134]
                if (airplane.getAssignedGate() != null) {
                    airplane.getAssignedGate().release();
                    airplane.setAssignedGate(null);
                    success = true;
                }
                break;

            case TAKEOFF_REQUESTED:
                // Requiere una NUEVA PISTA para despegar [cite: 129, 135]
                Runway takeoffRunway = findFreeRunway();
                if (takeoffRunway != null) {
                    takeoffRunway.occupy(airplane);
                    airplane.setAssignedRunway(takeoffRunway);
                    success = true;
                }
                break;

            case DEPARTED:
                // Libera la PISTA final [cite: 130, 135]
                if (airplane.getAssignedRunway() != null) {
                    airplane.getAssignedRunway().release();
                    airplane.setAssignedRunway(null);
                    success = true;
                }
                break;
                
            default:
                break;
        }

        if (success) {
            // Si se liberaron o asignaron recursos, notificamos a otros hilos
            notifyAll(); 
            printStatus("Operario " + operarioId + " procesó: " + type + " (" + airplane.getId() + ")");
        }
        
        return success;
    }

    private synchronized Runway findFreeRunway() {
        for (Runway r : runways) {
            if (r.isAvailable()) return r;
        }
        return null;
    }

    private synchronized Gate findFreeGate() {
        for (Gate g : gates) {
            if (g.isFree()) return g;
        }
        return null;
    }

    /**
     * Actualiza la interfaz gráfica y los logs de forma sincronizada[cite: 50, 52].
     */
    private void printStatus(String headerMsg) {
        String resourceMap = AirportState.showResourcesStatus(runways, gates);
        String queueMap = AirportState.showRequestQueue(new ArrayList<>(requestQueue));
        
        if (window != null) {
            window.addAirplaneEvent(headerMsg);
            String towerText = "ESTADO TORRE\n" + resourceMap + "\n" + queueMap;
            window.updateTowerArea(towerText);
            window.updateFlightPanel(generateFlightPanelText());
        }
    }

    private String generateFlightPanelText() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s %-25s\n", "VUELO", "ESTADO"));
        sb.append("════════════════════════════════════\n");
        if (registeredAirplanes != null) {
            for (Airplane plane : registeredAirplanes) {
                sb.append(String.format("%-10s %-25s\n", plane.getId(), plane.getState()));
            }
        }
        return sb.toString();
    }
}
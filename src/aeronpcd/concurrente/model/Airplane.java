package aeronpcd.concurrente.model;

import aeronpcd.secuencial.util.Logger;
import aeronpcd.concurrente.model.Request;
import aeronpcd.concurrente.model.AirplaneState;

/**
 * Representa un Avión como un hilo independiente[cite: 30].
 * Ejecuta su ciclo de vida interactuando con la Torre de Control.
 */
public class Airplane extends Thread { // Cambio a Thread para concurrencia [cite: 39]

    private String id;
    private AirplaneState state;
    private ControlTower tower;

    // Recursos asignados por la torre
    private Runway assignedRunway;
    private Gate assignedGate;

    // Métricas de tiempo 
    private long startTime;
    private long endTime;
    private long duracionEnMs;
    
    // Objeto de bloqueo para esperar a la torre
    private final Object confirmationLock = new Object();
    private boolean processedByOperator = false;

    public Airplane(String id, ControlTower tower) {
        this.id = id;
        this.tower = tower;
        this.state = AirplaneState.IN_FLIGHT; // Estado inicial 
    }
    @Override
    public void run() {
        // Guardamos el momento exacto en que solicita pista para aterrizar 
        this.startTime = System.currentTimeMillis();
    try {
        // --- FASE 1: ATERRIZAJE ---
        // 1. Solicitar Pista + Puerta [cite: 68, 125, 133]
        requestAndWait(AirplaneState.LANDING_REQUESTED);
        Thread.sleep(500); // Pausa visual: Preparando maniobra
        
        // 2. Aterrizar (100 ms obligatorios) 
        setState(AirplaneState.LANDING);
        Thread.sleep(100); 
        Thread.sleep(400); // Pausa visual: Terminando de frenar

        // 3. Notificar fin de aterrizaje para liberar PISTA [cite: 68, 127, 133]
        requestAndWait(AirplaneState.LANDED);
        Thread.sleep(500); // Pausa visual: Rodando hacia la puerta


        // 5. Suben pasajeros 
        setState(AirplaneState.BOARDING);
        Thread.sleep(1000); // Pausa visual: Simulación de embarque de pasajeros

        // 6. Liberar PUERTA (Termina embarque) [cite: 68, 128, 134]
        requestAndWait(AirplaneState.BOARDED);
        Thread.sleep(600); // Pausa visual: Remolque del avión (Pushback)

        // --- FASE 3: DESPEGUE ---
        // 7. Solicitar nueva Pista para despegar [cite: 68, 129, 135]
        requestAndWait(AirplaneState.TAKEOFF_REQUESTED);
        Thread.sleep(500); // Pausa visual: Esperando turno en cabecera

        // 8. Despegar (100 ms obligatorios) 
        setState(AirplaneState.DEPARTING);
        Thread.sleep(100); 
        Thread.sleep(400); // Pausa visual: Ascenso inicial

        // 9. Finalizar y quedar en el aire [cite: 68, 130, 135]
        requestAndWait(AirplaneState.DEPARTED);
        setState(AirplaneState.IN_FLIGHT);

    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        Logger.log("Avión " + id + " interrumpido.");
    } finally {
        // El cálculo de tiempo se hace aquí para asegurar que endTime ya existe
        this.endTime = System.currentTimeMillis();
        this.duracionEnMs = this.endTime - this.startTime; // 
        Logger.log("Avión " + id + " terminó ciclo. Duración: " + duracionEnMs + "ms");
    }
}

    /**
     * Envía una petición a la torre y bloquea el hilo hasta que un 
     * operario confirme el procesamiento[cite: 112].
     */
    private void requestAndWait(AirplaneState requestType) throws InterruptedException {
        synchronized (confirmationLock) {
            processedByOperator = false; 
            tower.addRequest(new Request(this, requestType)); // Envía a la cola [cite: 111]
            
            while (!processedByOperator) {
                confirmationLock.wait(); // Bloqueo pasivo hasta notify del operario
            }
        }
    }

    /**
     * Llamado por un Operario de la torre cuando ha terminado de 
     * asignar/liberar los recursos para este avión[cite: 113].
     */
    public void confirmRequestProcessed() {
        synchronized (confirmationLock) {
            processedByOperator = true;
            confirmationLock.notify(); // Despierta al hilo del avión
        }
    }

    // --- Getters y Setters ---

    public void setState(AirplaneState state) {
        this.state = state;
        Logger.logAirplane(this.id, state.toString(), "Cambio de estado");
    }

    public AirplaneState getAirplaneState() { return state; }
    public String getAirplaneId() { return id; }
    public void setAssignedRunway(Runway r) { this.assignedRunway = r; }
    public void setAssignedGate(Gate g) { this.assignedGate = g; }
    public Runway getAssignedRunway() { return assignedRunway; }
    public Gate getAssignedGate() { return assignedGate; }
    public long getTotalTime() { return endTime - startTime; }

	public long getDuracionEnMs() {
		// TODO Auto-generated method stub
		return duracionEnMs;
	}

	public void setDuracionEnMs(long duracionEnMs) {
		this.duracionEnMs = duracionEnMs;
	}
}
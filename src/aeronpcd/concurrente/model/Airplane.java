package aeronpcd.concurrente.model;

import aeronpcd.concurrente.util.Logger;

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

    /**
     * Ciclo de vida concurrente del avión[cite: 67].
     */
    @Override
    public void run() {
        this.startTime = System.currentTimeMillis();
        
        try {
            // 1. SOLICITAR ATERRIZAJE (Pide Pista + Puerta) [cite: 68, 125]
            requestAndWait(AirplaneState.LANDING_REQUESTED);
            
            // 2. ATERRIZAR (100 ms obligatorios) 
            setState(AirplaneState.LANDING);
            Thread.sleep(100); 

            // 3. NOTIFICAR ATERRIZAJE (Libera Pista) [cite: 68, 127]
            setState(AirplaneState.LANDED);  // El avión cambia su estado
            requestAndWait(AirplaneState.LANDED);  // Luego notifica a la torre

            // 5. SUBEN PASAJEROS 
            setState(AirplaneState.BOARDING);
            Thread.sleep(150); 

            // 6. LIBERAR PUERTA (Termina embarque) [cite: 68, 128]
            setState(AirplaneState.BOARDED);  // El avión cambia su estado
            requestAndWait(AirplaneState.BOARDED);  // Luego notifica a la torre

            // 7. SOLICITAR DESPEGUE (Pide nueva Pista) [cite: 68, 129]
            requestAndWait(AirplaneState.TAKEOFF_REQUESTED);

            // 8. DESPEGAR (100 ms obligatorios) 
            setState(AirplaneState.DEPARTING);
            Thread.sleep(100); 
            
            // 9. FINALIZAR (Libera Pista y queda en el aire) [cite: 68, 130]
            setState(AirplaneState.DEPARTED);  // El avión cambia su estado PRIMERO
            requestAndWait(AirplaneState.DEPARTED);  // Luego notifica a la torre para liberar pista
            
            // Ciclo completado
            this.endTime = System.currentTimeMillis();
            this.duracionEnMs = endTime - startTime;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (this.endTime == 0) {
                this.endTime = System.currentTimeMillis();
            }
            // Aquí se generaría la entrada para el CSV final [cite: 64]
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
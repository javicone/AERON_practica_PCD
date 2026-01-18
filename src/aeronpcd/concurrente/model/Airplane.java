package aeronpcd.concurrente.model;

import aeronpcd.concurrente.util.Logger;

/**
 * Representa un Avión como un hilo independiente.
 * Ejecuta su ciclo de vida interactuando con la Torre de Control.
 */
public class Airplane extends Thread { // Cambio a Thread para concurrencia

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

    /**
     * Constructor del avión.
     * @param id Identificador único del avión (ej. "IBE-001").
     * @param tower Referencia a la Torre de Control para enviar peticiones.
     */
    public Airplane(String id, ControlTower tower) {
        this.id = id;
        this.tower = tower;
        this.state = AirplaneState.IN_FLIGHT; // Estado inicial
    }

    /**
     * Ciclo de vida concurrente del avión.
     */
    @Override
    public void run() {
        this.startTime = System.currentTimeMillis();

        try {
            // 1. SOLICITAR ATERRIZAJE (Pide Pista + Puerta)
            requestAndWait(AirplaneState.LANDING_REQUESTED);

            // 2. ATERRIZAR (100 ms obligatorios)
            setState(AirplaneState.LANDING);
            Thread.sleep(100);

            // 3. NOTIFICAR ATERRIZAJE (Libera Pista)
            setState(AirplaneState.LANDED);  // El avión cambia su estado
            requestAndWait(AirplaneState.LANDED);  // Luego notifica a la torre

            // 4. SUBEN PASAJEROS
            setState(AirplaneState.BOARDING);
            Thread.sleep(100);

            // 5. LIBERAR PUERTA (Termina embarque)
            setState(AirplaneState.BOARDED);  // El avión cambia su estado
            requestAndWait(AirplaneState.BOARDED);  // Luego notifica a la torre

            // 6. SOLICITAR DESPEGUE (Pide nueva Pista)
            requestAndWait(AirplaneState.TAKEOFF_REQUESTED);

            // 7. DESPEGAR (100 ms obligatorios)
            setState(AirplaneState.DEPARTING);
            Thread.sleep(100);

            // 8. FINALIZAR (Libera Pista y queda en el aire)
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
            // Aquí se generaría la entrada para el CSV final
        }
    }

    /**
     * Envía una petición a la torre y bloquea el hilo hasta que un
     * operario confirme el procesamiento.
     */
    private void requestAndWait(AirplaneState requestType) throws InterruptedException {
        synchronized (confirmationLock) {
            processedByOperator = false;
            tower.addRequest(new Request(this, requestType)); // Envía a la cola

            while (!processedByOperator) {
                confirmationLock.wait(); // Bloqueo pasivo hasta notify del operario
            }
        }
    }

    /**
     * Llamado por un Operario de la torre cuando ha terminado de
     * asignar/liberar los recursos para este avión.
     */
    public void confirmRequestProcessed() {
        synchronized (confirmationLock) {
            processedByOperator = true;
            confirmationLock.notify(); // Despierta al hilo del avión
        }
    }

    // --- Getters y Setters ---

    /**
     * Cambia el estado del avión y registra el cambio en el log.
     * @param state Nuevo estado del avión.
     */
    public void setState(AirplaneState state) {
        this.state = state;
        Logger.logAirplane(this.id, state.toString(), "Cambio de estado");
    }

    /**
     * Obtiene el estado actual del avión.
     * @return El estado actual del avión.
     */
    public AirplaneState getAirplaneState() { return state; }

    /**
     * Obtiene el identificador único del avión.
     * @return El ID del avión.
     */
    public String getAirplaneId() { return id; }

    /**
     * Asigna una pista al avión.
     * @param r La pista asignada.
     */
    public void setAssignedRunway(Runway r) { this.assignedRunway = r; }

    /**
     * Asigna una puerta al avión.
     * @param g La puerta asignada.
     */
    public void setAssignedGate(Gate g) { this.assignedGate = g; }

    /**
     * Obtiene la pista asignada al avión.
     * @return La pista asignada, o null si no tiene.
     */
    public Runway getAssignedRunway() { return assignedRunway; }

    /**
     * Obtiene la puerta asignada al avión.
     * @return La puerta asignada, o null si no tiene.
     */
    public Gate getAssignedGate() { return assignedGate; }

    /**
     * Obtiene el tiempo total de ejecución del avión.
     * @return Tiempo en milisegundos desde el inicio hasta el fin.
     */
    public long getTotalTime() { return endTime - startTime; }

    /**
     * Obtiene la duración total en milisegundos.
     * @return La duración en ms.
     */
    public long getDuracionEnMs() {
        return duracionEnMs;
    }

    /**
     * Establece la duración total en milisegundos.
     * @param duracionEnMs La duración a establecer.
     */
    public void setDuracionEnMs(long duracionEnMs) {
        this.duracionEnMs = duracionEnMs;
    }
}
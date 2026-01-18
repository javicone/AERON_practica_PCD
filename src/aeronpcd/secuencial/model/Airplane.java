package aeronpcd.secuencial.model;

import aeronpcd.secuencial.util.Logger;

/**
 * Representa un avión en el simulador del Aeropuerto AERON.
 * 
 * En modo secuencial, cada avión ejecuta su ciclo de vida de forma lineal:
 * aterrizaje → embarque → despegue → salida.
 * 
 * El avión solicita recursos (pistas y puertas) a la torre de control y
 * mantiene un registro de tiempos de ejecución para estadísticas.
 */
public class Airplane {

    /**
     * Identificador único del avión (ej. "IBE-001").
     */
    private String id;
    
    /**
     * Estado actual del avión en su ciclo de vida.
     */
    private AirplaneState state;
    
    /**
     * Referencia a la torre de control para solicitar recursos.
     */
    private ControlTower tower;

    /**
     * Pista de aterrizaje/despegue asignada al avión (si está disponible).
     */
    private Runway assignedRunway;
    
    /**
     * Puerta de embarque asignada al avión (si está disponible).
     */
    private Gate assignedGate;

    /**
     * Marca de tiempo de inicio del ciclo de vida del avión.
     */
    private long startTime;
    
    /**
     * Marca de tiempo de fin del ciclo de vida del avión.
     */
    private long endTime;
    
    /**
     * Duración total del ciclo en milisegundos.
     */
    private long duracionEnMs;

    /**
     * Constructor de un avión.
     * Inicializa el avión en estado IN_FLIGHT y vinculado a una torre de control.
     * 
     * @param id Identificador único del avión.
     * @param tower Referencia a la torre de control.
     */
    public Airplane(String id, ControlTower tower) {
        this.id = id;
        this.state = AirplaneState.IN_FLIGHT;
        this.tower = tower;
        this.assignedRunway = null;
        this.assignedGate = null;
    }
    /**
     * Ejecuta el ciclo de vida completo del avión en modo secuencial.
     * Sigue las fases: Aterrizaje → Embarque → Despegue.
     * 
     * Cada fase incluye solicitud de recursos a la torre, realización de la maniobra,
     * confirmación y liberación de recursos.
     */
    public void runSequentialCycle() {
        this.startTime = System.currentTimeMillis();
        Logger.logAirplane(this.id, this.state.toString(), "Inicia ciclo");
        // ----------------------------------------------------------------
        // FASE 1: ATERRIZAJE (Solicitar → Aterrizar → Liberar Pista)
        // ----------------------------------------------------------------
        
        // Paso 1: Solicitar aterrizaje
        setState(AirplaneState.LANDING_REQUESTED);
        tower.addRequest(new Request(this, AirplaneState.LANDING_REQUESTED));
        // Verificamos si nos dio permiso (nos cambió a LANDING_ASSIGNED)
        if (this.state == AirplaneState.LANDING_ASSIGNED) {
            // Paso 2: Realizar maniobra de aterrizaje
            setState(AirplaneState.LANDING);
            simulationSleep(100); // Tarda 100 ms obligatoriamente 

            // Paso 3: Notificar fin de aterrizaje para liberar la PISTA
            setState(AirplaneState.LANDED);
            tower.addRequest(new Request(this, AirplaneState.LANDED));
            // Al volver de esta llamada, la torre ya habrá liberado la pista (null).
        } else {
            System.err.println("Error crítico: Avión " + id + " no recibió pista para aterrizar.");
            return; // En secuencial, si falla aquí, se rompe el ciclo.
        }

        // ----------------------------------------------------------------
        // FASE 2: EMBARQUE (Estacionar → Subir Pasajeros → Liberar Puerta)
        // ----------------------------------------------------------------

        // Paso 4: Proceso de Embarque (el avión está en la puerta)
        setState(AirplaneState.BOARDING);
        // El enunciado no especifica tiempo exacto, ponemos algo breve para simular
        simulationSleep(100);
        
        
        // Paso 5: Notificar fin de embarque para liberar la PUERTA
        setState(AirplaneState.BOARDED);
        tower.addRequest(new Request(this, AirplaneState.BOARDED));
        // ----------------------------------------------------------------
        // FASE 3: DESPEGUE (Solicitar → Despegar → Liberar Pista)
        // ----------------------------------------------------------------

        // Paso 6: Solicitar nueva pista para despegar
        setState(AirplaneState.TAKEOFF_REQUESTED);
        tower.addRequest(new Request(this, AirplaneState.TAKEOFF_REQUESTED));
        // Verificamos si la torre nos asignó nueva pista (TAKEOFF_ASSIGNED)
        if (this.state == AirplaneState.TAKEOFF_ASSIGNED) {
            // Paso 7: Realizar maniobra de despegue
            setState(AirplaneState.DEPARTING);
            simulationSleep(100); // Tarda 100 ms obligatoriamente 

            // Paso 8: Notificar que el avión se ha ido (Liberar pista)
            setState(AirplaneState.DEPARTED);
            tower.addRequest(new Request(this, AirplaneState.DEPARTED));
        } else {
            System.err.println("Error crítico: Avión " + id + " no recibió pista para despegar.");
        }

        // Fin del ciclo
        this.endTime = System.currentTimeMillis();
        duracionEnMs = endTime-startTime;
        Logger.logAirplane(this.id, this.state.toString(), "Completó ciclo en " + getTotalTime() + " ms.");
    }

    // --- Métodos de utilidad ---

    /**
     * Pausa la ejecución del avión durante un número especificado de milisegundos.
     * Simula el tiempo requerido para realizar maniobras (aterrizaje, despegue, embarque).
     * 
     * @param ms Milisegundos a esperar.
     */
    private void simulationSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Simulación interrumpida en avión " + id);
        }
    }

    // --- Getters y Setters ---

    /**
     * Obtiene el identificador del avión.
     * 
     * @return ID único del avión.
     */
    public String getId() {
        return id;
    }

    /**
     * Cambia el estado del avión y registra el cambio en el log.
     * 
     * @param state Nuevo estado del avión.
     */
    public void setState(AirplaneState state) {
        this.state = state;
        Logger.logAirplane(this.id, state.toString(), "Cambio de estado");
    }
    
    /**
     * Obtiene el estado actual del avión.
     * 
     * @return Estado actual del avión.
     */
    public AirplaneState getState() {
        return state;
    }

    /**
     * Asigna una pista al avión.
     * 
     * @param assignedRunway Pista asignada.
     */
    public void setAssignedRunway(Runway assignedRunway) {
        this.assignedRunway = assignedRunway;
    }

    /**
     * Obtiene la pista asignada al avión.
     * 
     * @return Pista asignada, o null si no hay pista asignada.
     */
    public Runway getAssignedRunway() {
        return assignedRunway;
    }

    /**
     * Asigna una puerta al avión.
     * 
     * @param assignedGate Puerta asignada.
     */
    public void setAssignedGate(Gate assignedGate) {
        this.assignedGate = assignedGate;
    }

    /**
     * Obtiene la puerta asignada al avión.
     * 
     * @return Puerta asignada, o null si no hay puerta asignada.
     */
    public Gate getAssignedGate() {
        return assignedGate;
    }

    /**
     * Calcula el tiempo total del ciclo de vida del avión.
     * 
     * @return Tiempo total en milisegundos.
     */
    public long getTotalTime() {
        return endTime - startTime;
    }

    /**
     * Obtiene la duración total del ciclo en milisegundos.
     * 
     * @return Duración total.
     */
    public long getDuracionEnMs() {
        return duracionEnMs;
    }

    /**
     * Establece la duración total del ciclo.
     * 
     * @param duracionEnMs Duración en milisegundos.
     */
    public void setDuracionEnMs(long duracionEnMs) {
        this.duracionEnMs = duracionEnMs;
    }
}
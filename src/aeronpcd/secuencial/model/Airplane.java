package aeronpcd.secuencial.model;

import aeronpcd.secuencial.util.Logger;

// Asegúrate de importar tus recursos
// import aeronpcd.util.Runway; 
// import aeronpcd.util.Gate;

public class Airplane {

    private String id;
    private AirplaneState state;
    private ControlTower tower;

    // Recursos asignados
    private Runway assignedRunway;
    private Gate assignedGate;

    // Estadísticas
    private long startTime;
    private long endTime;
    private long duracionEnMs;

    public Airplane(String id, ControlTower tower) {
        this.id = id;
        this.tower = tower;
        this.state = AirplaneState.IN_FLIGHT; // Estado inicial [cite: 359]
    }

    /**
     * Ejecuta el ciclo de vida completo del avión (Modo Secuencial).
     * Sigue el flujo: Aterrizar -> Embarcar -> Despegar.
     */
    public void runSequentialCycle() {
        this.startTime = System.currentTimeMillis();
        System.out.println("Avión [" + this.id + "] inicia ciclo (IN_FLIGHT).");
        Logger.logAirplane(this.id, this.state.toString(), "Inicia ciclo");
        // ----------------------------------------------------------------
        // FASE 1: ATERRIZAJE (Solicitar -> Aterrizar -> Liberar Pista)
        // ----------------------------------------------------------------
        
        // 1. Solicitar Aterrizaje
        setState(AirplaneState.LANDING_REQUESTED); // [cite: 360]
        tower.addRequest(new Request(this, AirplaneState.LANDING_REQUESTED));
        simulationSleep(500);
        // En modo secuencial, la torre responde al instante. 
        // Verificamos si nos dio permiso (nos cambió a LANDING_ASSIGNED [cite: 361]).
        if (this.state == AirplaneState.LANDING_ASSIGNED) {
        	Logger.logAirplane(this.id, this.state.toString(), "El avión está en " + this.state);
        	simulationSleep(500);
        	// 2. Realizar maniobra de aterrizaje
            setState(AirplaneState.LANDING); // [cite: 362]
            Logger.logAirplane(this.id, this.state.toString(), "El avión está en " + this.state);
            simulationSleep(800); // Tarda 100 ms obligatoriamente 

            // 3. Notificar fin de aterrizaje para liberar la PISTA
            setState(AirplaneState.LANDED); // [cite: 363]
            simulationSleep(500);
            tower.addRequest(new Request(this, AirplaneState.LANDED));
            // Al volver de esta llamada, la torre ya habrá liberado la pista (null).
        } else {
            System.err.println("Error crítico: Avión " + id + " no recibió pista para aterrizar.");
            return; // En secuencial, si falla aquí, se rompe el ciclo.
        }

        // ----------------------------------------------------------------
        // FASE 2: EMBARQUE (Estacionar -> Subir Pasajeros -> Liberar Puerta)
        // ----------------------------------------------------------------

        simulationSleep(500);
        // 4. Proceso de Embarque (el avión está en la puerta)
        setState(AirplaneState.BOARDING); // [cite: 364]
        // El enunciado no especifica tiempo exacto, ponemos algo breve para simular
        simulationSleep(500);
        simulationSleep(50); 
        
        // 5. Notificar fin de embarque para liberar la PUERTA
        setState(AirplaneState.BOARDED); // [cite: 365]
        tower.addRequest(new Request(this, AirplaneState.BOARDED));
        simulationSleep(500);
        // ----------------------------------------------------------------
        // FASE 3: DESPEGUE (Solicitar -> Despegar -> Liberar Pista)
        // ----------------------------------------------------------------

        // 6. Solicitar nueva pista para despegar
        setState(AirplaneState.TAKEOFF_REQUESTED); // [cite: 366]
        tower.addRequest(new Request(this, AirplaneState.TAKEOFF_REQUESTED));
        simulationSleep(500);
        // Verificamos si la torre nos asignó nueva pista (TAKEOFF_ASSIGNED [cite: 367])
        if (this.state == AirplaneState.TAKEOFF_ASSIGNED) {
        	simulationSleep(500);
            // 7. Realizar maniobra de despegue
            setState(AirplaneState.DEPARTING); // [cite: 368]
            simulationSleep(100); // Tarda 100 ms obligatoriamente 

            // 8. Notificar que el avión se ha ido (Liberar pista)
            setState(AirplaneState.DEPARTED); // [cite: 369]
            tower.addRequest(new Request(this, AirplaneState.DEPARTED));
            simulationSleep(500);
        } else {
            System.err.println("Error crítico: Avión " + id + " no recibió pista para despegar.");
        }

        // Fin del ciclo
        this.endTime = System.currentTimeMillis();
        duracionEnMs = endTime-startTime;
        System.out.println("Avión [" + this.id + "] completó ciclo en " + getTotalTime() + " ms.");
    }

    // --- Métodos de utilidad ---

    private void simulationSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Simulación interrumpida en avión " + id);
        }
    }

    // --- Getters y Setters ---

    public String getId() {
        return id;
    }

    public void setState(AirplaneState state) {
        this.state = state;
        // Opcional: Imprimir cambio de estado si quieres traza detallada
        // System.out.println("Avión " + id + " cambia estado a: " + state);
    }
    
    public AirplaneState getState() {
        return state;
    }

    public void setAssignedRunway(Runway assignedRunway) {
        this.assignedRunway = assignedRunway;
    }

    public Runway getAssignedRunway() {
        return assignedRunway;
    }

    public void setAssignedGate(Gate assignedGate) {
        this.assignedGate = assignedGate;
    }

    public Gate getAssignedGate() {
        return assignedGate;
    }

    public long getTotalTime() {
        return endTime - startTime;
    }

	public long getDuracionEnMs() {
		return duracionEnMs;
	}

	public void setDuracionEnMs(long duracionEnMs) {
		this.duracionEnMs = duracionEnMs;
	}
}
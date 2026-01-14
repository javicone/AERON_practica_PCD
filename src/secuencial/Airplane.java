package secuencial;

import java.sql.Time;

public class Airplane {

	private String id;
	private AirplaneState state;
	private ControlTower tower;
	private long executionTime;
	private long startTime;
	
	/**
     * Método principal para ejecutar el ciclo en MODO SECUENCIAL.
     * Llama a este método desde tu Main dentro de un bucle.
     */
    public void runSequentialCycle() {
        this.startTime = System.currentTimeMillis();

        // 1. Inicio
        setState(AirplaneState.IN_FLIGHT);

        // ---------------------------------------------------------
        // FASE DE ATERRIZAJE
        // ---------------------------------------------------------
        
        // 2. Solicitar Aterrizaje
        setState(AirplaneState.LANDING_REQUESTED);
        // Creamos la petición y la mandamos a la torre
        ControlTower.Request reqLanding = new ControlTower.Request(this, RequestType.LANDING);
        tower.addRequest(reqLanding); 
        // NOTA: En secuencial, al volver de addRequest, la torre YA nos ha asignado recursos 
        // porque processNextRequest() se ejecutó internamente.
        
        // Verificación defensiva (si la torre funcionó bien, deberíamos tener recursos)
        if (this.state == AirplaneState.LANDING_ASSIGNED) {
            logger.log("Avión [" + this.id + "] comienza maniobra de aterrizaje en " + assignedRunway.getId());
        }

        // 3. Aterrizando (Simulación de tiempo)
        setState(AirplaneState.LANDING);
        try {
            Thread.sleep(100); //  Aterrizar (100 ms)
        } catch (InterruptedException e) { e.printStackTrace(); }

        // 4. Aterrizado (CRÍTICO: Liberar Pista)
        // Aunque no tengas el estado en tu Enum, lógica de negocio requiere liberar pista aquí.
        // Si añades LANDED a tu enum, usa: setState(AirplaneState.LANDED);
        logger.log("Avión [" + this.id + "] ha aterrizado. Notificando a torre para liberar pista.");
        
        // Notificamos a la torre que ya aterrizamos (Tipo LANDED) para que libere la PISTA 
        tower.addRequest(new ControlTower.Request(this, RequestType.LANDED));

        // ---------------------------------------------------------
        // FASE DE EMBARQUE
        // ---------------------------------------------------------

        // 5. Embarcando (Ocupando puerta)
        setState(AirplaneState.BOARDING);
        logger.log("Avión [" + this.id + "] estacionado en " + assignedGate.getId() + ". Pasajeros subiendo...");
        // Simulación breve de embarque (opcional, el enunciado no da tiempo exacto, pero da realismo)
        try { Thread.sleep(50); } catch (InterruptedException e) {}

        // 6. Embarcado (CRÍTICO: Liberar Puerta)
        // Si añades BOARDED a tu enum: setState(AirplaneState.BOARDED);
        logger.log("Avión [" + this.id + "] fin de embarque. Liberando puerta.");
        
        // Notificamos a torre (Tipo BOARDED) para liberar PUERTA 
        tower.addRequest(new ControlTower.Request(this, RequestType.BOARDED));

        // ---------------------------------------------------------
        // FASE DE DESPEGUE
        // ---------------------------------------------------------

        // 7. Solicitar Despegue
        setState(AirplaneState.TAKING_OFF_REQUESTED);
        tower.addRequest(new ControlTower.Request(this, RequestType.TAKEOFF));
        // Al volver, la torre nos habrá asignado una NUEVA pista [cite: 36]

        if (this.state == AirplaneState.TAKING_OFF_ASSIGNED) {
             logger.log("Avión [" + this.id + "] inicia despegue en " + assignedRunway.getId());
        }

        // 8. Despegando
        setState(AirplaneState.DEPARTING);
        try {
            Thread.sleep(100); //  Despegar (100 ms)
        } catch (InterruptedException e) { e.printStackTrace(); }

        // 9. Fin del ciclo (Liberar Pista de despegue)
        // Notificamos DEPARTED para liberar la pista de despegue [cite: 37]
        tower.addRequest(new ControlTower.Request(this, RequestType.DEPARTED));
        
        setState(AirplaneState.FINISHED);
        this.endTime = System.currentTimeMillis();
        
        logger.log("Avión [" + this.id + " - FINISHED] Ciclo completado en " + getTotalCycleTime() + "ms");
    }

    // --- Getters y Setters necesarios para la Torre ---

    public void setStatus(AirplaneState newState) {
        this.state = newState;
        // Opcional: Loguear cada cambio de estado si quieres mucho detalle
    }
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @return the state
	 */
	public AirplaneState getState() {
		return state;
	}
	/**
	 * @return the tower
	 */
	public ControlTower getTower() {
		return tower;
	}
	/**
	 * @return the executionTime
	 */
	public long getExecutionTime() {
		return executionTime;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @param state the state to set
	 */
	public void setState(AirplaneState state) {
		this.state = state;
	}
	/**
	 * @param tower the tower to set
	 */
	public void setTower(ControlTower tower) {
		this.tower = tower;
	}
	/**
	 * @param executionTime the executionTime to set
	 */
	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}
	public long getTotalCycleTime() {
		// TODO Auto-generated method stub
		return 0;
	}
}

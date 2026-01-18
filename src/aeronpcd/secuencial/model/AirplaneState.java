package aeronpcd.secuencial.model;

/**
 * Enumeración de los estados posibles de un avión durante su ciclo de vida.
 * 
 * Estados de aterrizaje: IN_FLIGHT → LANDING_REQUESTED → LANDING_ASSIGNED → LANDING → LANDED
 * Estados de embarque: BOARDING → BOARDED
 * Estados de despegue: TAKEOFF_REQUESTED → TAKEOFF_ASSIGNED → DEPARTING → DEPARTED
 */
public enum AirplaneState {
    
    /**
     * Avión en vuelo / Iniciando el ciclo de vida.
     */
    IN_FLIGHT,
    
    /**
     * Avión solicitando permiso de aterrizaje a la torre.
     */
    LANDING_REQUESTED,
    
    /**
     * Avión con aterrizaje autorizado y pista asignada.
     */
    LANDING_ASSIGNED,
    
    /**
     * Avión realizando la maniobra de aterrizaje.
     */
    LANDING,
    
    /**
     * Avión ha aterrizando y libera la pista.
     */
    LANDED,
    
    /**
     * Avión en puerta y embarcando pasajeros.
     */
    BOARDING,
    
    /**
     * Avión completó el embarque de pasajeros y libera la puerta.
     */
    BOARDED,
    
    /**
     * Avión solicitando nueva pista para despegar.
     */
    TAKEOFF_REQUESTED,
    
    /**
     * Avión con despegue autorizado y pista asignada.
     */
    TAKEOFF_ASSIGNED,
    
    /**
     * Avión realizando la maniobra de despegue.
     */
    DEPARTING,
    
    /**
     * Avión ha despegado y completa su ciclo de vida.
     */
    DEPARTED
}

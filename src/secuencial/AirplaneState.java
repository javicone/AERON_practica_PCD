package secuencial;

public enum AirplaneState {
	IN_FLIGHT,              // En vuelo / Iniciando
    LANDING_REQUESTED,      // Solicitando aterrizaje (estado intermedio útil)
    LANDING_ASSIGNED,       // Aterrizaje asignado (tiene pista)
    LANDING,                // Aterrizando (ocupando pista)
    BOARDING,               // En puerta / Embarcando
    TAKING_OFF_REQUESTED,   // Solicitando despegue
    TAKING_OFF_ASSIGNED,    // Despegue asignado (tiene pista nueva)
    DEPARTING,              // Despegando
    FINISHED    			// Ciclo completado}
}

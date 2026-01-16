package aeronpcd.secuencial.model;

public enum AirplaneState {
	IN_FLIGHT,              // En vuelo / Iniciando
    LANDING_REQUESTED,      // Solicitando aterrizaje (estado intermedio útil)
    LANDING_ASSIGNED,       // Aterrizaje asignado (tiene pista)
    LANDING,                // Aterrizando (ocupando pista)
    LANDED,
    BOARDING,               // En puerta / Embarcando
    BOARDED,
    TAKEOFF_REQUESTED,   // Solicitando despegue
    TAKEOFF_ASSIGNED,    // Despegue asignado (tiene pista nueva)
    DEPARTING,              // Despegando
    DEPARTED    			// Ciclo completado}
}

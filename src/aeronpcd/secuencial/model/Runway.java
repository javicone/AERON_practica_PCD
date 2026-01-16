package aeronpcd.secuencial.model;

/**
 * Representa una pista de aterrizaje o despegue en el Aeropuerto AERON.
 * El aeropuerto dispone de 3 pistas en total[cite: 16, 33].
 */
public class Runway {
    private String id;             // Identificador (ej. "PIS1") 
    private boolean isFree;        // Estado de disponibilidad
    private Airplane currentPlane; // Avión que ocupa la pista actualmente

    /**
     * Constructor de la pista.
     * @param id Identificador único de la pista.
     */
    public Runway(String id) {
        this.id = id;
        this.isFree = true;
        this.currentPlane = null;
    }

    /**
     * Asigna la pista a un avión para una maniobra (aterrizaje o despegue).
     * Cada pista solo puede ser usada por un avión simultáneamente[cite: 41].
     * @param plane El avión que ocupará la pista.
     */
    public void occupy(Airplane plane) {
        this.isFree = false;
        this.currentPlane = plane;
    }

    /**
     * Libera la pista. 
     * Se debe llamar cuando el avión termina de aterrizar (LANDED) 
     * o cuando termina de despegar (DEPARTED)[cite: 341, 343].
     */
    public void release() {
        this.isFree = true;
        this.currentPlane = null;
    }

    // --- Getters necesarios para AirportState y Torre de Control ---

    public String getId() {
        return id;
    }

    /**
     * Método requerido por AirportState.java para mostrar el estado visual.
     * @return true si la pista está libre.
     */
    public boolean isAvailable() {
        return isFree;
    }

    public Airplane getCurrentPlane() {
        return currentPlane;
    }

    @Override
    public String toString() {
        return "Pista [" + id + (isFree ? " - LIBRE]" : " - OCUPADA por " + currentPlane.getId() + "]");
    }
}

package aeronpcd.concurrente.model;

/**
 * Representa una pista de aterrizaje o despegue en el Aeropuerto AERON.
 * El aeropuerto dispone de 3 pistas en total, utilizadas por los aviones
 * para las operaciones de aterrizaje y despegue.
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
     * Cada pista solo puede ser usada por un avión simultáneamente.
     * 
     * @param plane El avión que ocupará la pista.
     */
    public void occupy(Airplane plane) {
        this.isFree = false;
        this.currentPlane = plane;
    }

    /**
     * Libera la pista, dejándola disponible para el siguiente avión.
     * Se debe llamar cuando el avión termina de aterrizar (LANDED) 
     * o cuando termina de despegar (DEPARTED).
     */
    public void release() {
        this.isFree = true;
        this.currentPlane = null;
    }

    // --- Getters necesarios para AirportState y Torre de Control ---

    /**
     * Obtiene el ID de la pista.
     * 
     * @return Identificador único de la pista (ej. "PIS1").
     */
    public String getId() {
        return id;
    }

    /**
     * Verifica si la pista está disponible para una nueva operación.
     * 
     * @return true si la pista está libre, false si está ocupada.
     */
    public boolean isAvailable() {
        return isFree;
    }

    /**
     * Obtiene el avión que está ocupando actualmente la pista.
     * 
     * @return Referencia al avión que ocupa la pista, o null si está libre.
     */
    public Airplane getCurrentPlane() {
        return currentPlane;
    }

    /**
     * Representación en texto del estado de la pista.
     * 
     * @return Cadena con formato: "Pista [ID - LIBRE]" o "Pista [ID - OCUPADA por ID_AVION]".
     */
    @Override
    public String toString() {
        return "Pista [" + id + (isFree ? " - LIBRE]" : " - OCUPADA por " + currentPlane.getId() + "]");
    }
}

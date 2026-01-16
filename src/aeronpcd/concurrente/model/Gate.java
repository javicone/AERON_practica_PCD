package aeronpcd.concurrente.model;

/**
 * Representa una puerta de embarque en el Aeropuerto AERON.
 * En el modo secuencial, no requiere sincronización, pero sí control de estado.
 */
public class Gate {
    private String id;        // Identificador (ej. PUE1) 
    private boolean isFree;   // Estado de disponibilidad
    private Airplane currentPlane; // Avión que ocupa la puerta (útil para el panel de vuelos)

    /**
     * Constructor para inicializar la puerta con un identificador único.
     * @param id El nombre de la puerta.
     */
    public Gate(String id) {
        this.id = id;
        this.isFree = true; // Por defecto, las puertas empiezan libres
        this.currentPlane = null;
    }

    /**
     * Asigna un avión a la puerta.
     * @param plane El avión que va a estacionar.
     */
    public void occupy(Airplane plane) {
        this.isFree = false;
        this.currentPlane = plane;
    }

    /**
     * Libera la puerta para que pueda ser usada por otro avión.
     * Se llama cuando el avión termina el proceso de BOARDED[cite: 336, 342].
     */
    public void release() {
        this.isFree = true;
        this.currentPlane = null;
    }

    // --- Getters y Setters ---

    public String getId() {
        return id;
    }

    public boolean isFree() {
        return isFree;
    }

    /**
     * Método compatible con la clase AirportState proporcionada.
     */
    public boolean isOccupied() {
        return !isFree;
    }

    public Airplane getCurrentPlane() {
        return currentPlane;
    }

    @Override
    public String toString() {
        return "Puerta [" + id + (isFree ? " - LIBRE]" : " - OCUPADA por " + currentPlane.getId() + "]");
    }
}

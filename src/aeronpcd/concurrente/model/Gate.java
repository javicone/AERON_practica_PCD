package aeronpcd.concurrente.model;

/**
 * Representa una puerta de embarque en el Aeropuerto AERON.
 * Gestiona el estado de ocupación de una puerta y el avión que la utiliza.
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
     * Se llama cuando el avión termina el proceso de embarque (BOARDED).
     */
    public void release() {
        this.isFree = true;
        this.currentPlane = null;
    }

    // --- Getters y Setters ---

    /**
     * Obtiene el identificador de la puerta.
     * @return El ID de la puerta.
     */
    public String getId() {
        return id;
    }

    /**
     * Verifica si la puerta está disponible.
     * @return true si la puerta está libre, false si está ocupada.
     */
    public boolean isFree() {
        return isFree;
    }

    /**
     * Verifica si la puerta está ocupada.
     * @return true si la puerta está ocupada, false si está libre.
     */
    public boolean isOccupied() {
        return !isFree;
    }

    /**
     * Obtiene el avión que actualmente ocupa la puerta.
     * @return El avión asignado a la puerta, o null si está vacía.
     */
    public Airplane getCurrentPlane() {
        return currentPlane;
    }

    /**
     * Genera una representación textual del estado de la puerta.
     * @return Cadena con el ID y estado (LIBRE u OCUPADA) de la puerta.
     */
    @Override
    public String toString() {
        return "Puerta [" + id + (isFree ? " - LIBRE]" : " - OCUPADA por " + currentPlane.getId() + "]");
    }
}

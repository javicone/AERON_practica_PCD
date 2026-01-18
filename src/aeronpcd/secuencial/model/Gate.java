package aeronpcd.secuencial.model;

/**
 * Representa una puerta de embarque en el Aeropuerto AERON.
 * Gestiona el estado de ocupación de una puerta y registra qué avión la ocupa.
 */
public class Gate {
    
    /**
     * Identificador único de la puerta (ej. "G1").
     */
    private String id;
    
    /**
     * Indica si la puerta está disponible (libre) o no.
     */
    private boolean isFree;
    
    /**
     * Referencia al avión que ocupa actualmente la puerta, útil para el panel de vuelos.
     */
    private Airplane currentPlane;

    /**
     * Constructor de una puerta de embarque.
     * Inicializa la puerta como disponible sin avión asignado.
     * 
     * @param id Identificador único de la puerta.
     */
    public Gate(String id) {
        this.id = id;
        this.isFree = true; // Por defecto, las puertas empiezan libres
        this.currentPlane = null;
    }

    /**
     * Asigna un avión a la puerta marcándola como ocupada.
     * 
     * @param plane Avión que se estaciona en la puerta.
     */
    public void occupy(Airplane plane) {
        this.isFree = false;
        this.currentPlane = plane;
    }

    /**
     * Libera la puerta haciéndola disponible para otro avión.
     * Se llama cuando el avión completó el embarque de pasajeros.
     */
    public void release() {
        this.isFree = true;
        this.currentPlane = null;
    }

    // --- Getters y Setters ---

    /**
     * Obtiene el identificador de la puerta.
     * 
     * @return ID único de la puerta.
     */
    public String getId() {
        return id;
    }

    /**
     * Verifica si la puerta está disponible (libre).
     * 
     * @return true si la puerta está libre, false si está ocupada.
     */
    public boolean isFree() {
        return isFree;
    }

    /**
     * Verifica si la puerta está ocupada.
     * Método compatible con AirportState.java para visualización.
     * 
     * @return true si la puerta está ocupada, false si está libre.
     */
    public boolean isOccupied() {
        return !isFree;
    }

    /**
     * Obtiene el avión que ocupa actualmente la puerta.
     * 
     * @return Avión que ocupa la puerta, o null si está libre.
     */
    public Airplane getCurrentPlane() {
        return currentPlane;
    }

    /**
     * Representación en texto del estado de la puerta.
     * 
     * @return String con formato: "Puerta [ID - LIBRE]" o "Puerta [ID - OCUPADA por ID_AVION]".
     */
    @Override
    public String toString() {
        return "Puerta [" + id + (isFree ? " - LIBRE]" : " - OCUPADA por " + currentPlane.getId() + "]");
    }
}

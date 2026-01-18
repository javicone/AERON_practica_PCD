package aeronpcd.secuencial.model;

/**
 * Representa a un pasajero del avión.
 * Según las reglas de simplificación, solo hay un pasajero por avión.
 * El pasajero se vincula a un avión específico y participa en el ciclo de vida
 * de la aeronave (embarque y desembarque).
 */
public class Passenger {
    
    /**
     * Identificador numérico único del pasajero.
     */
    private int id;
    
    /**
     * Nombre del pasajero para mayor realismo en los logs.
     */
    private String nombre;
    
    /**
     * Referencia al avión al que está asignado este pasajero.
     */
    private Airplane planeAssigned;

    /**
     * Constructor de un pasajero.
     * 
     * @param id Identificador numérico único del pasajero.
     * @param nombre Nombre del pasajero para los registros y logs.
     * @param plane Avión al que está asignado este pasajero.
     */
    public Passenger(int id, String nombre, Airplane plane) {
        this.id = id;
        this.nombre = nombre;
        this.planeAssigned = plane;
    }

    /**
     * Simula la acción de embarque del pasajero en su avión asignado.
     * Este método se llama cuando el avión está en estado BOARDING.
     */
    public void board() {
        // En el log se debe ver que el pasajero sube al avión asignado
        System.out.println("Pasajero [" + nombre + "] subiendo al avión " + planeAssigned.getId() + "...");
    }

    // --- Getters y Setters ---

    /**
     * Obtiene el identificador numérico del pasajero.
     * 
     * @return ID del pasajero.
     */
    public int getId() {
        return id;
    }

    /**
     * Obtiene el nombre del pasajero.
     * 
     * @return Nombre del pasajero.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene el avión al que está asignado este pasajero.
     * 
     * @return Referencia al avión asignado.
     */
    public Airplane getPlaneAssigned() {
        return planeAssigned;
    }

    /**
     * Asigna un nuevo avión a este pasajero.
     * 
     * @param planeAssigned Nuevo avión a asignar.
     */
    public void setPlaneAssigned(Airplane planeAssigned) {
        this.planeAssigned = planeAssigned;
    }

    @Override
    public String toString() {
        return "Pasajero: " + nombre + " (ID: " + id + ")";
    }
}

package aeronpcd.concurrente.model;

/**
 * Representa al pasajero del avión.
 * Según las reglas, solo hay un pasajero por avión para simplificar la simulación.
 */
public class Passenger {
    private int id;
    private String nombre;
    private Airplane planeAssigned; // Avión al que pertenece este pasajero

    /**
     * Constructor para el pasajero.
     * @param id Identificador numérico.
     * @param nombre Nombre del pasajero (para dar realismo [cite: 57]).
     * @param plane El avión asignado.
     */
    public Passenger(int id, String nombre, Airplane plane) {
        this.id = id;
        this.nombre = nombre;
        this.planeAssigned = plane;
    }

    /**
     * Simula la acción de subir al avión.
     * Este método se llamará cuando el avión esté en estado BOARDING.
     */
    public void board() {
        // En el log se debe ver que el pasajero sube al avión asignado
        System.out.println("Pasajero [" + nombre + "] subiendo al avión " + planeAssigned.getId() + "...");
    }

    // --- Getters y Setters ---

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Airplane getPlaneAssigned() {
        return planeAssigned;
    }

    public void setPlaneAssigned(Airplane planeAssigned) {
        this.planeAssigned = planeAssigned;
    }

    @Override
    public String toString() {
        return "Pasajero: " + nombre + " (ID: " + id + ")";
    }
}

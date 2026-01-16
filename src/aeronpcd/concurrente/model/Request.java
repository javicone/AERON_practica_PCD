package aeronpcd.concurrente.model;

// Si usas un Enum para el tipo, impórtalo o defínelo aquí también.
// Suponiendo que RequestType es un enum en su propio archivo o parte del paquete.

public class Request {
    
    private Airplane airplane;       // ¿Quién pide?
    private AirplaneState type;        // ¿Qué pide? (LANDING, TAKEOFF...)
    private long timestamp;          // (Opcional) ¿Cuándo lo pidió? Útil para logs

    // Constructor
    public Request(Airplane airplane, AirplaneState type) {
        this.airplane = airplane;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public Airplane getAirplane() {
        return airplane;
    }

    public AirplaneState getType() {
        return type;
    }

    // Método toString para facilitar el Debug y los Logs
    @Override
    public String toString() {
        return "Petición [" + type + "] del Avión " + airplane.getId();
    }
}
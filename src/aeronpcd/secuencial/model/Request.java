package aeronpcd.secuencial.model;

// Si usas un Enum para el tipo, impórtalo o defínelo aquí también.
// Suponiendo que RequestType es un enum en su propio archivo o parte del paquete.

/**
 * Representa una petición (request) enviada por un avión a la torre de control.
 * 
 * Una petición encapsula toda la información necesaria para que la torre
 * asigne recursos (pista y puerta) al avión solicitante.
 * 
 * Las peticiones se encolan en la cola de la torre de control y son
 * procesadas de forma secuencial.
 */
public class Request {
    
    /**
     * Avión que realiza la petición.
     */
    private Airplane airplane;
    
    /**
     * Tipo de acción solicitada (LANDING, TAKEOFF...).
     */
    private AirplaneState type;
    
    /**
     * Marca de tiempo (millisegundos) cuando se creó la petición.
     */
    private long timestamp;

    /**
     * Constructor de una petición.
     * 
     * @param airplane Avión que realiza la petición.
     * @param type Tipo de acción solicitada (LANDING, BOARDING, TAKEOFF, DEPARTURE).
     */
    public Request(Airplane airplane, AirplaneState type) {
        this.airplane = airplane;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    
    /**
     * Obtiene el avión que realizó la petición.
     * 
     * @return Referencia al avión solicitante.
     */
    public Airplane getAirplane() {
        return airplane;
    }

    /**
     * Obtiene el tipo de acción solicitada por el avión.
     * 
     * @return Estado del avión (LANDING, BOARDING, TAKEOFF, DEPARTURE).
     */
    public AirplaneState getType() {
        return type;
    }

    /**
     * Representación en texto de la petición para debugging y logging.
     * 
     * @return Cadena con formato: "Petición [TIPO] del Avión ID".
     */
    @Override
    public String toString() {
        return "Petición [" + type + "] del Avión " + airplane.getId();
    }
}
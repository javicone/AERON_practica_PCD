package aeronpcd.concurrente.model;

/**
 * Representa una petición (request) enviada por un avión a la torre de control.
 * 
 * Una petición encapsula toda la información necesaria para que la torre
 * asigne recursos (pista y puerta) al avión solicitante.
 * 
 * Las peticiones se encolan en la cola de la torre de control y son
 * procesadas por los operarios de forma concurrente.
 */
public class Request {
    
    private Airplane airplane;       // Avión que realiza la petición
    private AirplaneState type;      // Tipo de acción solicitada (LANDING, TAKEOFF...)
    private long timestamp;          // Marca de tiempo (millisegundos) cuando se creó la petición

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
        return "Petición [" + type + "] del Avión " + airplane.getAirplaneId();
    }
}
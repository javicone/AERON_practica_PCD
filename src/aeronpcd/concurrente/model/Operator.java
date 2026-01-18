package aeronpcd.concurrente.model;

import aeronpcd.concurrente.util.Logger;

/**
 * Representa a un Operario de la Torre de Control.
 * Hilo Consumidor: Saca peticiones de la cola y las procesa de forma concurrente.
 */
public class Operator extends Thread {

    private final int id;
    private final ControlTower tower;
    
    /**
     * Constructor del Operario.
     * @param id Identificador único del operario.
     * @param tower Referencia a la Torre de Control para procesar peticiones.
     */
    public Operator(int id, ControlTower tower) {
        this.id = id;
        this.tower = tower;
        setName("Operario-" + id); // Para depuración
    }

    /**
     * Ejecuta el ciclo de procesamiento de peticiones del operario.
     * Lee peticiones de la cola de forma bloqueante (espera si está vacía),
     * las procesa asignando/liberando recursos, y si no hay recursos disponibles,
     * reencola la petición para intentarlo más tarde.
     */
    @Override
    public void run() {
        Logger.log("Operario " + id + " iniciando turno.");
        try {
            while (!isInterrupted()) {
                // 1. Obtener siguiente petición (bloqueante por semáforo)
                // Si no hay nada en la cola, el hilo se queda dormido aquí
                Request req = tower.getNextRequest();
                
                // 2. Intentar procesarla (asignar recursos con monitor)
                Logger.log("Operario " + id + " atiende petición de Avión " + req.getAirplane().getAirplaneId());

                boolean processed = tower.processRequest(req, id);
                
                if (processed) {
                    // Si tuvo éxito (había recursos), avisamos al avión
                    req.getAirplane().confirmRequestProcessed();
                } else {
                    // Si no había recursos (no hay pista/puerta libre),
                    // reencolamos la petición para intentarlo más tarde
                    Logger.log("Operario " + id + ": Recursos ocupados, reencolando petición de " + req.getAirplane().getAirplaneId());
                    tower.addRequest(req); 
                    
                    // Esperamos un poco para no saturar la cola reintentando constantemente
                    Thread.sleep(50); 
                }
            }
        } catch (InterruptedException e) {
            Logger.log("Operario " + id + " finaliza su turno (interrumpido).");
        }
    }
}
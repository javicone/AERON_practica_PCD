package aeronpcd.concurrente.model;

import aeronpcd.concurrente.util.Logger;

/**
 * Representa a un Operario de la Torre de Control.
 * Hilo Consumidor: Saca peticiones de la cola y las procesa.
 */
public class Operator extends Thread {

    private final int id;
    private final ControlTower tower;
    
    public Operator(int id, ControlTower tower) {
        this.id = id;
        this.tower = tower;
        setName("Operario-" + id); // Para depuración
    }

    @Override
    public void run() {
        Logger.log("Operario " + id + " iniciando turno.");
        try {
            while (!isInterrupted()) {
                // 1. Obtener siguiente petición (Bloqueante por Semáforo)
                // Si no hay nada en la cola, el hilo se queda "dormido" aquí.
                Request req = tower.getNextRequest();
                
                // 2. Intentar procesarla (Asignar recursos con Monitor)
                Logger.log("Operario " + id + " atiende petición de Avión " + req.getAirplane().getAirplaneId());
                
                // Simulamos un pequeño tiempo de "pensar" o gestión administrativa (opcional)
                Thread.sleep(50); 
                
                boolean processed = tower.processRequest(req, id);
                
                if (processed) {
                    // Si tuvo éxito (había recursos), avisamos al avión
                    req.getAirplane().confirmRequestProcessed();
                } else {
                    // ¡OJO! Si no había recursos (ej. no hay pista libre):
                    // Opción A (Simple): Devolver a la cola para intentarlo luego.
                    // Opción B (Robusta): En tu lógica de processRequest, si es 'release' siempre funciona.
                    // Si es 'assign' (LANDING/TAKEOFF), si retorna false es que no hay sitio.
                    // El enunciado dice "esperar hasta que se liberen".
                    
                    // Como processRequest está sincronizado, si retorna false significa que miró y estaba ocupado.
                    // Lo correcto es volver a meter la petición en la cola para no perderla.
                    Logger.log("Operario " + id + ": Recursos ocupados, reencolando petición de " + req.getAirplane().getAirplaneId());
                    tower.addRequest(req); 
                    
                    // Esperamos un poco para no saturar la cola reintentando a lo loco
                    Thread.sleep(100); 
                }
            }
        } catch (InterruptedException e) {
            Logger.log("Operario " + id + " finaliza su turno (interrumpido).");
        }
    }
}
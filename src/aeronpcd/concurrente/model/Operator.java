package aeronpcd.concurrente.model;

import aeronpcd.concurrente.util.Logger;

public class Operator extends Thread {
    private final int id;
    private final ControlTower tower;

    public Operator(int id, ControlTower tower) {
        this.id = id;
        this.tower = tower;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Request req = tower.getNextRequest(); // Saca una
                
                boolean processed = tower.processRequest(req, this.id);
                
                if (!processed) {
                    // Si no hay recursos, la devolvemos al final de la cola
                    // Así no se pierde y permitimos que pasen peticiones de liberación
                    tower.addRequest(req); 
                    Thread.sleep(50); // Pequeña pausa para no saturar la CPU
                } else {
                    // Solo si se procesó con éxito, despertamos al avión
                    req.getAirplane().confirmRequestProcessed();
                }
            }
        } catch (InterruptedException e) {
            Logger.log("Operario " + id + " finalizado.");
        }
    }
}
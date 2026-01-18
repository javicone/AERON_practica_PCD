package aeronpcd.concurrente.model;

import aeronpcd.concurrente.exceptions.*;
import aeronpcd.concurrente.util.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase principal para ejecutar la simulación en modo concurrente.
 */
public class MainConcurrente {

    /**
     * Ejecuta la simulación concurrente del aeropuerto.
     * Inicializa la Torre de Control, crea operarios y aviones como hilos,
     * lanza la simulación y genera los reportes finales (logs y CSV).
     * @param numAviones Número de aviones a simular.
     * @param numPistas Número de pistas disponibles.
     * @param numPuertas Número de puertas de embarque disponibles.
     * @param numOperarios Número de operarios concurrentes de la torre.
     */
    public static void runSimulation(int numAviones, int numPistas, int numPuertas, int numOperarios) {
        
        List<Airplane> airplaneThreads = new ArrayList<>();
        List<Operator> operatorThreads = new ArrayList<>();
        long tiempoInicio = System.currentTimeMillis();

        try {
            // Inicialización con parámetros dinámicos
            try {
                Logger.setup("CONCURRENT", numAviones, numPistas, numPuertas, numOperarios);
            } catch (LogWriteException e) {
                System.err.println(e.getMessage());
                return;
            }
            Logger.log("=== INICIO DE SIMULACIÓN CONCURRENTE ===");
            
            FlightPanelJSON.getInstance().configure("CONCURRENT", numAviones, numPistas, numPuertas, numOperarios);
            
            Window window = new Window();
            
            // Pasamos las pistas/puertas dinámicas al constructor
            ControlTower tower = new ControlTower(window, numPistas, numPuertas);

            // Crear Operarios dinámicos
            for (int i = 1; i <= numOperarios; i++) {
                Operator op = new Operator(i, tower);
                operatorThreads.add(op);
                op.start();
            }

            // Crear Aviones dinámicos
            for (int i = 1; i <= numAviones; i++) {
                String id = String.format("IBE-%03d", i);
                Airplane plane = new Airplane(id, tower);
                airplaneThreads.add(plane);
            }

            try {
                tower.registerAirplanes(airplaneThreads);
            } catch (FlightPanelException e) {
                Logger.log("[ERROR] " + e.getMessage());
            }

            // Lanzar hilos
            for (Airplane plane : airplaneThreads) plane.start();

            // Wait (Join)
            for (Airplane plane : airplaneThreads) {
                try { plane.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            
            // Verificación post-vuelo
            for (Airplane plane : airplaneThreads) {
                if (plane.getAirplaneState() != AirplaneState.DEPARTED) {
                    throw new RuntimeException("AVIÓN " + plane.getAirplaneId() + " NO COMPLETÓ SU CICLO.");
                }
            }

            // CSV Final
            try {
                ReportManager.generateCSV(airplaneThreads, "CONCURRENT", numPistas, numPuertas, numOperarios);
            } catch (CSVWriteException e) {
                System.err.println(e.getMessage());
            }
            
            // Parar operarios
            for (Operator op : operatorThreads) op.interrupt();

            // Tiempo total de ejecución
            long tiempoFin = System.currentTimeMillis();
            long tiempoTotal = tiempoFin - tiempoInicio;
            Logger.log("");
            Logger.log("════════════════════════════════════════════════════════════");
            Logger.log(String.format("TIEMPO TOTAL DE EJECUCIÓN: %d ms (%.2f segundos)", tiempoTotal, tiempoTotal / 1000.0));
            Logger.log(String.format("Aviones gestionados: %d | Pistas: %d | Puertas: %d | Operarios: %d", numAviones, numPistas, numPuertas, numOperarios));
            Logger.log("════════════════════════════════════════════════════════════");
            Logger.log("");
            Logger.log("=== FIN DE LA SIMULACIÓN CONCURRENTE ===");

        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO CONCURRENTE: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Logger.close();
        }
    }
}
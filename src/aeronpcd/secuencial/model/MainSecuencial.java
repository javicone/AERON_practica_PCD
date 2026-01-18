package aeronpcd.secuencial.model;

import aeronpcd.concurrente.exceptions.CSVWriteException;
import aeronpcd.concurrente.exceptions.LogWriteException;
import aeronpcd.secuencial.util.Logger;
import aeronpcd.secuencial.util.ReportManager;
import aeronpcd.secuencial.util.Window;
import java.util.ArrayList;
import java.util.List;

public class MainSecuencial {

    // Cambiamos 'main' por 'runSimulation' y aceptamos parámetros
    public static void runSimulation(int numAviones, int numPistas, int numPuertas) {
        int numOperarios = 1; // En secuencial siempre es 1 (la propia torre)
        long tiempoInicio = System.currentTimeMillis();

        try {
            // Inicialización con manejo de excepciones como en concurrente
            try {
                Logger.setup("SEQUENTIAL", numAviones, numPistas, numPuertas, numOperarios);
            } catch (LogWriteException e) {
                System.err.println(e.getMessage());
                return;
            }
            Logger.log("=== INICIO DE SIMULACIÓN SECUENCIAL ===");
            
            Window window = new Window();
            ControlTower tower = new ControlTower(window);

            List<Airplane> airplanes = new ArrayList<>();
            for (int i = 1; i <= numAviones; i++) {
                String id = String.format("IBE-%03d", i);
                airplanes.add(new Airplane(id, tower));
            }

            tower.registerAirplanes(airplanes);

            // EJECUCIÓN SECUENCIAL
            for (Airplane plane : airplanes) {
                plane.runSequentialCycle();
                sleep(500); 
            }
            
            // CSV Final - Llamada estática igual que en concurrente
            try {
                ReportManager.generateCSV(airplanes, "SEQUENTIAL", numPistas, numPuertas, numOperarios);
            } catch (CSVWriteException e) {
                System.err.println(e.getMessage());
            }
            
            // Tiempo total de ejecución
            long tiempoFin = System.currentTimeMillis();
            long tiempoTotal = tiempoFin - tiempoInicio;
            Logger.log("");
            Logger.log("════════════════════════════════════════════════════════════");
            Logger.log(String.format("TIEMPO TOTAL DE EJECUCIÓN: %d ms (%.2f segundos)", tiempoTotal, tiempoTotal / 1000.0));
            Logger.log(String.format("Aviones gestionados: %d | Pistas: %d | Puertas: %d | Operarios: %d", numAviones, numPistas, numPuertas, numOperarios));
            Logger.log("════════════════════════════════════════════════════════════");
            Logger.log("");
            Logger.log("=== FIN DE LA SIMULACIÓN SECUENCIAL ===");

        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO SECUENCIAL: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Logger.close();
        }
    }

    private static void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}
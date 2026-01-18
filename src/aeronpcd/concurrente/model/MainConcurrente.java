package aeronpcd.concurrente.model;

import aeronpcd.concurrente.util.Logger;
import aeronpcd.concurrente.util.ReportManager;
import aeronpcd.concurrente.util.Window;
import java.util.ArrayList;
import java.util.List;

public class MainConcurrente {

    public static void main(String[] args) {
        // --- CONFIGURACIÓN DE RECURSOS [cite: 16-18, 108] ---
        int numAviones = 20;
        int numPistas = 3;
        int numPuertas = 5;
        int numOperarios = 5; // Ampliado de 1 a 5 según Práctica 3 

        List<Airplane> airplaneThreads = new ArrayList<>();
        List<Operator> operatorThreads = new ArrayList<>();

        try {
            // 1. Inicializar Logger en modo CONCURRENTE [cite: 94-96]
            Logger.setup("CONCURRENT", numAviones, numPistas, numPuertas, numOperarios);
            Logger.log("=== INICIO DE SIMULACIÓN CONCURRENTE ===");
            
            ReportManager rm = new ReportManager();
            Window window = new Window(); // [cite: 50]

            // 2. Crear la Torre de Control (Monitor compartido)
            ControlTower tower = new ControlTower(window,numPistas, numPuertas);

            // 3. Crear y Lanzar los 5 Operarios de la Torre 
            for (int i = 1; i <= numOperarios; i++) {
                Operator op = new Operator(i, tower);
                operatorThreads.add(op);
                op.start(); // El operario empieza a escuchar peticiones [cite: 111]
            }

            // 4. Crear los 20 Aviones [cite: 18, 29]
            for (int i = 1; i <= numAviones; i++) {
                String id = String.format("IBE-%03d", i);
                Airplane plane = new Airplane(id, tower);
                airplaneThreads.add(plane);
            }

            // Registrar aviones para el Panel de Vuelos [cite: 53]
            tower.registerAirplanes(airplaneThreads);

            // 5. LANZAR TODOS LOS HILOS DE AVIONES [cite: 38]
            // A diferencia del secuencial, todos se inician casi a la vez
            for (Airplane plane : airplaneThreads) {
                plane.start(); 
            }

            // 6. ESPERAR A QUE TODOS LOS AVIONES TERMINEN 
            // El simulador finaliza cuando todos completan su ciclo [cite: 62]
            for (Airplane plane : airplaneThreads) {
                try {
                    plane.join(); // Bloquea el main hasta que este hilo muera
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            for (Airplane plane : airplaneThreads) {
                if (plane.getAirplaneState() != AirplaneState.DEPARTED) {

                    throw new RuntimeException("AVIÓN " + plane.getAirplaneId() + " NO COMPLETÓ SU CICLO CORRECTAMENTE.");
                }
            }

            // 7. FINALIZACIÓN Y REPORTES [cite: 64]
            // Una vez que los aviones terminan, generamos el CSV [cite: 64]
            rm.generateCSV(airplaneThreads);
            
            // Detener hilos de operarios (ya no hay más peticiones)
            for (Operator op : operatorThreads) {
                op.interrupt();
            }

            Logger.log("=== TODOS LOS AVIONES HAN COMPLETADO SU CICLO ===");
            Logger.log("=== FIN DE LA SIMULACIÓN CONCURRENTE ===");

        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO EN LA SIMULACIÓN: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Logger.close();
        }
    }
}
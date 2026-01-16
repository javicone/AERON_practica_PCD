package aeronpcd.secuencial.model;

import aeronpcd.secuencial.*;
import aeronpcd.secuencial.util.AirportState;
import aeronpcd.secuencial.util.Logger;
import aeronpcd.secuencial.util.ReportManager;
import aeronpcd.secuencial.util.Window;

import java.util.ArrayList;
import java.util.List;

public class main {

    public static void main(String[] args) {
        // --- CONFIGURACIÓN INICIAL [cite: 16-18] ---
        int numAviones = 20;
        int numPistas = 3;
        int numPuertas = 5;
        int numOperarios = 1; // En secuencial, la torre actúa como único operario

        try {
            // 1. Inicializar Logger (Requisito Práctica 1 y 6)
            Logger.setup("SEQUENTIAL", numAviones, numPistas, numPuertas, numOperarios);
            Logger.log("=== INICIO DE SIMULACIÓN SECUENCIAL ===");
            ReportManager rm = new ReportManager();
            // 2. Crear Ventana
            Window window = new Window();

            // 3. Crear Torre de Control y conectarla a la ventana
            ControlTower tower = new ControlTower(window);

            // 4. Crear los Aviones
            List<Airplane> airplanes = new ArrayList<>();
            for (int i = 1; i <= numAviones; i++) {
                // Generamos IDs tipo "IBE-001", "IBE-002"...
                String id = String.format("IBE-%03d", i);
                airplanes.add(new Airplane(id,tower));
            }

            // Registrar aviones en la torre para que aparezcan en el Panel de Vuelos
            tower.registerAirplanes(airplanes);

            // 5. EJECUCIÓN DEL CICLO DE VIDA (MOTOR SECUENCIAL) [cite: 37]
            // "Los aviones operan uno tras otro."
            for (Airplane plane : airplanes) {
                plane.runSequentialCycle();
                
                // Pequeña pausa entre aviones para apreciar visualmente el cambio
                sleep(500); 
            }
            rm.generateCSV(airplanes);
            Logger.log("=== TODOS LOS AVIONES HAN COMPLETADO SU CICLO ===");
            Logger.log("=== FIN DE LA SIMULACIÓN ===");

        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cerrar el fichero de log correctamente
            Logger.close();
        }
    }


    /**
     * Método auxiliar para pausar la ejecución y poder ver la simulación.
     */
    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // Ignorar en modo secuencial
        }
    }
}
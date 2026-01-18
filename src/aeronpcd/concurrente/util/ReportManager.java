package aeronpcd.concurrente.util;

import aeronpcd.concurrente.model.Airplane;
import aeronpcd.concurrente.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReportManager {

    private static final String FILE_NAME = "resumen_simulacion.csv";

    public static void generateCSV(List<Airplane> airplanes) {
        // Creamos una copia para no alterar la lista original si fuera necesario
        List<Airplane> sortedPlanes = new ArrayList<>(airplanes);

        // Ordenamos los aviones por duración (de menor a mayor tiempo)
        sortedPlanes.sort(Comparator.comparingLong(Airplane::getDuracionEnMs));

        try (PrintWriter writer = new PrintWriter(new File(FILE_NAME))) {
            // Cabecera del CSV
            writer.println("Avion,Tiempo total(ms)");

            for (int i = 0; i < sortedPlanes.size(); i++) {
                Airplane p = sortedPlanes.get(i);
                // El ranking es el índice + 1

                
                writer.println(String.format("%s,%d", 
                    p.getAirplaneId(), 
                    p.getDuracionEnMs()
                    ));
            }
            
            System.out.println("Reporte generado con éxito: " + FILE_NAME);

        } catch (Exception e) {
        	e.printStackTrace();
            // REQUISITO PRÁCTICA 6: Mensaje de error específico
            System.err.println("Error al escribir el resumen de la simulación. " +
                               "No se ha podido guardar en el fichero " + FILE_NAME);
        }
    }
}
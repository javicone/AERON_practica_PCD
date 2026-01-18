package aeronpcd.secuencial.util;

import aeronpcd.concurrente.exceptions.CSVWriteException;
import aeronpcd.secuencial.model.Airplane;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Gestor de reportes y generación de archivos CSV con los resultados de la simulación.
 * Proporciona funcionalidades para escribir datos de aviones en formato CSV.
 */
public class ReportManager {

    /**
     * Genera un archivo CSV con el resumen de la simulación.
     * El archivo contiene los tiempos totales de cada avión, ordenados de menor a mayor duración.
     * Utiliza la misma nomenclatura y carpeta de destino que el sistema de logs.
     * 
     * Formato CSV:
     * Avion,Tiempo total(ms)
     * IBE-001,5432
     * IBE-002,6789
     * ...
     * 
     * @param airplanes Lista de aviones con sus datos de tiempo de ejecución.
     * @param mode Modo de ejecución: "CONCURRENT" o "SEQUENTIAL" (determina carpeta).
     * @param nPistas Número de pistas del aeropuerto.
     * @param nPuertas Número de puertas de embarque.
     * @param nOperarios Número de operarios en la simulación.
     * @throws CSVWriteException Si no se puede escribir en el archivo CSV.
     */
    public static void generateCSV(List<Airplane> airplanes, String mode, int nPistas, int nPuertas, int nOperarios) throws CSVWriteException {
        // Creamos una copia para no alterar la lista original si fuera necesario
        List<Airplane> sortedPlanes = new ArrayList<>(airplanes);

        // Ordenamos los aviones por duración (de menor a mayor tiempo)
        sortedPlanes.sort(Comparator.comparingLong(Airplane::getDuracionEnMs));

        // Misma estructura de carpetas que Logger
        String folderPath = "logs/" + (mode.equalsIgnoreCase("SEQUENTIAL") ? "secuencial/" : "concurrent/");
        
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Mismo formato de nombre que Logger
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("aeron-%s-%dAV-%dPIS-%dPUE-%dOPE-%s",
                mode.toUpperCase(), airplanes.size(), nPistas, nPuertas, nOperarios, timeStamp);
        
        String fullPath = folderPath + fileName + ".csv";

        try (PrintWriter writer = new PrintWriter(new File(fullPath))) {
            // Cabecera del CSV
            writer.println("Avion,Tiempo total(ms)");

            for (int i = 0; i < sortedPlanes.size(); i++) {
                Airplane p = sortedPlanes.get(i);
                writer.println(String.format("%s,%d", 
                    p.getId(), 
                    p.getDuracionEnMs()));
            }
            
            System.out.println("Reporte generado con éxito: " + fullPath);

        } catch (FileNotFoundException e) {
            // REQUISITO PRÁCTICA 6: Lanzar excepción personalizada
            throw new CSVWriteException(fileName, e);
        }
    }
}
package secuencial;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import secuencial.Airplane; // Asumo que tu clase Avión está aquí

public class Logger {

    private PrintWriter writer;
    private String logFilePath;

    /**
     * Constructor que genera automáticamente el nombre del archivo según el formato obligatorio:
     * aeron-MODE-N1AV-N2PIS-N3PUE[-N4OPE-]-TIMESTAMP
     */
    public Logger(String mode, int numAviones, int numPistas, int numPuertas, int numOperarios) {
    	try {
            // 1. Crear directorios si no existen
            String directorio = "logs/" + mode.toLowerCase() + "/"; // logs/secuencial/ o logs/concurrent/
            File dir = new File(directorio);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 2. Generar Timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            // 3. Construir nombre del archivo 
            // Formato: aeron-MODE-N1AV-N2PIS-N3PUE[-N4OPE-]-TIMESTAMP
            String fileName = String.format("aeron-%s-%dAV-%dPIS-%dPUE-%dOPE-%s.log",
                    mode, numAviones, numPistas, numPuertas, numOperarios, timestamp);

            this.logFilePath = directorio + fileName;

            // 4. Inicializar el escritor (Append = true no es estrictamente necesario si creamos uno nuevo cada vez)
            this.writer = new PrintWriter(new BufferedWriter(new FileWriter(logFilePath)));
            
            System.out.println("Logger iniciado en: " + logFilePath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error crítico: No se pudo crear el archivo de log.");
        }
    }

    /**
     * Método principal para escribir una línea en el log.
     * @param String message.
     * Es SYNCHRONIZED para que cuando pases a modo concurrente, los hilos no escriban unos encima de otros.
     */
    public synchronized void log(String message) {
        if (writer != null) {
            writer.println(message);
            writer.flush(); // Asegura que se escribe en el disco inmediatamente
            // Opcional: Imprimir también por consola para ver qué pasa mientras ejecutas
            // System.out.println(message); 
        }
    }

    /**
     * Escribe una cabecera o título de sección (ej: "Torre de control", "Aviones")
     */
    public synchronized void logHeader(String header) {
        log(""); // Espacio en blanco antes
        log(header);
    }

    /**
     * Cierra el flujo de escritura al terminar la simulación.
     */
    public void close() {
        if (writer != null) {
            writer.close();
        }
    }

    /**
     * Genera el archivo CSV de estadísticas al final de la simulación.
     * Requisito: 
     * @param List<Airplane> airplanes
     */
    public void generateCsvStats(List<Airplane> airplanes) {
        String csvPath = logFilePath.replace(".log", ".csv");
        try (PrintWriter csvWriter = new PrintWriter(new BufferedWriter(new FileWriter(csvPath)))) {
            
            // Cabecera del CSV
            csvWriter.println("Avión,Tiempo total (ms),Observaciones");

            // Datos de cada avión
            for (Airplane p : airplanes) {
                // Asumo que tu clase Airplane tiene métodos getId() y getTotalTime()
                long time = p.getTotalCycleTime(); 
                String obs = ""; // Puedes añadir lógica para "1º", "2º" si la tienes
                
                csvWriter.printf("%s,%d,%s%n", p.getId(), time, obs);
            }
            
            System.out.println("Estadísticas CSV generadas en: " + csvPath);

        } catch (IOException e) {
            System.err.println("Error al generar el CSV.");
            e.printStackTrace();
        }
    }
}
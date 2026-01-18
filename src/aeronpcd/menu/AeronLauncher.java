package aeronpcd.menu;

import aeronpcd.concurrente.model.MainConcurrente;
import aeronpcd.secuencial.model.MainSecuencial;
import java.util.Scanner;

/**
 * Punto de entrada principal del Simulador del Aeropuerto AERON.
 * 
 * Proporciona un menú interactivo para seleccionar el modo de ejecución
 * (secuencial o concurrente) y configurar los parámetros de la simulación
 * (número de aviones, pistas, puertas y operarios).
 * 
 * El modo secuencial ejecuta las operaciones del aeropuerto de forma lineal,
 * mientras que el modo concurrente utiliza múltiples threads (aviones y operarios)
 * para simular operaciones simultáneas.
 */
public class AeronLauncher {

    /**
     * Método principal de la aplicación.
     * Muestra un menú de opciones y permite al usuario seleccionar el modo de ejecución
     * (secuencial o concurrente) e introducir los parámetros de configuración del aeropuerto.
     * 
     * Parámetros por defecto:
     * - Aviones: 20
     * - Pistas: 3
     * - Puertas: 5
     * - Operarios: 5 (solo en modo concurrente)
     * 
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("============================================");
        System.out.println("   AEROPUERTO AERON - SISTEMA DE GESTIÓN    ");
        System.out.println("============================================");
        System.out.println("Seleccione el modo de ejecución:");
        System.out.println("1. Modo SECUENCIAL (Simulación lineal)");
        System.out.println("2. Modo CONCURRENTE (Simulación con múltiples threads)");
        System.out.println("0. Salir");
        System.out.print(">> Opción: ");

        int opcion = leerEntero(scanner);

        if (opcion == 0) {
            System.out.println("Saliendo...");
            return;
        }

        // --- CONFIGURACIÓN DE PARÁMETROS ---
        System.out.println("\n--- CONFIGURACIÓN DEL AEROPUERTO ---");
        
        System.out.print("Número de AVIONES (Default 20): ");
        int aviones = leerEntero(scanner);
        if (aviones <= 0) aviones = 20;

        System.out.print("Número de PISTAS (Default 3): ");
        int pistas = leerEntero(scanner);
        if (pistas <= 0) pistas = 3;

        System.out.print("Número de PUERTAS (Default 5): ");
        int puertas = leerEntero(scanner);
        if (puertas <= 0) puertas = 5;

        // --- LANZAMIENTO ---
        if (opcion == 1) {
            System.out.println("\nIniciando MODO SECUENCIAL...");
            // En modo secuencial, solo se usa un operario interno sin threads
            MainSecuencial.runSimulation(aviones, pistas, puertas);
            
        } else if (opcion == 2) {
            System.out.print("Número de OPERARIOS (Default 5): ");
            int operarios = leerEntero(scanner);
            if (operarios <= 0) operarios = 5;

            System.out.println("\nIniciando MODO CONCURRENTE...");
            MainConcurrente.runSimulation(aviones, pistas, puertas, operarios);
            
        } else {
            System.err.println("Opción no válida.");
        }
        
        scanner.close();
    }

    /**
     * Método auxiliar para leer un número entero de entrada estándar.
     * 
     * Captura y maneja excepciones si el usuario introduce caracteres no numéricos,
     * devolviendo -1 para indicar un valor inválido que será rechazado por el validador.
     * 
     * @param sc Scanner vinculado a la entrada estándar (System.in).
     * @return Número entero leído, o -1 si la entrada no es un número válido.
     */
    private static int leerEntero(Scanner sc) {
        try {
            String input = sc.next();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1; // Valor inválido
        }
    }
}
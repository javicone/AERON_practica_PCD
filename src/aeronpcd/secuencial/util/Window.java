package aeronpcd.secuencial.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * Ventana principal de la interfaz gráfica del Simulador AERON en modo secuencial.
 * 
 * Proporciona tres paneles simultáneos para visualizar:
 * - Panel 1: Eventos de aviones (log de actividad)
 * - Panel 2: Estado de la Torre (recursos, pistas, puertas, cola de peticiones)
 * - Panel 3: Panel de Vuelos (estados de los aviones)
 * 
 * Las actualizaciones se realizan de forma thread-safe mediante SwingUtilities.invokeLater().
 */
public class Window extends JFrame {

    /**
     * Área de texto para mostrar el registro de eventos de aviones.
     */
    private JTextArea airplaneEventsArea;
    
    /**
     * Área de texto para mostrar el estado técnico de la torre y recursos.
     */
    private JTextArea towerControlArea;
    
    /**
     * Área de texto para mostrar el panel de vuelos con estados de aviones.
     */
    private JTextArea flightPanelArea;

    /**
     * Constructor de la ventana principal del simulador AERON.
     * Inicializa los tres paneles (eventos, torre, panel de vuelos) con estilo terminal
     * y configura el layout de 1 fila x 3 columnas.
     * 
     * La ventana se posiciona en el centro de la pantalla con tamaño 1200x600.
     */
    public Window() {
        super("Simulador Aeropuerto AERON - Modo Secuencial");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1200, 600); // Tamaño amplio para ver las 3 columnas
        this.setLocationRelativeTo(null); // Centrar en pantalla
        
        // Usamos un GridLayout de 1 fila y 3 columnas para dividir la pantalla
        this.setLayout(new GridLayout(1, 3));

        // --- 1. Panel de Eventos de Aviones ---
        airplaneEventsArea = createTextArea("Eventos de Aviones");
        this.add(new JScrollPane(airplaneEventsArea));

        // --- 2. Panel de Torre de Control (Pistas/Puertas/Cola) ---
        towerControlArea = createTextArea("Torre de Control & Recursos");
        this.add(new JScrollPane(towerControlArea));

        // --- 3. Panel de Vuelos (Estados) ---
        flightPanelArea = createTextArea("Panel de Vuelos (Flight Board)");
        this.add(new JScrollPane(flightPanelArea));

        this.setVisible(true);
    }

    /**
     * Crea un área de texto configurada con estilo terminal (negro y verde).
     * Utiliza fuente monoespaciada para que los gráficos ASCII se alineen correctamente.
     * 
     * @param title Título a mostrar en el borde del área de texto.
     * @return JTextArea configurado con el estilo terminal.
     */
    private JTextArea createTextArea(String title) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setBackground(Color.BLACK);
        area.setForeground(Color.GREEN); // Estilo "terminal" clásico
        area.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Vital para AirportState
        
        // Borde con título
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleColor(Color.WHITE);
        area.setBorder(border);
        
        return area;
    }

    // ================= MÉTODOS DE ACTUALIZACIÓN THREAD-SAFE =================

    /**
     * Añade un evento de avión al panel de eventos.
     * Utiliza SwingUtilities.invokeLater() para garantizar thread-safety.
     * Hace scroll automático hacia abajo para mostrar los eventos más recientes.
     * 
     * @param event Descripción del evento a añadir al log.
     */
    public void addAirplaneEvent(String event) {
        SwingUtilities.invokeLater(() -> {
            airplaneEventsArea.append(event + "\n");
            airplaneEventsArea.setCaretPosition(airplaneEventsArea.getDocument().getLength());
        });
    }

    /**
     * Actualiza el panel de estado de la Torre de Control.
     * Reemplaza completamente el contenido con el nuevo estado (recursos, cola, pistas, puertas).
     * Utiliza SwingUtilities.invokeLater() para garantizar thread-safety.
     * 
     * @param status Texto formateado con gráficos ASCII del estado actual de la torre.
     */
    public void updateTowerArea(String status) {
        SwingUtilities.invokeLater(() -> {
            towerControlArea.setText(status);
        });
    }

    /**
     * Actualiza el Panel de Vuelos con el estado de todos los aviones.
     * Reemplaza completamente el contenido con el listado de estados.
     * Utiliza SwingUtilities.invokeLater() para garantizar thread-safety.
     * 
     * @param panelData Listado completo de estados de vuelos formateado para mostrar.
     */
    public void updateFlightPanel(String panelData) {
        SwingUtilities.invokeLater(() -> {
            flightPanelArea.setText(panelData);
        });
    }
}
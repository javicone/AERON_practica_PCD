package aeronpcd.concurrente.util;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class Window extends JFrame {

    // Componentes de texto para las 3 secciones requeridas
    private JTextArea airplaneEventsArea; // [cite: 51]
    private JTextArea towerControlArea;   // [cite: 52]
    private JTextArea flightPanelArea;    // [cite: 53]

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
     * Método auxiliar para configurar las áreas de texto con estilo común.
     * Importante: Usa fuente MONOSPACED para que los dibujos ASCII se alineen bien.
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

    // ================= MÉTODOS DE ACTUALIZACIÓN =================

    /**
     * Añade una línea al log de eventos de aviones (Columna Izquierda).
     * Hace scroll automático hacia abajo.
     */
    public void addAirplaneEvent(String event) {
        SwingUtilities.invokeLater(() -> {
            airplaneEventsArea.append(event + "\n");
            airplaneEventsArea.setCaretPosition(airplaneEventsArea.getDocument().getLength());
        });
    }

    /**
     * Reemplaza todo el contenido del área de la Torre (Columna Central).
     * Se usa para refrescar el gráfico ASCII de las pistas y colas.
     */
    public void updateTowerArea(String status) {
        SwingUtilities.invokeLater(() -> {
            towerControlArea.setText(status);
        });
    }

    /**
     * Actualiza el Panel de Vuelos (Columna Derecha).
     * Recibe el listado completo de estados.
     */
    public void updateFlightPanel(String panelData) {
        SwingUtilities.invokeLater(() -> {
            flightPanelArea.setText(panelData);
        });
    }
}
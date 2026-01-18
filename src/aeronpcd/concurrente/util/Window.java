package aeronpcd.concurrente.util;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class Window extends JFrame {

    private JTextArea airplaneEventsArea; // Columna 1
    private JTextArea towerControlArea;   // Columna 2
    private JTextArea flightPanelArea;    // Columna 3 (Panel de Vuelos)

    // Color Ámbar típico de pantallas de información de vuelos (FIDS)
    private static final Color AIRPORT_AMBER = new Color(255, 191, 0);

    public Window() {
        super("Simulador Aeropuerto AERON - Panel de Control Híbrido");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1366, 768); // Un poco más ancho para que luzca el panel
        this.setLocationRelativeTo(null);

        // Estilo nativo del SO para la ventana
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }

        // Layout de 3 columnas
        this.setLayout(new GridLayout(1, 3, 10, 0));
        // Fondo general gris suave
        this.getContentPane().setBackground(new Color(240, 240, 240));

        // --- 1. Panel de Eventos (Estilo Profesional Claro) ---
        airplaneEventsArea = createProfessionalTextArea("Registro de Eventos");
        this.add(createStyledScrollPane(airplaneEventsArea));

        // --- 2. Panel de Torre (Estilo Profesional Claro) ---
        towerControlArea = createProfessionalTextArea("Estado Técnica de Torre");
        this.add(createStyledScrollPane(towerControlArea));

        // --- 3. Panel de Vuelos (ESTILO REALISTA DE AEROPUERTO) ---
        // ¡Aquí está la magia! Usamos el nuevo estilo personalizado.
        flightPanelArea = createAirportPanelTextArea("PANEL DE VUELOS (SALIDAS / LLEGADAS)");
        // Usamos un scrollpane especial que también sea negro
        this.add(createAirportScrollPane(flightPanelArea));

        this.setVisible(true);
    }

    /**
     * ESTILO 1: Limpio y profesional (Blanco sobre Gris Oscuro) para logs técnicos.
     */
    private JTextArea createProfessionalTextArea(String title) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setBackground(Color.WHITE);
        area.setForeground(new Color(40, 40, 40));
        area.setFont(new Font("Monospaced", Font.BOLD, 12));
        area.setMargin(new Insets(5, 5, 5, 5));

        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), title);
        border.setTitleFont(new Font("SansSerif", Font.BOLD, 14));
        border.setTitleColor(new Color(0, 51, 102)); // Azul marino
        area.setBorder(border);

        return area;
    }

    /**
     * ESTILO 2: Imitación realista de pantalla digital de aeropuerto (Ámbar sobre Negro).
     */
    private JTextArea createAirportPanelTextArea(String title) {
        JTextArea area = new JTextArea();
        area.setEditable(false);

        // 1. FONDO NEGRO PURO
        area.setBackground(Color.BLACK);
        // 2. TEXTO ÁMBAR BRILLANTE
        area.setForeground(AIRPORT_AMBER);
        // Cambiamos también el color del cursor para que no desentone
        area.setCaretColor(AIRPORT_AMBER);

        // 3. FUENTE: Monospaced más grande y negrita para efecto "pantalla LED"
        area.setFont(new Font("Monospaced", Font.BOLD, 15));

        // Margen interno generoso
        area.setMargin(new Insets(15, 15, 15, 15));

        // 4. BORDE: Línea sólida del mismo color Ámbar
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(AIRPORT_AMBER, 2), // Borde de 2px
                title);
        // Título centrado, grande y en color Ámbar
        border.setTitleFont(new Font("Monospaced", Font.BOLD, 18));
        border.setTitleColor(AIRPORT_AMBER);
        border.setTitleJustification(TitledBorder.CENTER);

        area.setBorder(border);

        return area;
    }

    // ScrollPane normal para estilo claro
    private JScrollPane createStyledScrollPane(JComponent view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return scroll;
    }

    // ScrollPane especial NEGRO para el panel de aeropuerto
    private JScrollPane createAirportScrollPane(JComponent view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        scroll.getViewport().setBackground(Color.BLACK); // El fondo detrás del texto
        scroll.setBackground(Color.BLACK); // El fondo de las barras de desplazamiento
        // Opcional: personalizar las barras de scroll (más complejo en Swing puro)
        return scroll;
    }

    // ================= MÉTODOS DE ACTUALIZACIÓN =================

    public void addAirplaneEvent(String event) {
        SwingUtilities.invokeLater(() -> {
            airplaneEventsArea.append(event + "\n");
            airplaneEventsArea.setCaretPosition(airplaneEventsArea.getDocument().getLength());
        });
    }

    public void updateTowerArea(String status) {
        SwingUtilities.invokeLater(() -> {
            towerControlArea.setText(status);
        });
    }

    public void updateFlightPanel(String panelData) {
        SwingUtilities.invokeLater(() -> {
            flightPanelArea.setText(panelData);
            // IMPORTANTE EN PANELES DE VUELO: Scroll al principio para ver la cabecera siempre
            flightPanelArea.setCaretPosition(0);
        });
    }
}
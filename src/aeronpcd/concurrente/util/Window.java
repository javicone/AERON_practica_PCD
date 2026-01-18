package aeronpcd.concurrente.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * Ventana principal de la interfaz gráfica del simulador del Aeropuerto AERON.
 * 
 * Implementa una interfaz de usuario con tres paneles simultáneos:
 * - Panel 1: Registro de Eventos (log de actividad de aviones)
 * - Panel 2: Estado Técnico de Torre (información del control de torre)
 * - Panel 3: Panel de Vuelos (estilo FIDS de aeropuerto con ámbar sobre negro)
 * 
 * Las actualizaciones de cada panel se realizan de forma thread-safe mediante
 * SwingUtilities.invokeLater() para garantizar acceso al Event Dispatch Thread.
 */
public class Window extends JFrame {

    /**
     * Área de texto para mostrar el registro de eventos de aviones.
     * Se actualiza continuamente con eventos de aviones (landing, boarding, etc).
     */
    private JTextArea airplaneEventsArea;
    
    /**
     * Área de texto para mostrar el estado técnico de la torre de control.
     * Se actualiza con información de operarios, recursos disponibles, etc.
     */
    private JTextArea towerControlArea;
    
    /**
     * Área de texto para mostrar el Panel de Vuelos estilo FIDS de aeropuerto.
     * Utiliza colores ámbar sobre negro para simular una pantalla de información de vuelos real.
     */
    private JTextArea flightPanelArea;

    /**
     * Color ámbar típico de pantallas de información de vuelos (FIDS) en aeropuertos.
     * RGB: (255, 191, 0) - Amarillo/Ámbar brillante.
     */
    private static final Color AIRPORT_AMBER = new Color(255, 191, 0);

    /**
     * Constructor de la ventana principal del simulador AERON.
     * Inicializa los tres paneles (eventos, torre, panel de vuelos) con estilos diferentes
     * y configura el layout de la ventana en GridLayout 1x3.
     * 
     * La ventana se posiciona en el centro de la pantalla y usa el look-and-feel
     * nativo del sistema operativo para mejor integración.
     */
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

        // --- 3. Panel de Vuelos (Estilo realista de aeropuerto: ámbar sobre negro) ---
        flightPanelArea = createAirportPanelTextArea("PANEL DE VUELOS (SALIDAS / LLEGADAS)");
        this.add(createAirportScrollPane(flightPanelArea));

        this.setVisible(true);
    }

    /**
     * Crea un área de texto con estilo profesional y limpio.
     * Fondo blanco, texto gris oscuro, fuente monoespaciada en 12pt.
     * Utilizado para paneles de eventos y estado de torre.
     * 
     * @param title Título a mostrar en el borde del área de texto.
     * @return JTextArea configurado con el estilo profesional.
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
     * Crea un área de texto con estilo realista de Panel de Vuelos (FIDS).
     * Fondo negro, texto ámbar brillante, fuente monoespaciada en 15pt para efecto LED.
     * Imita la apariencia de pantallas de información de vuelos en aeropuertos reales.
     * 
     * @param title Título a mostrar en el borde del panel en color ámbar.
     * @return JTextArea configurado con el estilo de aeropuerto (FIDS).
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

    /**
     * Crea un JScrollPane estándar para áreas de texto con estilo profesional.
     * Aplicar bordes vacíos de 5px de margen interno.
     * 
     * @param view Componente a incluir en el scroll pane.
     * @return JScrollPane configurado con bordes y márgenes.
     */
    private JScrollPane createStyledScrollPane(JComponent view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return scroll;
    }

    /**
     * Crea un JScrollPane especial para el panel de vuelos con tema oscuro.
     * Fondo negro tanto en el viewport como en las barras de desplazamiento
     * para mantener el efecto de pantalla FIDS de aeropuerto.
     * 
     * @param view Componente a incluir en el scroll pane (área de texto ámbar).
     * @return JScrollPane configurado con tema negro oscuro.
     */
    private JScrollPane createAirportScrollPane(JComponent view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        scroll.getViewport().setBackground(Color.BLACK); // El fondo detrás del texto
        scroll.setBackground(Color.BLACK); // El fondo de las barras de desplazamiento
        // Opcional: personalizar las barras de scroll (más complejo en Swing puro)
        return scroll;
    }

    // ================= MÉTODOS DE ACTUALIZACIÓN THREAD-SAFE =================

    /**
     * Añade un evento de avión al panel de eventos.
     * Utiliza SwingUtilities.invokeLater() para garantizar thread-safety
     * en actualizaciones desde threads de aviones.
     * 
     * @param event Descripción del evento a añadir.
     */
    public void addAirplaneEvent(String event) {
        SwingUtilities.invokeLater(() -> {
            airplaneEventsArea.append(event + "\n");
            airplaneEventsArea.setCaretPosition(airplaneEventsArea.getDocument().getLength());
        });
    }

    /**
     * Actualiza el panel de estado técnico de la Torre de Control.
     * Reemplaza completamente el contenido con el nuevo estado.
     * Utiliza SwingUtilities.invokeLater() para garantizar thread-safety.
     * 
     * @param status Texto con el estado actual de la torre y operarios.
     */
    public void updateTowerArea(String status) {
        SwingUtilities.invokeLater(() -> {
            towerControlArea.setText(status);
        });
    }


    /**
     * Actualiza el Panel de Vuelos (estilo FIDS) con nuevos datos.
     * Reemplaza completamente el contenido con los datos del panel.
     * Posiciona el cursor al inicio para asegurar que la cabecera siempre sea visible.
     * Utiliza SwingUtilities.invokeLater() para garantizar thread-safety.
     * 
     * @param panelData Texto formateado con la información de vuelos (salidas/llegadas).
     */
    public void updateFlightPanel(String panelData) {
        SwingUtilities.invokeLater(() -> {
            flightPanelArea.setText(panelData);
            // Posiciona el cursor al inicio para que la cabecera sea siempre visible
            flightPanelArea.setCaretPosition(0);
        });
    }
}
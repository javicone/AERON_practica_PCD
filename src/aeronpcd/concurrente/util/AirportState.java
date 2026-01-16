package aeronpcd.concurrente.util;

import java.util.ArrayList;
import java.util.List;

import aeronpcd.concurrente.model.Gate;
import aeronpcd.concurrente.model.Request;
import aeronpcd.concurrente.model.Runway;


public class AirportState {


	/**
     * Muestra el contenido de la cola de peticiones (FIFO) con una separación
     * visible arriba y abajo para que se vea claramente la cola.
     */
    public static String showRequestQueue(List<Request> requestQueue) {
        int n = requestQueue.size();
        List<String> lines = new ArrayList<>();
        String title = "Cola de peticiones (" + n + ")";
        if (n == 0) {
            String empty = title + ": vacía";
            // sin márgenes laterales, el ancho es justo el de la línea
            int innerWidth = empty.length();
            String border = repeat('═', innerWidth);
            StringBuilder sbEmpty = new StringBuilder();
            sbEmpty.append("╔").append(border).append("╗\n");
            sbEmpty.append(empty).append("\n");
            sbEmpty.append("╚").append(border).append("╝");
            return sbEmpty.toString();
        }

        lines.add(title + ":");
        int i = 1;
        for (Request r : requestQueue) {
            String tipo;
            switch (r.getType()) {
            case LANDING:
                tipo = "Aterrizaje 🛬";
                break;
            case LANDED:
                tipo = "Aterrizado ✅";
                break;
            case BOARDED:
                tipo = "Embarcado 🧳";
                break;
            case TAKEOFF_ASSIGNED:
                tipo = "Despegue 🛫";
                break;
            case DEPARTED:
                tipo = "Despegado ✈️";
                break;
            default:
                tipo = r.getType().toString();
                break;
            }
            lines.add("  " + i++ + ") " + tipo + " — " + r.getAirplane());
        }

        // calcular ancho máximo de las líneas para la barra superior/inferior
        int max = 0;
        for (String l : lines) {
            if (l.length() > max)
                max = l.length();
        }

        String border = repeat('═', max);
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("╔").append(border).append("╗\n");
        for (String l : lines) {
            sb.append(l);
            // rellenar espacios a la derecha para que coincida con el ancho del borde
            int padding = max - l.length();
            if (padding > 0) {
                sb.append(repeat(' ', padding));
            }
            sb.append("\n");
        }
        sb.append("╚").append(border).append("╝");

        return sb.toString();
    }

    private static String repeat(char ch, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

    // metodo para ver el estado de las pistas y de las puertas
    public static String showResourcesStatus(List<Runway> runways, List<Gate> gates) {
        List<String> lines = new ArrayList<>();
        lines.add("Estado de recursos:");

        // Pistas: ids en una línea, iconos justo debajo alineados por columna
        if (runways.isEmpty()) {
            lines.add("Pistas: (vacías)");
        } else {
            lines.add("Pistas:");
            int nR = runways.size();
            String[] idsR = new String[nR];
            String[] iconsR = new String[nR];
            int[] colW = new int[nR];

            for (int i = 0; i < nR; i++) {
                Runway r = runways.get(i);
                idsR[i] = r.getId();
                iconsR[i] = r.isAvailable() ? "🟢" : "🔴";
                colW[i] = Math.max(idsR[i].length(), iconsR[i].length());
            }

            StringBuilder idLine = new StringBuilder();
            StringBuilder iconLine = new StringBuilder();
            for (int i = 0; i < nR; i++) {
                idLine.append(idsR[i]);
                iconLine.append(iconsR[i]);
                int padId = colW[i] - idsR[i].length();
                int padIcon = colW[i] - iconsR[i].length();
                if (padId > 0)
                    idLine.append(repeat(' ', padId));
                if (padIcon > 0)
                    iconLine.append(repeat(' ', padIcon));
                if (i < nR - 1) {
                    idLine.append("   "); // separación entre columnas
                    iconLine.append("   ");
                }
            }
            lines.add(idLine.toString());
            lines.add(iconLine.toString());
        }

        // Separador visual entre secciones
        lines.add("");

        // Puertas: mismo formato que pistas
        if (gates.isEmpty()) {
            lines.add("Puertas: (vacías)");
        } else {
            lines.add("Puertas:");
            int nG = gates.size();
            String[] idsG = new String[nG];
            String[] iconsG = new String[nG];
            int[] colWg = new int[nG];

            for (int i = 0; i < nG; i++) {
                Gate g = gates.get(i);
                idsG[i] = g.getId();
                iconsG[i] = g.isOccupied() ? "🔴" : "🟢";
                colWg[i] = Math.max(idsG[i].length(), iconsG[i].length());
            }

            StringBuilder idLineG = new StringBuilder();
            StringBuilder iconLineG = new StringBuilder();
            for (int i = 0; i < nG; i++) {
                idLineG.append(idsG[i]);
                iconLineG.append(iconsG[i]);
                int padId = colWg[i] - idsG[i].length();
                int padIcon = colWg[i] - iconsG[i].length();
                if (padId > 0)
                    idLineG.append(repeat(' ', padId));
                if (padIcon > 0)
                    iconLineG.append(repeat(' ', padIcon));
                if (i < nG - 1) {
                    idLineG.append("   ");
                    iconLineG.append("   ");
                }
            }
            lines.add(idLineG.toString());
            lines.add(iconLineG.toString());
        }

        // calcular ancho máximo de las líneas para la barra superior/inferior
        int max = 0;
        for (String l : lines) {
            if (l.length() > max)
                max = l.length();
        }

        String border = repeat('═', max);
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("╔").append(border).append("╗\n");
        for (String l : lines) {
            sb.append(l);
            int padding = max - l.length();
            if (padding > 0) {
                sb.append(repeat(' ', padding));
            }
            sb.append("\n");
        }
        sb.append("╚").append(border).append("╝");

        return sb.toString();
    }

}

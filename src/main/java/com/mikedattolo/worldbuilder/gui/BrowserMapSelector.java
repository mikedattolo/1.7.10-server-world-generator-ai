package com.mikedattolo.worldbuilder.gui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Window;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

final class BrowserMapSelector extends JDialog {
    private final JTextField bboxField;
    private final AtomicReference<String> latestBbox = new AtomicReference<String>();
    private HttpServer server;
    private String selectedBbox;

    private BrowserMapSelector(Window owner, String initialBbox) {
        super(owner, "Select DEM Area on Real Map", ModalityType.APPLICATION_MODAL);
        latestBbox.set(initialBbox == null ? "" : initialBbox.trim());
        bboxField = new JTextField(latestBbox.get(), 42);
        bboxField.setEditable(false);

        JLabel instructions = new JLabel("A browser map opened. Drag an area there, then click Use Selected Area here.");
        instructions.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));

        JButton use = new JButton("Use Selected Area");
        JButton offline = new JButton("Use Offline Selector");
        JButton cancel = new JButton("Cancel");
        use.addActionListener(e -> {
            selectedBbox = bboxField.getText().trim();
            if (!selectedBbox.isEmpty()) {
                dispose();
            }
        });
        offline.addActionListener(e -> {
            selectedBbox = BBoxMapSelector.choose(owner, bboxField.getText().trim());
            dispose();
        });
        cancel.addActionListener(e -> {
            selectedBbox = null;
            dispose();
        });

        JPanel buttons = new JPanel();
        buttons.add(use);
        buttons.add(offline);
        buttons.add(cancel);

        JPanel body = new JPanel(new BorderLayout(8, 8));
        body.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        body.add(new JLabel("Selected bbox:"), BorderLayout.WEST);
        body.add(bboxField, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(instructions, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setMinimumSize(new Dimension(720, 150));
        setLocationRelativeTo(owner);
    }

    static String choose(Window owner, String initialBbox) {
        if (!Desktop.isDesktopSupported()) {
            return BBoxMapSelector.choose(owner, initialBbox);
        }
        BrowserMapSelector selector = new BrowserMapSelector(owner, initialBbox);
        try {
            selector.startServer();
            Desktop.getDesktop().browse(new URI("http://127.0.0.1:" + selector.server.getAddress().getPort() + "/"));
            selector.setVisible(true);
            return selector.selectedBbox;
        } catch (Exception ex) {
            selector.stopServer();
            return BBoxMapSelector.choose(owner, initialBbox);
        } finally {
            selector.stopServer();
        }
    }

    private void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", new MapPageHandler());
        server.createContext("/select", new SelectHandler());
        server.start();
    }

    private void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private final class SelectHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getRawQuery();
            String bbox = extractBbox(query);
            if (bbox != null && !bbox.isEmpty()) {
                latestBbox.set(bbox);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        bboxField.setText(latestBbox.get());
                    }
                });
            }
            byte[] body = "OK".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        }

        private String extractBbox(String query) throws IOException {
            if (query == null) {
                return null;
            }
            String[] parts = query.split("&");
            for (String part : parts) {
                int idx = part.indexOf('=');
                if (idx > 0 && "bbox".equals(part.substring(0, idx))) {
                    return URLDecoder.decode(part.substring(idx + 1), "UTF-8");
                }
            }
            return null;
        }
    }

    private static final class MapPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] body = html().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        }

        private static String html() {
            return "<!doctype html>\n" +
                    "<html><head><meta charset='utf-8'><title>WorldBuilder Map Selector</title>\n" +
                    "<meta name='viewport' content='width=device-width, initial-scale=1'>\n" +
                    "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'>\n" +
                    "<style>html,body,#map{height:100%;margin:0} .panel{position:absolute;z-index:1000;top:12px;left:12px;background:white;padding:10px;border-radius:6px;font:14px sans-serif;box-shadow:0 2px 12px #0004}.bbox{font-family:monospace;margin-top:6px}</style>\n" +
                    "</head><body><div id='map'></div><div class='panel'>Drag on the real map to select DEM area.<div class='bbox' id='bbox'>No area selected</div></div>\n" +
                    "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>\n" +
                    "<script>\n" +
                    "const map=L.map('map').setView([39,-98],5);\n" +
                    "const base={\n" +
                    "  'OpenStreetMap':L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,attribution:'OpenStreetMap'}),\n" +
                    "  'Esri Satellite':L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',{maxZoom:19,attribution:'Esri'}),\n" +
                    "  'Carto Light':L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png',{maxZoom:19,attribution:'Carto, OpenStreetMap'})\n" +
                    "}; base['OpenStreetMap'].addTo(map); L.control.layers(base).addTo(map);\n" +
                    "let start=null,rect=null;\n" +
                    "function bboxString(a,b){const s=L.latLngBounds(a,b);return [s.getSouth(),s.getWest(),s.getNorth(),s.getEast()].map(v=>v.toFixed(6)).join(',');}\n" +
                    "map.on('mousedown',e=>{start=e.latlng;if(rect){map.removeLayer(rect);rect=null;} map.dragging.disable();});\n" +
                    "map.on('mousemove',e=>{if(!start)return; const bounds=L.latLngBounds(start,e.latlng); if(!rect){rect=L.rectangle(bounds,{color:'#1f64c8',weight:2,fillOpacity:.18}).addTo(map);}else{rect.setBounds(bounds);} document.getElementById('bbox').textContent=bboxString(start,e.latlng);});\n" +
                    "map.on('mouseup',e=>{if(!start)return; const bbox=bboxString(start,e.latlng); document.getElementById('bbox').textContent=bbox; fetch('/select?bbox='+encodeURIComponent(bbox)).catch(()=>{}); start=null; map.dragging.enable();});\n" +
                    "</script></body></html>\n";
        }
    }
}

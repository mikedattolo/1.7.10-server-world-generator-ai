package com.mikedattolo.worldbuilder.gui;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JTextField;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class BBoxMapSelector extends JDialog {
    private final MapPanel mapPanel;
    private final JTextField bboxField;
    private String selectedBbox;

    private BBoxMapSelector(Window owner, String initialBbox) {
        super(owner, "Select DEM Area", ModalityType.APPLICATION_MODAL);
        this.mapPanel = new MapPanel(initialBbox);
        this.bboxField = new JTextField(initialBbox == null ? "" : initialBbox, 34);
        this.bboxField.setEditable(false);

        JLabel instructions = new JLabel("Drag on the map to highlight the DEM area. Use zoom buttons to adjust detail.");
        instructions.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));

        JPanel controls = new JPanel(new BorderLayout(8, 8));
        controls.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));

        JPanel mapButtons = new JPanel();
        JButton zoomIn = new JButton("Zoom In");
        JButton zoomOut = new JButton("Zoom Out");
        JButton centerUs = new JButton("Center US");
        JButton clear = new JButton("Clear");
        mapButtons.add(zoomIn);
        mapButtons.add(zoomOut);
        mapButtons.add(centerUs);
        mapButtons.add(clear);

        JPanel actionButtons = new JPanel();
        JButton useSelection = new JButton("Use Selected Area");
        JButton cancel = new JButton("Cancel");
        actionButtons.add(useSelection);
        actionButtons.add(cancel);

        controls.add(bboxField, BorderLayout.CENTER);
        controls.add(mapButtons, BorderLayout.WEST);
        controls.add(actionButtons, BorderLayout.EAST);

        mapPanel.setSelectionListener(new MapPanel.SelectionListener() {
            @Override
            public void bboxChanged(String bbox) {
                bboxField.setText(bbox);
            }
        });
        zoomIn.addActionListener(e -> mapPanel.zoomIn());
        zoomOut.addActionListener(e -> mapPanel.zoomOut());
        centerUs.addActionListener(e -> mapPanel.centerOn(39.0, -98.0));
        clear.addActionListener(e -> {
            mapPanel.clearSelection();
            bboxField.setText("");
        });
        useSelection.addActionListener(e -> {
            selectedBbox = bboxField.getText().trim();
            if (!selectedBbox.isEmpty()) {
                dispose();
            }
        });
        cancel.addActionListener(e -> {
            selectedBbox = null;
            dispose();
        });

        setLayout(new BorderLayout());
        add(instructions, BorderLayout.NORTH);
        add(mapPanel, BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);
        pack();
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(owner);
    }

    static String choose(Window owner, String initialBbox) {
        BBoxMapSelector selector = new BBoxMapSelector(owner, initialBbox);
        selector.setVisible(true);
        return selector.selectedBbox;
    }

    private static final class MapPanel extends JPanel {
        private static final int TILE_SIZE = 256;
        private static final int MIN_ZOOM = 2;
        private static final int MAX_ZOOM = 16;
        private static final Color WATER = new Color(200, 221, 235);
        private static final Color LAND = new Color(225, 226, 212);
        private static final Color GRID = new Color(112, 126, 135, 80);
        private static final Color SELECT_FILL = new Color(40, 120, 215, 70);
        private static final Color SELECT_STROKE = new Color(30, 100, 200);

        private final Map<String, Image> tileCache = new HashMap<String, Image>();
        private final Set<String> loadingTiles = new HashSet<String>();
        private int zoom = 5;
        private double centerLat = 39.0;
        private double centerLon = -98.0;
        private Point dragStart;
        private Point dragEnd;
        private SelectionListener selectionListener;

        MapPanel(String initialBbox) {
            setPreferredSize(new Dimension(900, 520));
            setBackground(WATER);
            parseInitialBbox(initialBbox);

            MouseAdapter mouse = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    dragStart = e.getPoint();
                    dragEnd = e.getPoint();
                    repaint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    dragEnd = e.getPoint();
                    publishSelection();
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    dragEnd = e.getPoint();
                    publishSelection();
                    repaint();
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
        }

        void setSelectionListener(SelectionListener selectionListener) {
            this.selectionListener = selectionListener;
        }

        void zoomIn() {
            zoom = Math.min(MAX_ZOOM, zoom + 1);
            repaint();
        }

        void zoomOut() {
            zoom = Math.max(MIN_ZOOM, zoom - 1);
            repaint();
        }

        void centerOn(double lat, double lon) {
            centerLat = clampLat(lat);
            centerLon = clampLon(lon);
            repaint();
        }

        void clearSelection() {
            dragStart = null;
            dragEnd = null;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paintTiles(g2);
            paintGrid(g2);
            paintSelection(g2);
            paintAttribution(g2);
            g2.dispose();
        }

        private void parseInitialBbox(String initialBbox) {
            if (initialBbox == null || initialBbox.trim().isEmpty()) {
                return;
            }
            String[] parts = initialBbox.split(",");
            if (parts.length != 4) {
                return;
            }
            try {
                double minLat = Double.parseDouble(parts[0].trim());
                double minLon = Double.parseDouble(parts[1].trim());
                double maxLat = Double.parseDouble(parts[2].trim());
                double maxLon = Double.parseDouble(parts[3].trim());
                centerLat = (minLat + maxLat) / 2.0;
                centerLon = (minLon + maxLon) / 2.0;
            } catch (RuntimeException ignored) {
                centerLat = 39.0;
                centerLon = -98.0;
            }
        }

        private void paintTiles(Graphics2D g2) {
            Point2D.Double center = lonLatToGlobal(centerLon, centerLat, zoom);
            double topLeftX = center.x - getWidth() / 2.0;
            double topLeftY = center.y - getHeight() / 2.0;
            int minTileX = (int) Math.floor(topLeftX / TILE_SIZE);
            int minTileY = (int) Math.floor(topLeftY / TILE_SIZE);
            int maxTileX = (int) Math.floor((topLeftX + getWidth()) / TILE_SIZE);
            int maxTileY = (int) Math.floor((topLeftY + getHeight()) / TILE_SIZE);
            int tileCount = 1 << zoom;

            g2.setColor(LAND);
            g2.fillRect(0, 0, getWidth(), getHeight());

            for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
                for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
                    if (tileY < 0 || tileY >= tileCount) {
                        continue;
                    }
                    int wrappedTileX = ((tileX % tileCount) + tileCount) % tileCount;
                    int drawX = (int) Math.round(tileX * TILE_SIZE - topLeftX);
                    int drawY = (int) Math.round(tileY * TILE_SIZE - topLeftY);
                    Image image = loadTile(wrappedTileX, tileY);
                    if (image != null) {
                        g2.drawImage(image, drawX, drawY, TILE_SIZE, TILE_SIZE, this);
                    } else {
                        g2.setColor(tileY % 2 == 0 ? LAND : new Color(216, 222, 206));
                        g2.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                    }
                }
            }
        }

        private Image loadTile(int tileX, int tileY) {
            String key = zoom + "/" + tileX + "/" + tileY;
            synchronized (tileCache) {
                if (tileCache.containsKey(key)) {
                    return tileCache.get(key);
                }
                if (loadingTiles.contains(key)) {
                    return null;
                }
                loadingTiles.add(key);
            }

            Thread loader = new Thread(new Runnable() {
                @Override
                public void run() {
                    Image image = null;
                    try {
                        image = ImageIO.read(new URL("https://tile.openstreetmap.org/" + key + ".png"));
                    } catch (IOException ignored) {
                        image = null;
                    }
                    synchronized (tileCache) {
                        loadingTiles.remove(key);
                        tileCache.put(key, image);
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            repaint();
                        }
                    });
                }
            }, "worldbuilder-map-tile-" + key.replace('/', '-'));
            loader.setDaemon(true);
            loader.start();
            return null;
        }

        private void paintGrid(Graphics2D g2) {
            g2.setColor(GRID);
            for (int x = 0; x < getWidth(); x += 80) {
                g2.drawLine(x, 0, x, getHeight());
            }
            for (int y = 0; y < getHeight(); y += 80) {
                g2.drawLine(0, y, getWidth(), y);
            }
        }

        private void paintSelection(Graphics2D g2) {
            Rectangle rect = selectionRect();
            if (rect == null) {
                return;
            }
            g2.setColor(SELECT_FILL);
            g2.fill(rect);
            g2.setColor(SELECT_STROKE);
            g2.setStroke(new BasicStroke(2.0f));
            g2.draw(rect);
        }

        private void paintAttribution(Graphics2D g2) {
            String text = "Map tiles: OpenStreetMap | Drag to select DEM bbox";
            FontMetrics fm = g2.getFontMetrics();
            int width = fm.stringWidth(text) + 12;
            int height = fm.getHeight() + 6;
            int x = getWidth() - width - 8;
            int y = getHeight() - height - 8;
            g2.setColor(new Color(255, 255, 255, 210));
            g2.fillRoundRect(x, y, width, height, 6, 6);
            g2.setColor(new Color(45, 45, 45));
            g2.drawString(text, x + 6, y + fm.getAscent() + 3);
        }

        private Rectangle selectionRect() {
            if (dragStart == null || dragEnd == null) {
                return null;
            }
            int x = Math.min(dragStart.x, dragEnd.x);
            int y = Math.min(dragStart.y, dragEnd.y);
            int width = Math.abs(dragStart.x - dragEnd.x);
            int height = Math.abs(dragStart.y - dragEnd.y);
            if (width < 4 || height < 4) {
                return null;
            }
            return new Rectangle(x, y, width, height);
        }

        private void publishSelection() {
            Rectangle rect = selectionRect();
            if (rect == null || selectionListener == null) {
                return;
            }
            Point2D.Double nw = screenToLonLat(rect.x, rect.y);
            Point2D.Double se = screenToLonLat(rect.x + rect.width, rect.y + rect.height);
            double minLat = Math.min(nw.y, se.y);
            double maxLat = Math.max(nw.y, se.y);
            double minLon = Math.min(nw.x, se.x);
            double maxLon = Math.max(nw.x, se.x);
            selectionListener.bboxChanged(String.format(Locale.US, "%.6f,%.6f,%.6f,%.6f", minLat, minLon, maxLat, maxLon));
        }

        private Point2D.Double screenToLonLat(int screenX, int screenY) {
            Point2D.Double center = lonLatToGlobal(centerLon, centerLat, zoom);
            double globalX = center.x + screenX - getWidth() / 2.0;
            double globalY = center.y + screenY - getHeight() / 2.0;
            return globalToLonLat(globalX, globalY, zoom);
        }

        private static Point2D.Double lonLatToGlobal(double lon, double lat, int zoom) {
            double sinLat = Math.sin(Math.toRadians(clampLat(lat)));
            double scale = TILE_SIZE * (1 << zoom);
            double x = (clampLon(lon) + 180.0) / 360.0 * scale;
            double y = (0.5 - Math.log((1.0 + sinLat) / (1.0 - sinLat)) / (4.0 * Math.PI)) * scale;
            return new Point2D.Double(x, y);
        }

        private static Point2D.Double globalToLonLat(double x, double y, int zoom) {
            double scale = TILE_SIZE * (1 << zoom);
            double lon = x / scale * 360.0 - 180.0;
            double mercator = Math.PI - 2.0 * Math.PI * y / scale;
            double lat = Math.toDegrees(Math.atan(Math.sinh(mercator)));
            return new Point2D.Double(clampLon(lon), clampLat(lat));
        }

        private static double clampLat(double lat) {
            return Math.max(-85.05112878, Math.min(85.05112878, lat));
        }

        private static double clampLon(double lon) {
            while (lon < -180.0) {
                lon += 360.0;
            }
            while (lon > 180.0) {
                lon -= 360.0;
            }
            return lon;
        }

        interface SelectionListener {
            void bboxChanged(String bbox);
        }
    }
}
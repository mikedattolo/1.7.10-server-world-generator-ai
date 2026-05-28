package com.mikedattolo.worldbuilder.dem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class TerrariumElevationProvider {
    private static final int TILE_SIZE = 256;
    private static final int ZOOM = 10;
    private static final int TIMEOUT_MS = 1500;
    private final Map<String, BufferedImage> cache = new HashMap<String, BufferedImage>();
    private boolean disabled;

    public Double elevationMeters(double lat, double lon) {
        if (disabled) {
            return null;
        }
        try {
            double clippedLat = Math.max(-85.05112878, Math.min(85.05112878, lat));
            double clippedLon = wrapLon(lon);
            double scale = 1 << ZOOM;
            double x = (clippedLon + 180.0) / 360.0 * scale;
            double sinLat = Math.sin(Math.toRadians(clippedLat));
            double y = (0.5 - Math.log((1.0 + sinLat) / (1.0 - sinLat)) / (4.0 * Math.PI)) * scale;
            int tileX = (int) Math.floor(x);
            int tileY = (int) Math.floor(y);
            int pixelX = Math.max(0, Math.min(255, (int) Math.floor((x - tileX) * TILE_SIZE)));
            int pixelY = Math.max(0, Math.min(255, (int) Math.floor((y - tileY) * TILE_SIZE)));
            BufferedImage tile = tile(tileX, tileY);
            if (tile == null) {
                return null;
            }
            int rgb = tile.getRGB(pixelX, pixelY);
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = rgb & 0xFF;
            return (red * 256.0 + green + blue / 256.0) - 32768.0;
        } catch (RuntimeException ex) {
            disabled = true;
            return null;
        }
    }

    private BufferedImage tile(int tileX, int tileY) {
        String key = ZOOM + "/" + tileX + "/" + tileY;
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        try {
            URL url = new URL("https://s3.amazonaws.com/elevation-tiles-prod/terrarium/" + key + ".png");
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("User-Agent", "WorldBuilderDEM/1.0");
            BufferedImage image = ImageIO.read(connection.getInputStream());
            cache.put(key, image);
            return image;
        } catch (IOException ex) {
            disabled = true;
            cache.put(key, null);
            return null;
        }
    }

    private static double wrapLon(double lon) {
        while (lon < -180.0) {
            lon += 360.0;
        }
        while (lon > 180.0) {
            lon -= 360.0;
        }
        return lon;
    }
}

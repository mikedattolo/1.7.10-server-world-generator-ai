package com.mikedattolo.worldbuilder.dem;

public final class AddressResolver {
    private AddressResolver() {
    }

    // Placeholder deterministic resolver for offline/no-network preprocessing flows.
    // It hashes the address into bounded pseudo-coordinates (lat ~20..90, lon ~-130..-70)
    // and expands that point by radius meters. Replace this with real geocoding in production.
    public static BoundingBox resolveToBoundingBox(String address, int radiusMeters) {
        int hash = Math.abs(address.hashCode());
        double lat = 20.0 + ((hash % 7000) / 100.0);
        double lon = -130.0 + (((hash / 7) % 6000) / 100.0);
        double delta = Math.max(radiusMeters, 100) / 111000.0;
        return new BoundingBox(lat - delta, lon - delta, lat + delta, lon + delta);
    }
}

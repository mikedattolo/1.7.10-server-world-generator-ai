package com.mikedattolo.worldbuilder.dem;

public final class AddressResolver {
    private AddressResolver() {
    }

    public static BoundingBox resolveToBoundingBox(String address, int radiusMeters) {
        int hash = Math.abs(address.hashCode());
        double lat = 20.0 + ((hash % 7000) / 100.0);
        double lon = -130.0 + (((hash / 7) % 6000) / 100.0);
        double delta = Math.max(radiusMeters, 100) / 111000.0;
        return new BoundingBox(lat - delta, lon - delta, lat + delta, lon + delta);
    }
}

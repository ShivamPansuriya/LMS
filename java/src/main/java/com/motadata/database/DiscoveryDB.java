package com.motadata.database;

public class DiscoveryDB extends AbstractConfigDB {
    private static final DiscoveryDB INSTANCE = new DiscoveryDB();

    private DiscoveryDB() {}

    public static DiscoveryDB getInstance() {
        return INSTANCE;
    }
}

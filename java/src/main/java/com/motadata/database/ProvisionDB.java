package com.motadata.database;

public class ProvisionDB extends AbstractConfigDB {
    private static final ProvisionDB INSTANCE = new ProvisionDB();

    private ProvisionDB() {}

    public static ProvisionDB getInstance() {
        return INSTANCE;
    }
}

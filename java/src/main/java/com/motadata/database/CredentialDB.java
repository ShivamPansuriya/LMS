package com.motadata.database;

public class CredentialDB extends AbstractConfigDB {
    private static final CredentialDB INSTANCE = new CredentialDB();

    private CredentialDB() {}

    public static CredentialDB getInstance() {
        return INSTANCE;
    }
}

package org.example;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import org.example.credentialprofile.CredentialProfile;
import org.example.database.DBHandler;
import org.example.pluginengine.PluginEngine;
import org.example.route.Routes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class boostrap
{
    public static final Logger logger = LoggerFactory.getLogger(boostrap.class);

    public static void main(String[] args)
    {

        var vertx = Vertx.vertx();

        vertx.deployVerticle(Routes.class.getName());

        vertx.deployVerticle(CredentialProfile.class.getName(),new DeploymentOptions().setInstances(1));

        vertx.deployVerticle(discovery.class.getName(),new DeploymentOptions().setInstances(1));

        vertx.deployVerticle(DBHandler.class.getName(),new DeploymentOptions().setInstances(1).setThreadingModel(ThreadingModel.WORKER));

        vertx.deployVerticle(PluginEngine.class.getName(),new DeploymentOptions().setInstances(4).setThreadingModel(ThreadingModel.WORKER));

        logger.info("verticle deployed");

    }
}

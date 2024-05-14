package com.motadata;

import com.motadata.database.DBHandler;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import com.motadata.credentialprofile.CredentialProfile;
import com.motadata.discovery.Discovery;
import com.motadata.pluginengine.PluginEngine;
import com.motadata.provision.Provision;
import com.motadata.route.APIEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Boostrap
{
    public static final Logger logger = LoggerFactory.getLogger(Boostrap.class);

    public static void main(String[] args)
    {

        logger.info("Application started");

        var vertx = Vertx.vertx();

        vertx.deployVerticle(APIEngine.class.getName()).onComplete(result->
        {
            if(result.succeeded())
            {
                logger.info("API Engine started");
            }
            else
            {
                logger.error(result.cause().getMessage());
            }
        });

        vertx.deployVerticle(CredentialProfile.class.getName(),new DeploymentOptions().setInstances(1)).onComplete(result->
        {
            if(result.succeeded())
            {
                logger.info("Credential Profile vertical deployed");
            }
            else
            {
                logger.error(result.cause().getMessage());
            }
        });

        vertx.deployVerticle(Discovery.class.getName(),new DeploymentOptions().setInstances(1)).onComplete(result->
        {
            if(result.succeeded())
            {
                logger.info("Discovery vertical deployed");
            }
            else
            {
                logger.error(result.cause().getMessage());
            }
        });

        vertx.deployVerticle(Provision.class.getName(),new DeploymentOptions().setInstances(1)).onComplete(result->
        {
            if(result.succeeded())
            {
                logger.info("Provision vertical deployed");
            }
            else
            {
                logger.error(result.cause().getMessage());
            }
        });

        vertx.deployVerticle(DBHandler.class.getName(),new DeploymentOptions().setInstances(1).setThreadingModel(ThreadingModel.WORKER)).onComplete(result->
        {
            if(result.succeeded())
            {
                logger.info("DB Engine started");
            }
            else
            {
                logger.error(result.cause().getMessage());
            }
        });

        vertx.deployVerticle(PluginEngine.class.getName(),new DeploymentOptions().setInstances(4).setThreadingModel(ThreadingModel.WORKER)).onComplete(result->
        {
            if(result.succeeded())
            {
                logger.info("Plugin Engine started");
            }
            else
            {
                logger.error(result.cause().getMessage());
            }
        });

        logger.info("Application verticals deployed");
    }
}

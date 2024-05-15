package com.motadata;

import com.motadata.engine.ConfigEngine;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import com.motadata.profile.Credential;
import com.motadata.profile.Discovery;
import com.motadata.engine.PluginEngine;
import com.motadata.profile.Provision;
import com.motadata.engine.APIEngine;
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

        vertx.deployVerticle(Credential.class.getName(),new DeploymentOptions().setInstances(1)).onComplete(result->
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

        vertx.deployVerticle(ConfigEngine.class.getName(),new DeploymentOptions().setInstances(1).setThreadingModel(ThreadingModel.WORKER)).onComplete(result->
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

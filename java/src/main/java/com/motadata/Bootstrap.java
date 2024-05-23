package com.motadata;

import com.motadata.api.ApiEngine;
import com.motadata.engine.AgentHandler;
import com.motadata.engine.Scheduler;
import com.motadata.engine.Worker;
import com.motadata.utils.Config;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap
{
    public static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args)
    {
        logger.info("Application started");

        if(Config.PORT == Config.ZMQ_PORT)
        {
            System.out.println("multiple ports can't be same");

            logger.error("both given port zmq and host.port are same");

            return;
        }
        var vertx = Vertx.vertx();

        try
        {
            vertx.deployVerticle(Worker.class.getName(), new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))

                    .compose(id -> vertx.deployVerticle(Scheduler.class.getName()))

                    .compose(id -> vertx.deployVerticle(ApiEngine.class.getName()))

                    .compose(id-> vertx.deployVerticle(AgentHandler.class.getName()))

                    .onSuccess(handler-> logger.info("Application verticals deployed"))

                    .onFailure(handler -> logger.error("unable to deploy vertical"));

        } catch(Exception exception)
        {
            logger.error("deploy exception {}", exception.getMessage());

            logger.error("Application stopped");
        }

    }
}

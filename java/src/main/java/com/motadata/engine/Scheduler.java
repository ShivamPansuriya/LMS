package com.motadata.engine;

import com.motadata.constants.Constants;
import com.motadata.database.ProvisionDB;
import com.motadata.utils.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Scheduler extends AbstractVerticle
{
    static final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    @Override
    public void start(Promise<Void> startPromise)
    {
        var eventBus = vertx.eventBus();

        var validPollID = new JsonArray();

        vertx.setPeriodic(Config.POLL_TIME * 1000L, deploymentID ->
        {
            validPollID.clear();

            var contexts = ProvisionDB.getInstance().getAll(Constants.PROFILES);

            logger.debug("getting data for {}",contexts);

            eventBus.send(Constants.COLLECT,contexts);

        });

        startPromise.complete();

    }
}

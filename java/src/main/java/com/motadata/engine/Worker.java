package com.motadata.engine;

import com.motadata.constants.Constants;
import com.motadata.database.DiscoveryDB;
import com.motadata.utils.ProcessBuilder;
import com.motadata.utils.Utils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Worker extends AbstractVerticle
{
    static final Logger logger = LoggerFactory.getLogger(Worker.class);

    @Override
    public void start(Promise<Void> startPromise)
    {
        vertx.eventBus().<JsonArray>localConsumer(Constants.RUN_DISCOVERY, message -> {

            var validContext = new JsonArray();

            for(var context : message.body())
            {
                if(ProcessBuilder.available(new JsonObject(context.toString()).getString(Constants.IP)))
                {
                    validContext.add(new JsonObject(context.toString()));
                }
            }

             var results  = ProcessBuilder.spawnProcess(validContext);

            for(var object:results)
            {
                var result = new JsonObject(object.toString());

                if(!result.getString(Constants.CREDENTIAL_PROFILE_ID).equals("-1"))
                {
                    var profile = Utils.createContext(result);

                    DiscoveryDB.getInstance().create(Long.parseLong(result.getString(Constants.DISCOVERY_ID)),profile);
                }
            }
        });

        vertx.eventBus().<JsonArray>localConsumer(Constants.COLLECT, message -> {

            var validContext = new JsonArray();

            for(var context : message.body())
            {
                if(ProcessBuilder.available(new JsonObject(context.toString()).getString(Constants.IP)))
                {
                    validContext.add(new JsonObject(context.toString()));
                }
            }

            if(!validContext.isEmpty())
            {
                var results  = ProcessBuilder.spawnProcess(validContext);

                for(var object : results)
                {
                    var result = new JsonObject(object.toString());

                    Utils.writeToFile(vertx, result).onSuccess(handler ->

                                    logger.info("content is added to file: {}", result))

                            .onFailure(handler ->

                                    logger.error("error in writing file: {}", handler.getMessage()));
                }
            }
        });


        startPromise.complete();
    }
}


/*
{
   "credential.profile.id":0,
   "device.type":"linux",
   "discovery.id":"1",
   "ip":"192.168.185.249",
   "object.timeout":30,
   "port":22,
   "request.type":"collect",
   "status":"success"
}
 */
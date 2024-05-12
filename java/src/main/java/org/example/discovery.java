package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.example.utils.Constants;

public class discovery extends AbstractVerticle
{
    @Override
    public void start() throws Exception
    {
        vertx.eventBus().localConsumer(Constants.CREATE_DISCOVERY, message ->
        {
            var ms = "discovery"+Constants.MESSAGE_SEPARATOR +message.body().toString();

            vertx.eventBus().request("insert",ms , msg ->
            {
                if(msg.succeeded())
                {
                    message.reply(msg.result().body().toString());
                }
            });
        });

        vertx.eventBus().localConsumer(Constants.GET_DISCOVERY, message ->
        {
            vertx.eventBus().request("get","discovery" , msg ->
            {
                message.reply(msg.result().body().toString());
            });
        });

        vertx.eventBus().localConsumer(Constants.RUN_DISCOVERY, message ->
        {
            var ms = "Discovery"+Constants.MESSAGE_SEPARATOR +message.body().toString();

            vertx.eventBus().request("run.engine",ms,msg ->
            {
                if(msg.succeeded())
                {
                   var result = new JsonArray(msg.result().body().toString());
                }
            });

        });

    }


}

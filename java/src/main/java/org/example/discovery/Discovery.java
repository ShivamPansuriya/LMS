package org.example.discovery;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.example.utils.Constants;
import org.example.utils.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discovery extends AbstractVerticle
{
    static final Logger logger = LoggerFactory.getLogger(Discovery.class);

    @Override
    public void start()
    {
        vertx.eventBus().localConsumer(Constants.SAVE_DISCOVERY, message ->
        {
            try
            {
                var request = new JsonObject(message.body().toString());

                if(!request.containsKey("object.ip") || !request.containsKey("object.port") || !request.containsKey("credentials"))
                {
                    message.fail(400, new JsonObject().put(Constants.STATUS, Constants.STATUS_FAIL).put("message", "please entre valid fields").toString());
                }
                else
                {
                    var ms = "discovery" + Constants.MESSAGE_SEPARATOR + message.body().toString();

                    vertx.eventBus().request("insert", ms, msg ->
                    {
                        if(msg.succeeded())
                        {
                            message.reply(msg.result().body().toString());
                        }
                    });
                }

            }
            catch(Exception exception)
            {
                message.fail(400, new JsonObject().put(Constants.STATUS, Constants.STATUS_FAIL).put("message", "please entre valid json format").toString());

                logger.error(String.format("not valid json format %s",exception));
            }
        });

        vertx.eventBus().localConsumer(Constants.GET_DISCOVERY, message ->
        {
            vertx.eventBus().request("get", "discovery", msg ->
            {
                message.reply(msg.result().body().toString());
            });
        });

        vertx.eventBus().localConsumer(Constants.RUN_DISCOVERY, message ->
        {

            try
            {
                var request = new JsonObject(message.body().toString());

                var helper = new Helper();

                if(request.isEmpty() || !request.containsKey("discoveryID"))
                {
                    message.fail(400, new JsonObject().put(Constants.STATUS, Constants.STATUS_FAIL).put("message", "please entre discoveryID in Json").toString());
                }
                else
                {

                    var ms = "getContext" + Constants.MESSAGE_SEPARATOR + request.getString("discoveryID");

                    vertx.eventBus().request("get", ms, reply ->
                    {
                        if(reply.succeeded())
                        {
                            var context = new JsonObject(reply.result().body().toString());

                            var contexts = helper.createContext(context.getJsonArray("discovery.data"), "Discovery", logger);

                            if(!contexts.isEmpty())
                            {
                                var contextMessage = contexts.toString();

                                vertx.eventBus().request("run.engine", contextMessage, msg ->
                                {
                                    var results = new JsonArray(msg.result().body().toString());

                                    var credentialID = results.getJsonObject(0).getString("credential.profile.id");

                                    if(credentialID.equals("-1"))
                                    {
                                        message.reply(new JsonObject().put(Constants.STATUS, Constants.STATUS_FAIL).put("message", "no valid credential profile is present"));
                                    }
                                    else
                                    {
                                        var insertMessage = Constants.CREATE_DISCOVERY + Constants.MESSAGE_SEPARATOR + request.getString("discoveryID") + Constants.MESSAGE_SEPARATOR + credentialID;

                                        vertx.eventBus().request("insert", insertMessage, resultHandler ->
                                        {
                                            if(resultHandler.succeeded())
                                            {
                                                message.reply(resultHandler.result().body().toString());
                                            }
                                        });
                                    }

                                });
                            }
                        }
                    });
                }

            }
            catch(Exception exception)
            {
                message.fail(400, new JsonObject().put(Constants.STATUS, Constants.STATUS_FAIL).put("message", "please entre valid json format").toString());

                logger.error(String.format("not valid json format %s",exception));
            }
        });

    }

}

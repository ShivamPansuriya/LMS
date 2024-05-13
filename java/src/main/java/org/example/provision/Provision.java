package org.example.provision;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.example.utils.Constants;
import org.example.utils.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Provision extends AbstractVerticle
{
    static final Logger logger = LoggerFactory.getLogger(Provision.class);

    @Override
    public void start()
    {
        vertx.eventBus().localConsumer(Constants.PROVISION, message ->
        {
            try
            {
                var request = new JsonObject(message.body().toString());

                if(request.isEmpty() || !request.containsKey("discoveryID"))
                {
                    message.fail(400, new JsonObject().put(Constants.STATUS, Constants.STATUS_FAIL).put("message", "please fill proper information").toString());
                }
                else
                {
                    var helper = new Helper();

                    var discoveryID = request.getString("discoveryID");

                    final boolean[] issend = new boolean[1];

                    var setProvision = "setProvision" + Constants.MESSAGE_SEPARATOR + discoveryID;

                    vertx.eventBus().request("insert", setProvision, result ->
                    {
                        issend[0] = true;

                        message.reply(new JsonObject(result.result().body().toString()));
                    });
                }

            }
            catch(Exception exception)
            {
                message.fail(400, new JsonObject().put(Constants.STATUS, Constants.STATUS_FAIL).put("message", "please entre valid json format").toString());
            }
        });

        vertx.eventBus().localConsumer(Constants.GET_POOL_DATA, message ->
        {
            try
            {
                var requestJson = new JsonObject(message.body().toString());

                if(requestJson.isEmpty() && !requestJson.containsKey("object.ip"))
                {
                    message.fail(400, new JsonObject().put(Constants.STATUS, Constants.STATUS_FAIL).toString());
                }
                else
                {
                    var requestMessage = "getData" + Constants.MESSAGE_SEPARATOR + requestJson.getString("object.ip");
                    vertx.eventBus().request("get", requestMessage, result -> {
                        if(result.succeeded())
                        {
                            message.reply(result.result().body().toString());
                        }
                    });
                }
            } catch(DecodeException e)
            {
                message.fail(400, new JsonObject().put(Constants.STATUS, Constants.STATUS_FAIL).put("message", "please entre valid json format").toString());
            }
        });


        var validPollID = new JsonArray();

        var helper = new Helper();

        vertx.setPeriodic(10*1000, deploymentID ->
        {
            validPollID.clear();

            var requestMessage = "getPoolingTime" + Constants.MESSAGE_SEPARATOR;

            vertx.eventBus().request("get", requestMessage, result ->
            {
                if(result.succeeded())
                {
                    var pollTimeJson = new JsonObject(result.result().body().toString());

                    var pollTimeMap = pollTimeJson.getMap();

                    var poolTime = (HashMap<String, Integer>) pollTimeMap.get("polling.time.map");

                    for(var discoveryId : poolTime.keySet())
                    {
                        if(poolTime.get(discoveryId) == 60)
                        {
                            validPollID.add(discoveryId);
                        }
                    }

                    if(!validPollID.isEmpty())
                    {
                        var ms = "getProvisionContext" + Constants.MESSAGE_SEPARATOR + validPollID;

                        vertx.eventBus().request("get", ms, reply -> {

                            if(reply.succeeded())
                            {
                                var replyMessage = reply.result().body().toString();

                                var discoveryProfile = new JsonObject(replyMessage);

                                if(discoveryProfile.getJsonArray("poll.data").isEmpty())
                                {
                                    logger.error("there is data to be poll");
                                }
                                else
                                {
                                    var contextArray = helper.createContext(discoveryProfile.getJsonArray("poll.data"), "Collect", logger);

                                    if(!contextArray.isEmpty())
                                    {
                                        vertx.eventBus().request("run.engine", contextArray, msg -> {

                                            var messageContext = "contextResult" + Constants.MESSAGE_SEPARATOR + msg.result().body().toString();

                                            vertx.eventBus().request("insert", messageContext);
                                        });
                                    }
                                    else
                                    {
                                        logger.error("error in creating request context JSON");
                                    }
                                }
                            }
                        });
                    }

                }
            });
        });
    }
}

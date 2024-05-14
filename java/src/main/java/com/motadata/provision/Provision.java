package com.motadata.provision;

import com.motadata.utils.Helper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import com.motadata.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class Provision extends AbstractVerticle
{
    static final Logger logger = LoggerFactory.getLogger(Provision.class);

    @Override
    public void start(Promise<Void> startPromise)
    {
        var eventBus = vertx.eventBus();

        eventBus.localConsumer(Constants.PROVISION, message ->
        {
            try
            {
                var request = new JsonObject(message.body().toString());

                if(request.isEmpty() || !request.containsKey(Constants.ID))
                {
                    logger.error("invalid json request");

                    message.fail(400,new JsonObject().put(Constants.STATUS,Constants.STATUS_FAIL).put(Constants.ERROR,new JsonObject().put(Constants.ERROR_MESSAGE,"please entre valid fields").put(Constants.ERROR_CODE,400).put(Constants.ERROR,"invalid format")).toString());
                }
                else
                {
                    var discoveryID = request.getString(Constants.ID);

                    var object = new JsonObject().put(Constants.REQUEST_TYPE,Constants.SET_PROVISION).put(Constants.REQUEST_DATA,request);

                    eventBus.request(Constants.POST, object, result ->
                    {
                        if(result.succeeded())
                        {
                            message.reply(result.result().body().toString());
                        }
                        else
                        {
                            message.reply(result.cause().getMessage());
                        }
                    });
                }

            }
            catch(Exception exception)
            {
                logger.error(String.format("invalid json format %s",exception));

                message.fail(400,new JsonObject().put(Constants.STATUS,Constants.STATUS_FAIL).put(Constants.ERROR,new JsonObject().put(Constants.ERROR_MESSAGE,"please entre valid json format").put(Constants.ERROR_CODE,400).put(Constants.ERROR,"invalid format")).toString());
            }
        });

        eventBus.localConsumer(Constants.GET_POOL_DATA, message ->
        {
            try
            {
                var request = new JsonObject(message.body().toString());

                if(request.isEmpty() && !request.containsKey(Constants.IP))
                {
                    logger.error("invalid Json request");

                    message.fail(400,new JsonObject().put(Constants.STATUS,Constants.STATUS_FAIL).put(Constants.ERROR,new JsonObject().put(Constants.ERROR_MESSAGE,"please entre valid fields").put(Constants.ERROR_CODE,400).put(Constants.ERROR,"invalid format")).toString());
                }
                else
                {
                    var object = new JsonObject().put(Constants.REQUEST_TYPE,Constants.POOL_DATA).put(Constants.REQUEST_DATA,request);

                    eventBus.request(Constants.GET, object, result ->
                    {
                        if(result.succeeded())
                        {
                            message.reply(result.result().body().toString());
                        }
                        else
                        {
                            message.reply(result.cause().getMessage());
                        }
                    });
                }
            }
            catch(Exception exception)
            {
                logger.error(String.format("invalid json format %s",exception));

                message.fail(400,new JsonObject().put(Constants.STATUS,Constants.STATUS_FAIL).put(Constants.ERROR,new JsonObject().put(Constants.ERROR_MESSAGE,"please entre valid json format").put(Constants.ERROR_CODE,400).put(Constants.ERROR,"invalid format")).toString());
            }
        });

        var validPollID = new JsonArray();

        var helper = new Helper();

        vertx.setPeriodic(10*1000, deploymentID ->
        {
            validPollID.clear();

            var object = new JsonObject().put(Constants.REQUEST_TYPE,Constants.POOL_TIME).put(Constants.REQUEST_DATA,new JsonObject());

            eventBus.request(Constants.GET, object, result ->
            {
                if(result.succeeded())
                {
                    var pollTimeJson = new JsonObject(result.result().body().toString());

                    var pollTimeMap = pollTimeJson.getMap();

                    var poolTime = (HashMap<String, Integer>) pollTimeMap.get(Constants.POOL_TIME);

                    for(var discoveryId : poolTime.keySet())
                    {
                        if(poolTime.get(discoveryId) == 60)
                        {
                            validPollID.add(discoveryId);
                        }
                    }

                    if(!validPollID.isEmpty())
                    {
                        logger.info(String.format("fetching data for %s",validPollID));

                        var contextRequest = new JsonObject().put(Constants.REQUEST_TYPE,Constants.PROVISION_CONTEXT).put(Constants.REQUEST_DATA,new JsonObject().put(Constants.ID,validPollID.toString()));

                        eventBus.request(Constants.GET, contextRequest, reply ->
                        {
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
                                        eventBus.request(Constants.RUN, contextArray, msg ->
                                        {
                                            var pollData = new JsonObject().put(Constants.REQUEST_TYPE,Constants.POOL_DATA).put(Constants.REQUEST_DATA, new JsonObject().put("data",msg.result().body().toString()));

                                            eventBus.send(Constants.POST, pollData);
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

        startPromise.complete();
    }
}

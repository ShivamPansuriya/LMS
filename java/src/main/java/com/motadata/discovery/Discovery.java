package com.motadata.discovery;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import com.motadata.utils.Constants;
import com.motadata.utils.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discovery extends AbstractVerticle
{
    static final Logger logger = LoggerFactory.getLogger(Discovery.class);

    @Override
    public void start(Promise<Void> startPromise)
    {
        var eventBus = vertx.eventBus();

        eventBus.localConsumer(Constants.CREATE_DISCOVERY, message ->
        {
            try
            {
                var request = new JsonObject(message.body().toString());

                if(!request.containsKey(Constants.IP) || !request.containsKey(Constants.PORT) || !request.containsKey("credentials"))
                {
                    message.fail(400, new JsonObject().put(Constants.STATUS, Constants.STATUS_FAIL).put(Constants.MESSAGE, "please entre valid fields").toString());
                }
                else
                {
                    var object = new JsonObject().put(Constants.REQUEST_TYPE,Constants.DISCOVERY).put(Constants.REQUEST_DATA,request);

                    eventBus.request(Constants.POST, object, result ->
                    {
                        if(result.succeeded())
                        {
                            message.reply(result.result().body().toString());
                        }
                        else
                        {
                            logger.warn("unable to save discovery profiles to DB");

                            message.fail(501,result.cause().getMessage());
                        }
                    });
                }

            }
            catch(Exception exception)
            {
                message.fail(400, new JsonObject().put(Constants.STATUS, Constants.STATUS_FAIL).put(Constants.MESSAGE, "please entre valid json format").toString());

                logger.error(String.format("not valid json format %s",exception));
            }
        });

        eventBus.localConsumer(Constants.GET_DISCOVERY, message ->
        {
            var object = new JsonObject().put(Constants.REQUEST_TYPE,Constants.DISCOVERY).put(Constants.REQUEST_DATA,new JsonObject());

            eventBus.request(Constants.GET, object, result ->
            {
                if(result.succeeded())
                {
                    message.reply(result.result().body().toString());
                }
                else
                {
                    logger.warn("unable to get discovery profiles from DB");

                    message.fail(501,result.cause().getMessage());
                }
            });
        });

        eventBus.localConsumer(Constants.RUN_DISCOVERY, message ->
        {
            try
            {
                var request = new JsonObject(message.body().toString());

                var helper = new Helper();

                if(request.isEmpty() || !request.containsKey(Constants.ID))
                {
                    logger.info("not valid json value request");

                    message.fail(400, new JsonObject().put(Constants.STATUS, Constants.STATUS_FAIL).put(Constants.MESSAGE, "please entre discoveryID in Json").toString());
                }
                else
                {
                    var contextRequest = new JsonObject().put(Constants.REQUEST_TYPE,Constants.CONTEXT).put(Constants.REQUEST_DATA,request);

                    eventBus.request(Constants.GET, contextRequest, reply ->
                    {
                        if(reply.succeeded())
                        {
                            var context = new JsonObject(reply.result().body().toString());

                            var contexts = helper.createContext(context.getJsonArray("discovery.data"), "Discovery", logger);

                            if(!contexts.isEmpty())
                            {
                                var contextMessage = contexts.toString();

                                eventBus.request(Constants.RUN, contextMessage, msg ->
                                {
                                    if(msg.succeeded())
                                    {

                                        var results = new JsonArray(msg.result().body().toString());

                                        var result = results.getJsonObject(0);

                                        if(result.containsKey(Constants.ERROR))
                                        {
                                            logger.warn(String.format("There is error in running plugin engine: %s", result.getString(Constants.ERROR)));

                                            message.fail(500,new JsonObject().put(Constants.STATUS,Constants.STATUS_FAIL).put(Constants.ERROR,new JsonObject().put(Constants.ERROR_MESSAGE,"There is error in running plugin engine").put(Constants.ERROR_CODE,500).put(Constants.ERROR,"Plugin error")).toString());
                                        }
                                        else
                                        {
                                            var credentialID = result.getString("credential.profile.id");

                                            if(credentialID.equals(Constants.INVALID_CREDENTIAL))
                                            {
                                                logger.warn(String.format("all given credentials are invalid. request: %s", context));

                                                message.fail(501, new JsonObject().put(Constants.STATUS, Constants.STATUS_FAIL).put(Constants.MESSAGE, "no valid credential profile is present").toString());
                                            }
                                            else
                                            {
                                                var object = new JsonObject().put(Constants.REQUEST_TYPE, Constants.SAVE_DISCOVERY).put(Constants.REQUEST_DATA, new JsonObject().put("discovery.id", request.getString(Constants.ID)).put("credential.id", credentialID));

                                                eventBus.request(Constants.POST, object, resultHandler -> {
                                                    if(resultHandler.succeeded())
                                                    {
                                                        message.reply(resultHandler.result().body().toString());
                                                    }
                                                    else
                                                    {
                                                        logger.error(resultHandler.cause().getMessage());

                                                        message.reply(resultHandler.cause().getMessage());
                                                    }
                                                });
                                            }
                                        }
                                    }
                                    else
                                    {
                                        message.reply( msg.cause().getMessage());
                                    }
                                });
                            }
                        }
                        else
                        {
                            message.fail(501,reply.cause().getMessage());
                        }
                    });
                }

            }
            catch(Exception exception)
            {
                message.fail(400, new JsonObject().put(Constants.STATUS, Constants.STATUS_FAIL).put(Constants.MESSAGE, "please entre valid json format").toString());

                logger.error(String.format("not valid json format %s",exception));
            }
        });

        eventBus.localConsumer(Constants.UPDATE_DISCOVERY, message ->
        {
            try
            {
                var request = new JsonObject(message.body().toString());

                if( !request.containsKey(Constants.IP) || !request.containsKey(Constants.PORT) || !request.containsKey(Constants.ID))
                {
                    logger.info("not proper request format");

                    message.fail(400,new JsonObject().put(Constants.STATUS,Constants.STATUS_FAIL).put(Constants.ERROR,new JsonObject().put(Constants.ERROR_MESSAGE,"please entre valid fields").put(Constants.ERROR_CODE,400).put(Constants.ERROR,"invalid format")).toString());
                }
                else
                {
                    var object = new JsonObject().put(Constants.REQUEST_TYPE,Constants.DISCOVERY).put(Constants.REQUEST_DATA,request);

                    eventBus.request(Constants.PUT, object, result ->
                    {
                        if(result.succeeded())
                        {
                            message.reply(result.result().body().toString());
                        }
                        else
                        {
                            logger.warn("unable to UPDATE discovery profiles to DB");

                            message.reply(result.cause().getMessage());
                        }
                    });
                }
            }
            catch(Exception exception)
            {
                logger.error(String.format("json invalid format: %s",exception));

                message.fail(400,new JsonObject().put(Constants.STATUS,Constants.STATUS_FAIL).put(Constants.ERROR,new JsonObject().put(Constants.ERROR_MESSAGE,"please entre valid JSON object").put(Constants.ERROR_CODE,400).put(Constants.ERROR,"invalid format")).toString());
            }
        });

        eventBus.localConsumer(Constants.DELETE_DISCOVERY, message ->
        {
            try
            {
                var request = new JsonObject(message.body().toString());

                if(!request.containsKey(Constants.ID))
                {
                    logger.info("not proper request format");

                    message.fail(400,new JsonObject().put(Constants.STATUS,Constants.STATUS_FAIL).put(Constants.ERROR,new JsonObject().put(Constants.ERROR_MESSAGE,"please entre valid fields").put(Constants.ERROR_CODE,400).put(Constants.ERROR,"invalid format")).toString());
                }
                else
                {
                    var object = new JsonObject().put(Constants.REQUEST_TYPE,Constants.DISCOVERY).put(Constants.REQUEST_DATA,request);

                    eventBus.request(Constants.DELETE, object, result ->
                    {
                        if(result.succeeded())
                        {
                            message.reply(result.result().body().toString());
                        }
                        else
                        {
                            logger.warn("unable to DELETE discovery profiles from DB");

                            message.fail(501,result.cause().getMessage());
                        }
                    });
                }
            }
            catch(Exception exception)
            {
                logger.error(String.format("json invalid format: %s",exception));

                message.fail(400,new JsonObject().put(Constants.STATUS,Constants.STATUS_FAIL).put(Constants.ERROR,new JsonObject().put(Constants.ERROR_MESSAGE,"please entre valid json format").put(Constants.ERROR_CODE,400).put(Constants.ERROR,"invalid format")).toString());
            }
        });

        startPromise.complete();
    }

}

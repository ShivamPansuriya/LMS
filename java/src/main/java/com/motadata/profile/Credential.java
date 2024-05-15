package com.motadata.profile;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import com.motadata.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Credential extends AbstractVerticle
{
    static final Logger logger = LoggerFactory.getLogger(Discovery.class);

    @Override
    public void start(Promise<Void> startPromise)
    {
        var eventBus = vertx.eventBus();

        eventBus.localConsumer(Constants.CREATE_CREDENTIAL, message ->
        {
            try
            {
                var request = new JsonObject(message.body().toString());

                if(!request.containsKey(Constants.HOSTNAME) || !request.containsKey(Constants.PASSWORD))
                {
                    logger.info("missing arguments in request json");

                    message.fail(400,new JsonObject().put(Constants.STATUS,Constants.STATUS_FAIL).put(Constants.ERROR,new JsonObject().put(Constants.ERROR_MESSAGE,"please entre valid fields").put(Constants.ERROR_CODE,400).put(Constants.ERROR,"invalid format")).toString());
                }
                else
                {
                    var object = new JsonObject().put(Constants.REQUEST_TYPE,Constants.CREDENTIAL).put(Constants.REQUEST_DATA,request);

                    eventBus.request(Constants.POST, object, result ->
                    {
                        if(result.succeeded())
                        {
                            message.reply(result.result().body().toString());
                        }
                        else
                        {
                            logger.warn("unable to save credential profiles to DB");

                            message.fail(501,result.cause().getMessage());
                        }
                    });
                }
            }
            catch(Exception exception)
            {
                logger.error(String.format("invalid format: %s",exception));

                message.fail(400,new JsonObject().put(Constants.STATUS,Constants.STATUS_FAIL).put(Constants.ERROR,new JsonObject().put(Constants.ERROR_MESSAGE,"please entre valid input format").put(Constants.ERROR_CODE,400).put(Constants.ERROR,"invalid format")).toString());
            }
        });

        eventBus.localConsumer(Constants.GET_CREDENTIAL, message ->
        {
            var object = new JsonObject().put(Constants.REQUEST_TYPE,Constants.CREDENTIAL).put(Constants.REQUEST_DATA,new JsonObject());

            eventBus.request(Constants.GET,object , result ->
            {
                if(result.succeeded())
                {
                    message.reply(result.result().body().toString());
                }
                else
                {
                    logger.warn("unable to fetch credential profiles from DB");

                    message.fail(501,result.cause().getMessage());
                }
            });
        });

        eventBus.localConsumer(Constants.UPDATE_CREDENTIAL, message ->
        {
            try
            {
                var request = new JsonObject(message.body().toString());

                if(!request.containsKey(Constants.HOSTNAME) || !request.containsKey(Constants.PASSWORD) || !request.containsKey(Constants.ID))
                {
                    logger.info("not proper request format");

                    message.fail(400,new JsonObject().put(Constants.STATUS,Constants.STATUS_FAIL).put(Constants.ERROR,new JsonObject().put(Constants.ERROR_MESSAGE,"please entre valid fields").put(Constants.ERROR_CODE,400).put(Constants.ERROR,"invalid format")).toString());
                }
                else
                {
                    var object = new JsonObject().put(Constants.REQUEST_TYPE,Constants.CREDENTIAL).put(Constants.REQUEST_DATA,request);

                    eventBus.request(Constants.PUT, object, result ->
                    {
                        if(result.succeeded())
                        {
                            message.reply(result.result().body().toString());
                        }
                        else
                        {
                            logger.warn("unable to save credential profiles to DB");

                            message.reply(result.cause().getMessage());
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

        eventBus.localConsumer(Constants.DELETE_CREDENTIAL, message ->
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
                    var object = new JsonObject().put(Constants.REQUEST_TYPE,Constants.CREDENTIAL).put(Constants.REQUEST_DATA,request);

                    eventBus.request(Constants.DELETE, object, result ->
                    {
                        if(result.succeeded())
                        {
                            message.reply(result.result().body().toString());
                        }
                        else
                        {
                            logger.warn("unable to save credential profiles to DB");

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

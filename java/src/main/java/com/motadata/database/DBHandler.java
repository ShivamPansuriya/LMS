package com.motadata.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import com.motadata.utils.Constants;
import com.motadata.utils.IDFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DBHandler extends AbstractVerticle
{
    static final Logger logger = LoggerFactory.getLogger(DBHandler.class);

    IDFactory idFactory = new IDFactory();

    private final Map<Long, JsonObject> credentialProfile = new HashMap<>();

    private final Map<Long, JsonObject> discoveryProfile = new HashMap<>();

    private final Map<Long, Long> validDiscoveryProfile = new HashMap<>();

    private final Map<String, ArrayList<JsonObject>> poolData = new HashMap<>();

    private final Map<Long, Long> provisionTime = new HashMap<>();

    @Override
    public void start(Promise<Void> startPromise)
    {
        vertx.eventBus().localConsumer(Constants.POST, message ->
        {
            var response = new JsonObject();

            try
            {
                var request = new JsonObject(message.body().toString());

                var data = request.getJsonObject(Constants.REQUEST_DATA);

                Long id;

                    switch(request.getString(Constants.REQUEST_TYPE)) // if used DB use switch case to create query
                    {
                        case Constants.CREDENTIAL:
                            id = idFactory.generateID();

                            credentialProfile.put(id, data);

                            response.put(Constants.ID, id);

                            break;

                        case Constants.DISCOVERY:
                            var credentials = data.getJsonArray("credentials");

                            var isValid = true;

                            for(var credential : credentials)
                            {
                                if(!credentialProfile.containsKey(Long.parseLong(credential.toString())))
                                {
                                    isValid = false;

                                    break;
                                }
                            }

                            if(isValid && !credentials.isEmpty())
                            {
                                id = idFactory.generateID();

                                discoveryProfile.put(id, data);

                                response.put(Constants.ID, id);

                            }
                            break;

                        case Constants.SET_PROVISION:
                            id = idFactory.generateID();

                            if(validDiscoveryProfile.containsKey(Long.parseLong(data.getString(Constants.ID))) && !provisionTime.containsKey(Long.parseLong(data.getString(Constants.ID))))
                            {
                                provisionTime.put(Long.parseLong(data.getString(Constants.ID)), 60L);

                                response.put(Constants.ID, id);
                            }
                            else
                            {
                                response.put(Constants.MESSAGE, "Already provisioned or run discovery before provision");
                            }
                            break;

                        case Constants.POOL_DATA:

                            var results = new JsonArray(data.getString("data"));

                            for(var context: results)
                            {
                                var result = new JsonObject(context.toString());

                                if(poolData.containsKey(result.getString(Constants.IP)))
                                {
                                    poolData.get(result.getString(Constants.IP)).add(result);
                                }
                                else
                                {
                                poolData.put(result.getString(Constants.IP), new ArrayList<>());

                                poolData.get(result.getString(Constants.IP)).add(result);
                            }
                            }

                            response.put(Constants.MESSAGE,"poll data saved");

                            break;

                        case Constants.SAVE_DISCOVERY:

                            var discoveryID = Long.parseLong(data.getString("discovery.id"));

                            if(!validDiscoveryProfile.containsKey(discoveryID))
                            {
                                validDiscoveryProfile.put(discoveryID, Long.parseLong(data.getString("credential.id")));
                            }
                            response.put(Constants.MESSAGE, "discovery run successfully");

                            break;
                    }

                    // if used DB first fire query and then send status
                if(!response.isEmpty())
                {
                    response.put(Constants.STATUS, Constants.STATUS_SUCCESS);

                    message.reply(response);

                    logger.info(response.toString());
                }
                else
                {
                    response.put(Constants.STATUS,Constants.STATUS_FAIL);

                    response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "INSERTION ERROR").put(Constants.ERROR_CODE, 502).put(Constants.MESSAGE, String.format("error in saving %s",request.getString(Constants.REQUEST_TYPE))));

                    message.fail(44, response.toString());

                    logger.error(response.toString());
                }
            } catch(Exception exception)
            {
                response.put(Constants.STATUS, Constants.STATUS_FAIL);

                response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, exception).put(Constants.ERROR_CODE, 502).put(Constants.MESSAGE, "error in executing update operation"));

                message.fail(501, response.toString());

                logger.error(exception.getMessage());
            }

        });

        vertx.eventBus().localConsumer(Constants.GET, message ->
        {
            var response = new JsonObject();

            try
            {
                var request = new JsonObject(message.body().toString());

                var data = request.getJsonObject(Constants.REQUEST_DATA);

                switch(request.getString(Constants.REQUEST_TYPE)) // if used DB use switch case to create get query
                {
                    case Constants.CREDENTIAL:
                        for(var key : credentialProfile.keySet())
                        {
                            response.put(key.toString(), credentialProfile.get(key));
                        }
                        break;

                    case Constants.DISCOVERY:
                        for(var key : discoveryProfile.keySet())
                        {
                            response.put(key.toString(), discoveryProfile.get(key));
                        }
                        break;

                    case Constants.CONTEXT:

                        if(discoveryProfile.containsKey(Long.parseLong(data.getString(Constants.ID))))
                        {
                            var contexts = new JsonArray();

                            var profile = discoveryProfile.get(Long.parseLong(data.getString(Constants.ID)));

                            var credentialList = profile.getJsonArray("credentials");

                            var credentials = new JsonArray();

                            for(var id : credentialList)
                            {
                                if(credentialProfile.containsKey(Long.parseLong(id.toString())))
                                {
                                    var credential = credentialProfile.get(Long.parseLong(id.toString()));

                                    credential.put("credential.id", id);

                                    credentials.add(credential);
                                }
                            }

                            contexts.add(new JsonObject().put(Constants.DISCOVERY, profile).put("credentials", credentials));

                            response.put("discovery.data", contexts);
                        }
                        break;

                    case Constants.PROVISION_CONTEXT:
                        var contexts = new JsonArray();

                        var ids = new JsonArray(data.getString(Constants.ID));

                        for(var id : ids)
                        {
                            var profile = new JsonObject();

                            id = Long.parseLong(id.toString());

                            if(validDiscoveryProfile.containsKey(id))
                            {
                                var credentialID = validDiscoveryProfile.get(id);

                                profile.put(Constants.DISCOVERY, discoveryProfile.get(id));

                                profile.put("credentials", new JsonArray().add(credentialProfile.get(credentialID)));

                                contexts.add(profile);
                            }
                        }
                        response.put(Constants.POOL_DATA, contexts);
                        break;

                    case Constants.POOL_TIME:

                        for(var discoveryId : provisionTime.keySet())
                        {
                            var time = provisionTime.get(discoveryId);

                            time -= 10L;

                            if(time <= 0)
                            {
                                provisionTime.put(discoveryId, 60L);
                            }
                            else
                            {
                                provisionTime.put(discoveryId, time);
                            }
                        }

                        response.put(Constants.POOL_TIME, provisionTime);

                        break;

                    case Constants.POOL_DATA:

                        response.put(Constants.POOL_DATA, poolData.get(data.getString(Constants.IP)));

                        break;
                }

                // if used DB first fire query and then send status
                if(!response.isEmpty())
                {
                    response.put(Constants.STATUS, Constants.STATUS_SUCCESS);

                    message.reply(response);
                }
                else
                {
                    response.put(Constants.STATUS,Constants.STATUS_FAIL);

                    response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "GET ERROR").put(Constants.ERROR_CODE, 404).put(Constants.MESSAGE, String.format("error in getting %s",request.getString(Constants.REQUEST_TYPE))));

                    message.fail(404, response.toString());

                    logger.error(response.toString());
                }
            } catch(Exception exception)
            {
                response.put(Constants.STATUS, Constants.STATUS_FAIL);

                response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, exception).put(Constants.ERROR_CODE, 500).put(Constants.MESSAGE, "error in executing GET operation"));

                message.fail(500, response.toString());

                logger.error(exception.getMessage());
            }
        });

        vertx.eventBus().localConsumer(Constants.PUT, message ->
        {
            var response = new JsonObject();

            try
            {
                var request = new JsonObject(message.body().toString());

                var data = request.getJsonObject(Constants.REQUEST_DATA);

                var id = Long.parseLong(data.getString(Constants.ID));

                switch(request.getString(Constants.REQUEST_TYPE)) // if used DB use switch case to create get query
                {
                    case Constants.CREDENTIAL:

                        if(credentialProfile.containsKey(id))
                        {
                            credentialProfile.put(id, new JsonObject().put(Constants.HOSTNAME, data.getString(Constants.HOSTNAME)).put(Constants.PASSWORD, data.getString(Constants.PASSWORD)));

                            response.put(Constants.MESSAGE, "Credential profile updated successfully");
                        }
                        break;

                    case Constants.DISCOVERY:

                        if(discoveryProfile.containsKey(id))
                        {
                            var profile = discoveryProfile.get(id);

                            profile.put(Constants.IP, data.getString(Constants.IP)).put(Constants.PORT, data.getString(Constants.PORT));

                            response.put(Constants.MESSAGE, "Discovery profile updated successfully");
                        }
                        break;
                }

                // if used DB first fire query and then send status
                if(!response.isEmpty())
                {
                    response.put(Constants.STATUS, Constants.STATUS_SUCCESS);

                    message.reply(response);

                    logger.info(response.toString());
                }
                else
                {
                    response.put(Constants.STATUS, Constants.STATUS_FAIL);

                    response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "No such profile").put(Constants.ERROR_CODE, 409).put(Constants.MESSAGE, String.format("unable to update %s profile", request.getString(Constants.REQUEST_TYPE))));

                    message.fail(409, response.toString());

                    logger.error(response.toString());
                }
            } catch(Exception exception)
            {
                response.put(Constants.STATUS, Constants.STATUS_FAIL);

                response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, exception.getCause().getMessage()).put(Constants.ERROR_CODE, 500).put(Constants.MESSAGE, "error in executing update operation"));

                message.fail(500, response.toString());

                logger.error(exception.getMessage());
            }
        });

        vertx.eventBus().localConsumer(Constants.DELETE, message ->
        {
            var response = new JsonObject();

            try
            {
                var request = new JsonObject(message.body().toString());

                var data = request.getJsonObject(Constants.REQUEST_DATA);

                var id = Long.parseLong(data.getString(Constants.ID));

                switch(request.getString(Constants.REQUEST_TYPE)) // if used DB use switch case to create get query
                {
                    case Constants.CREDENTIAL:

                        if(!validDiscoveryProfile.containsValue(id) && credentialProfile.containsKey(id))
                        {
                            credentialProfile.remove(id);

                            response.put(Constants.MESSAGE, "Credential profile deleted successfully");
                        }
                        break;

                    case Constants.DISCOVERY:

                        if(!validDiscoveryProfile.containsValue(id) && discoveryProfile.containsKey(id))
                        {
                            discoveryProfile.remove(id);

                            response.put(Constants.MESSAGE, "Credential profile deleted successfully");
                        }
                        break;

                }

                // if used DB first fire query and then send status
                if(!response.isEmpty())
                {
                    response.put(Constants.STATUS, Constants.STATUS_SUCCESS);

                    message.reply(response);

                    logger.info(response.toString());
                }
                else
                {
                    response.put(Constants.STATUS, Constants.STATUS_FAIL);

                    response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR,String.format( "This %s profile is currently provisioned",request.getString(Constants.REQUEST_TYPE))).put(Constants.ERROR_CODE, 501).put(Constants.ERROR_MESSAGE, String.format("unable to delete %s profile", request.getString(Constants.REQUEST_TYPE))));

                    message.fail(501, response.toString());

                    logger.error(response.toString());
                }
            } catch(Exception exception)
            {
                response.put(Constants.STATUS, Constants.STATUS_FAIL);

                response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, exception.getCause().getMessage()).put(Constants.ERROR_CODE, 501).put(Constants.MESSAGE, "error in executing update operation"));

                message.fail(501, response.toString());

                logger.error(exception.getMessage());
            }
        });

        startPromise.complete();
    }
}

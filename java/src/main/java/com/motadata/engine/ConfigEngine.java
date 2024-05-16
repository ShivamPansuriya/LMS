package com.motadata.engine;

import com.motadata.manager.ConfigManager;
import com.motadata.utils.SSHClient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import com.motadata.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConfigEngine extends AbstractVerticle
{
    static final Logger logger = LoggerFactory.getLogger(ConfigEngine.class);

    private final String idSelectQ = "SELECT `id` FROM `data`.`Devices` WHERE `ip_address` = ?;";

    private final String ipInsertQ = "INSERT INTO `data`.`Devices` (`ip_address`) VALUES (?);";

    private final Map<Long, JsonObject> credentialProfile = new HashMap<>();

    private final Map<Long, JsonObject> discoveryProfile = new HashMap<>();

    private final Map<Long, Long> validDiscoveryProfile = new HashMap<>();

    private final Map<String, ArrayList<JsonObject>> poolData = new HashMap<>();

    private final Map<Long, Integer> provisionTime = new HashMap<>();

    private final Map<Long,Long> provisionList = new HashMap<>();

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

                    switch(request.getString(Constants.REQUEST_TYPE))
                    {
                        case Constants.CREDENTIAL:

                            id = ConfigManager.generateID();

                            credentialProfile.put(id, data);

                            response.put(Constants.ID, id);

                            logger.debug("new credential profile added {}",response);

                            break;

                        case Constants.DISCOVERY:
                            var credentials = data.getJsonArray("credentials");

                            var isValid = true;

                            for(var credential : credentials)
                            {
                                if(!credentialProfile.containsKey(Long.parseLong(credential.toString())))
                                {
                                    isValid = false;

                                    logger.debug("credential not matched with existing data");

                                    response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "INVALID DATA").put(Constants.ERROR_CODE, 400).put(Constants.MESSAGE, String.format("error in saving %s, credentials do not match",request.getString(Constants.REQUEST_TYPE))));

                                    break;
                                }
                            }

                            if(isValid && !credentials.isEmpty())
                            {
                                id = ConfigManager.generateID();

                                discoveryProfile.put(id, data);

                                response.put(Constants.ID, id);

                                logger.debug("new discovery profile added {}",response);
                            }

                            break;

                        case Constants.PROVISION:
                            id = ConfigManager.generateID();

                            if(validDiscoveryProfile.containsKey(Long.parseLong(data.getString(Constants.ID))) && !provisionTime.containsKey(Long.parseLong(data.getString(Constants.ID))))
                            {
                                provisionTime.put(Long.parseLong(data.getString(Constants.ID)), 60);

                                provisionList.put(id,Long.parseLong(data.getString(Constants.ID)));

                                response.put(Constants.ID, id);

                                logger.debug("provision started for discovery id {}",data.getString(Constants.ID));
                            }
                            else
                            {
                                response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "PROVISION ERROR").put(Constants.ERROR_CODE, 400).put(Constants.MESSAGE, String.format("error in saving %s, make sure that discovery run is success and not already provisioned",request.getString(Constants.REQUEST_TYPE))));
                            }

                            break;

                        case Constants.POLL_DATA:
//                            var results = new JsonArray(data.getString("data"));
//
//                            for(var context: results)
//                            {
//                                var result = new JsonObject(context.toString());
//
//                                if(poolData.containsKey(result.getString(Constants.IP)))
//                                {
//                                    poolData.get(result.getString(Constants.IP)).add(result);
//                                }
//                                else
//                                {
//                                poolData.put(result.getString(Constants.IP), new ArrayList<>());
//
//                                poolData.get(result.getString(Constants.IP)).add(result);
//                            }
//                            }
//
//                            response.put(Constants.MESSAGE,"poll data saved");
                            var results = new JsonArray(data.getString(Constants.POLL_DATA));

                            for(var context: results)
                            {
                                var result = new JsonObject(context.toString());

                                var deviceID = 1;

                                if(!result.containsKey(Constants.ERROR))
                                {
                                    try(var conn = ConfigManager.getConnection(logger); var ipSelectStmt = conn.prepareStatement(idSelectQ);)
                                    {
                                        ipSelectStmt.setString(1, result.getString(Constants.IP));

                                        var rs = ipSelectStmt.executeQuery();

                                        if(!rs.isBeforeFirst())
                                        {
                                            try(var ipInsertStmt = conn.prepareStatement(ipInsertQ))
                                            {
                                                ipInsertStmt.setString(1, result.getString(Constants.IP));

                                                var rowsInserted = ipInsertStmt.executeUpdate();

                                                if(rowsInserted > 0)
                                                {
                                                    logger.debug("{} rows inserted in Devices table", rowsInserted);
                                                }
                                            }
                                        }
                                        else
                                        {
                                            rs = ipSelectStmt.executeQuery();

                                            if(rs.next())
                                            {
                                                deviceID = rs.getInt(Constants.ID);
                                            }
                                        }
                                    }
                                    try(var conn = ConfigManager.getConnection(logger))
                                    {
                                        var statements = ConfigManager.prepare(conn, result.getJsonObject(Constants.RESULT), deviceID);

                                        for(var statement : statements)
                                        {
                                            var rowsInserted = statement.executeUpdate();

                                            statement.close();
                                        }
                                        logger.debug("rows inserted ip: {}", result.getString(Constants.IP));

                                    }
                                }
                                else
                                {
                                    logger.error(result.getString(Constants.ERROR));
                                }
                            }

                            response.put(Constants.MESSAGE,"poll data saved");

                            break;

                        case Constants.SAVE_DISCOVERY:

                            var discoveryID = Long.parseLong(data.getString("discovery.id"));

                            if(!validDiscoveryProfile.containsKey(discoveryID))
                            {
                                validDiscoveryProfile.put(discoveryID, Long.parseLong(data.getString("credential.id")));

                                logger.debug("value added to valid discovery {}:{}",discoveryID,data.getString("credential.id"));
                            }

                            response.put(Constants.MESSAGE, "discovery run successfully");

                            break;
                    }

                if(!response.isEmpty() && !response.containsKey(Constants.ERROR))
                {
                    response.put(Constants.STATUS, Constants.STATUS_SUCCESS);

                    message.reply(response);
                }
                else
                {
                    response.put(Constants.STATUS,Constants.STATUS_FAIL);

                    message.fail(400, response.toString());
                }
            }
            catch(Exception exception)
            {
                response.put(Constants.STATUS, Constants.STATUS_FAIL);

                response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, exception.getMessage()).put(Constants.ERROR_CODE, 404).put(Constants.MESSAGE, "error in executing INSERT operation"));

                message.fail(500, response.toString());

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

                switch(request.getString(Constants.REQUEST_TYPE))
                {
                    case Constants.CREDENTIAL:
                        for(var key : credentialProfile.keySet())
                        {
                            response.put(key.toString(), credentialProfile.get(key));
                        }

                        if(response.isEmpty())
                            response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "GET").put(Constants.ERROR_CODE, 500).put(Constants.MESSAGE, "No Credential profile is present.Please add credential profile!"));

                        break;

                    case Constants.DISCOVERY:
                        for(var key : discoveryProfile.keySet())
                        {
                            response.put(key.toString(), discoveryProfile.get(key));
                        }

                        if(response.isEmpty())
                            response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "GET").put(Constants.ERROR_CODE, 500).put(Constants.MESSAGE, "No Discovery profile is present.Please add Discovery profile!"));

                        break;

                    case Constants.CONTEXT:

                        if(discoveryProfile.containsKey(Long.parseLong(data.getString(Constants.ID))))
                        {
                            var contexts = new JsonArray();

                            var profile = discoveryProfile.get(Long.parseLong(data.getString(Constants.ID)));

                            if(SSHClient.isAvailable(profile.getString(Constants.IP),logger))
                            {
                                var credentialList = profile.getJsonArray(Constants.CREDENTIALS);

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

                                contexts.add(new JsonObject().put(Constants.DISCOVERY, profile).put(Constants.CREDENTIALS, credentials));
                            }

                            response.put("discovery.data", contexts);

                            logger.debug("discovery run performed for id {}",data.getString(Constants.ID));
                        }
                        else
                        {
                            response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "RUN ERROR").put(Constants.ERROR_CODE, 404).put(Constants.MESSAGE, String.format("no such Discovery profile(id= %s) available", data.getString(Constants.ID))));

                            logger.warn("Request Discovery id {} is not present so can't run discovery",data.getString(Constants.ID));
                        }
                        break;

                    case Constants.PROVISION_CONTEXT:
                        var contexts = new JsonArray();

                        var ids = new JsonArray(data.getString(Constants.ID));

                        for(var id : ids)
                        {
                            var profile = new JsonObject();

                            id = Long.parseLong(id.toString());

                            if(validDiscoveryProfile.containsKey(id) && SSHClient.isAvailable(discoveryProfile.get(id).getString(Constants.IP),logger) )
                            {
                                var credentialID = validDiscoveryProfile.get(id);

                                profile.put(Constants.DISCOVERY, discoveryProfile.get(id));

                                profile.put(Constants.CREDENTIAL, new JsonArray().add(credentialProfile.get(credentialID)));

                                contexts.add(profile);
                            }
                        }

                        response.put(Constants.POLL_DATA, contexts);

                        break;

                    case Constants.POLL_TIME:

                        for(var discoveryId : provisionTime.keySet())
                        {
                            var time = provisionTime.get(discoveryId);

                            time -= 10;

                            if(time <= 0)
                            {
                                provisionTime.put(discoveryId, Constants.POLL_DURATION);
                            }
                            else
                            {
                                provisionTime.put(discoveryId, time);
                            }
                        }

                        response.put(Constants.POLL_TIME, provisionTime);

                        break;

                        // currently not in use
                    case Constants.POLL_DATA:

                        response.put(Constants.POLL_DATA, poolData.get(data.getString(Constants.IP)));

                        if(response.isEmpty())
                            response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "GET").put(Constants.ERROR_CODE, 500).put(Constants.MESSAGE, "unable to fetch data from DB"));

                        break;
                }

                if(!response.isEmpty() && !response.containsKey(Constants.ERROR))
                {
                    response.put(Constants.STATUS, Constants.STATUS_SUCCESS);

                    message.reply(response);
                }
                else
                {
                    response.put(Constants.STATUS, Constants.STATUS_FAIL);

                    message.fail(400, response.toString());
                }
            }
            catch(Exception exception)
            {
                response.put(Constants.STATUS, Constants.STATUS_FAIL);

                response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, exception.getMessage()).put(Constants.ERROR_CODE, 500).put(Constants.MESSAGE, "error in executing GET operation"));

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

                    response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "UPDATE").put(Constants.ERROR_CODE, 400).put(Constants.MESSAGE, String.format("unable to update %s profile, as no such profile exists", request.getString(Constants.REQUEST_TYPE))));

                    message.fail(400, response.toString());

                    logger.error(response.toString());
                }
            }
            catch(Exception exception)
            {
                response.put(Constants.STATUS, Constants.STATUS_FAIL);

                response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, exception.getMessage()).put(Constants.ERROR_CODE, 500).put(Constants.MESSAGE, "error in executing UPDATE operation"));

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

                            logger.info("credential profile {} deleted",id);
                        }
                        else
                        {
                            response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, String.format("credential profile %s is currently provisioned", request.getString(Constants.REQUEST_TYPE))).put(Constants.ERROR_CODE, 400).put(Constants.ERROR_MESSAGE, String.format("unable to delete profile id: %s", request.getString(Constants.REQUEST_TYPE))));

                            logger.debug("unable to delete credential profile {} because is provisioned",id);

                        }
                        break;

                    case Constants.DISCOVERY:

                        if(!validDiscoveryProfile.containsValue(id) && discoveryProfile.containsKey(id))
                        {
                            discoveryProfile.remove(id);

                            response.put(Constants.MESSAGE, "Discovery profile deleted successfully");

                            logger.info("discovery profile {} deleted",id);

                        }
                        else
                        {
                            response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, String.format("Discovery profile %s is currently provisioned", request.getString(Constants.REQUEST_TYPE))).put(Constants.ERROR_CODE, 400).put(Constants.ERROR_MESSAGE, String.format("unable to delete profile id: %s", request.getString(Constants.REQUEST_TYPE))));

                            logger.debug("unable to delete discovery profile {} because is provisioned",id);

                        }
                        break;

                    case Constants.PROVISION:

                        if(provisionList.containsKey(id))
                        {
                            provisionTime.remove(provisionList.get(id));

                            provisionList.remove(id);

                            response.put(Constants.MESSAGE, "Un-provision successfully");

                            logger.info("provision device {} is unprovisioned",id);

                        }
                        else
                        {
                            response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR,"already un-provisioned").put(Constants.ERROR_CODE, 400).put(Constants.ERROR_MESSAGE, String.format("Request device id: %s is not provisioned",id)));
                        }
                        break;

                }

                // if used DB first fire query and then send status
                if(!response.isEmpty() && !response.containsKey(Constants.ERROR))
                {
                    response.put(Constants.STATUS, Constants.STATUS_SUCCESS);

                    message.reply(response);
                }
                else
                {
                    response.put(Constants.STATUS, Constants.STATUS_FAIL);

                    message.fail(400, response.toString());

                    logger.error(response.toString());
                }
            }
            catch(Exception exception)
            {
                response.put(Constants.STATUS, Constants.STATUS_FAIL);

                response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, exception.getMessage()).put(Constants.ERROR_CODE, 500).put(Constants.MESSAGE, "error in executing DELETE operation"));

                message.fail(500, response.toString());

                logger.error(exception.getMessage());
            }
        });

        startPromise.complete();
    }
}

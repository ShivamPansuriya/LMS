package org.example.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.example.utils.Constants;
import org.example.utils.IDFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DBHandler extends AbstractVerticle
{
    IDFactory idFactory = new IDFactory();

    private final Map<Long, JsonObject> credentialProfile = new HashMap<>();

    private final Map<Long, JsonObject> discoveryProfile = new HashMap<>();

    private final Map<Long, Long> validDiscoveryProfile = new HashMap<>();

    private final Map<String, ArrayList<JsonObject>> poolData = new HashMap<>();

    private final Map<Long, Long> provisionList = new HashMap<>();

    @Override
    public void start()
    {
        vertx.eventBus().localConsumer("insert", message ->
        {
            var response = new JsonObject();

            var id = idFactory.generateID();

            var token = message.body().toString().split(Constants.MESSAGE_SEPARATOR);

            if(token.length < 2)
            {
                response.put("status", "fail");
            }
            else
            {
                switch(token[0]) // if used DB use switch case to create query
                {
                    case "credential":
                        credentialProfile.put(id, new JsonObject(token[1]));

                        response.put("credential profile id", id);

                        break;

                    case "discovery":
                        discoveryProfile.put(id, new JsonObject(token[1]));

                        response.put("discovery profile id", id);

                        break;

                    case "setProvision":
                        provisionList.put(Long.parseLong(token[1]), 60L);

                        response.put("provision id", id);

                        break;

                    case "contextResult":

                        var results = new JsonArray(token[1]);

                        var result = results.getJsonObject(0);

                        if(poolData.containsKey(result.getString("object.ip")))
                        {
                           poolData.get(result.getString("object.ip")).add(result);
                        }
                        else
                        {
                            poolData.put(result.getString("object.ip"), new ArrayList<>());
                        }

                        response.put("discovery profile id", id);

                        break;

                    case Constants.CREATE_DISCOVERY:

                        var discoveryID = Long.parseLong(token[1]);

                        if(!validDiscoveryProfile.containsKey(discoveryID))
                        {
                            validDiscoveryProfile.put(discoveryID, Long.parseLong(token[2]));
                        }
                        response.put("message","discovery run successfully");

                        break;
                }

                // if used DB first fire query and then send status

            }
            if(response != null)
            {
                response.put("status", "success");
            }
            else
            {
                response.put("status", "fail");
            }

            message.reply(response);
        });

        vertx.eventBus().localConsumer("get", message ->
        {
//            try
//            {
                var identifier = message.body().toString().split(Constants.MESSAGE_SEPARATOR);

                var object = new JsonObject();

                switch(identifier[0]) // if used DB use switch case to create get query
                {
                    case "credentialList":
                        for(var key : credentialProfile.keySet())
                        {
                            object.put(key.toString(), credentialProfile.get(key));
                        }
                        break;

                    case "discovery":
                        for(var key : discoveryProfile.keySet())
                        {
                            object.put(key.toString(), discoveryProfile.get(key));
                        }
                        break;

                    case "getContext":

                        var contexts = new JsonArray();

                        var profile = discoveryProfile.get(Long.parseLong(identifier[1]));

                        var credentialList = profile.getJsonArray("credentials");

                        var credentials = new JsonArray();

                        for(var id : credentialList)
                        {
                            var credential = credentialProfile.get(Long.parseLong(id.toString()));

                            credential.put("credential.id",id);

                            credentials.add(credential);
                        }

                        contexts.add(new JsonObject().put("discovery", profile).put("credentials", credentials));

                        object.put("discovery.data", contexts);

                        break;

                    case "getProvisionContext":
                        contexts = new JsonArray();

                        var ids = new JsonArray(identifier[1]);

                        for(var id : ids)
                        {
                            profile = new JsonObject();

                            id = Long.parseLong(id.toString());

                            if(validDiscoveryProfile.containsKey(id))
                            {
                                var credentialID = validDiscoveryProfile.get(id);

                                profile.put("discovery", discoveryProfile.get(id));

                                profile.put("credentials", new JsonArray().add(credentialProfile.get(credentialID)));

                                contexts.add(profile);
                            }
                        }
                        object.put("poll.data",contexts);
                        break;

                    case "getPoolingTime":

                        for(var discoveryId : provisionList.keySet())
                        {
                            var time = provisionList.get(discoveryId);

                            time -= 10L;

                            if(time<=0)
                            {
                                provisionList.put(discoveryId,60L);
                            }
                            else
                            {
                                provisionList.put(discoveryId,time);
                            }
                        }

                        object.put("polling.time.map",provisionList);

                        break;

                    case "getData":

                        object.put("data",poolData.get(identifier[1]));

                        break;
                }

                // if used DB first fire query and then send status
                if(!object.isEmpty())
                {
                    object.put("status", "success");
                }
                else
                {
                    object.put("status", "fail");

                    object.put("message", String.format("There is no %s profile", identifier[0]));
                }
                message.reply(object);
//            }
        });

        vertx.eventBus().localConsumer("put", message ->
        {

            var identifier = message.body().toString().split(Constants.MESSAGE_SEPARATOR);

            var profileObject = new JsonObject();

            switch(identifier[0]) // if used DB use switch case to create get query
            {

            }

            // if used DB first fire query and then send status
            if(!profileObject.isEmpty())
            {
                profileObject.put("status", "success");
            }
            else
            {
                profileObject.put("status", "fail");

                profileObject.put("message", String.format("There is no %s profile", identifier[0]));
            }
            message.reply(profileObject);
            //            }
        });

    }
}

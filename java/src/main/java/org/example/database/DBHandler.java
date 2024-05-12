package org.example.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.example.utils.Constants;
import org.example.utils.IDFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class DBHandler extends AbstractVerticle
{
    IDFactory idFactory = new IDFactory();

    private final Map<Long, JsonObject> credentialProfile = new HashMap<>();

    private final Map<Long, JsonObject> discoveryProfile = new HashMap<>();

    @Override
    public void start() throws Exception
    {
        vertx.eventBus().localConsumer("insert", message -> {

            var responseJson = new JsonObject();

            var profileID = idFactory.generateID();

            var identifier = message.body().toString().split(Constants.MESSAGE_SEPARATOR);

            if(identifier.length < 2)
            {
                responseJson.put("status", "fail");
            }
            else
            {
                switch(identifier[0]) // if used DB use switch case to create query
                {
                    case "credential":
                        credentialProfile.put(profileID, new JsonObject(identifier[1]));

                        responseJson.put("credential profile id", profileID);

                    case "discovery":
                        discoveryProfile.put(profileID, new JsonObject(identifier[1]));

                        responseJson.put("discovery profile id", profileID);
                }

                // if used DB first fire query and then send status

            }
            if(responseJson != null)
            {
                responseJson.put("status", "success");
            }
            else
            {
                responseJson.put("status", "fail");
            }

            message.reply(responseJson);
        });

        vertx.eventBus().localConsumer("get", message -> {
            try
            {
                var identifier = message.body().toString().split(Constants.MESSAGE_SEPARATOR);

                var profileObject = new JsonObject();

                switch(identifier[0]) // if used DB use switch case to create get query
                {
                    case "credential":
                        for(var key : credentialProfile.keySet())
                        {
                            profileObject.put(key.toString(), credentialProfile.get(key));
                        }

                    case "discovery":
                        for(var key : discoveryProfile.keySet())
                        {
                            profileObject.put(key.toString(), discoveryProfile.get(key));
                        }

                    case "getContext":
                        var profile = discoveryProfile.get(Long.parseLong(identifier[1]));

                        var credential = profile.getJsonArray("credential");

                        var credentials = new JsonArray();

                        for(var id: credential){
                            credentials.add(credentialProfile.get(Long.parseLong(id.toString())));
                        }

                        profileObject.put("discovery",profile);

                        profileObject.put("credentials",credentials);
                }

                // if used DB first fire query and then send status
                if(!profileObject.isEmpty())
                {
                    profileObject.put("status", "success");
                }
                else
                {
                    var statusMessage = String.format("There is no %s profile", identifier[0]);

                    profileObject.put("status", "fail");

                    profileObject.put("message", statusMessage);
                }
                message.reply(profileObject);

            } catch(NumberFormatException e)
            {
                // Handle if the string cannot be parsed as a long

                //                System.err.println("Error: The string is not a valid long format.");
            }
        });


    }
}

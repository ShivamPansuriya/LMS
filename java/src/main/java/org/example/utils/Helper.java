package org.example.utils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;

public class Helper
{
    public JsonArray createContext(JsonArray profileList, String requestType, Logger logger)
    {
        var contextArray = new JsonArray();

        try
        {
            for(var profile: profileList)
            {
                var discoveryCredential = new JsonObject(profile.toString());

                var context = new JsonObject();

                var discoveryProfile = discoveryCredential.getJsonObject("discovery");

                context.put("request.type", requestType);

                context.put("device.type", "linux");

                context.put("object.port", Integer.parseInt(discoveryProfile.getString("object.port")));

                context.put("object.ip", discoveryProfile.getString("object.ip"));

                context.put("credentials", discoveryCredential.getJsonArray("credentials"));

                contextArray.add(context);
            }

        }
        catch(Exception exception)
        {
            logger.error(String.format("null pointer exception: %s",exception));
        }
        return contextArray;
    }
}

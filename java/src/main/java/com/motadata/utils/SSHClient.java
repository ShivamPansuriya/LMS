package com.motadata.utils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SSHClient
{
    private SSHClient(){}
    public static JsonArray createContext(JsonArray profileList, String requestType, Logger logger)
    {
        var contextArray = new JsonArray();

        try
        {
            for(var profile: profileList)
            {
                var discoveryCredential = new JsonObject(profile.toString());

                var context = new JsonObject();

                var discoveryProfile = discoveryCredential.getJsonObject(Constants.DISCOVERY);

                context.put(Constants.REQUEST_TYPE, requestType);

                context.put("device.type", "linux");

                context.put(Constants.PORT, Integer.parseInt(discoveryProfile.getString(Constants.PORT)));

                context.put(Constants.IP, discoveryProfile.getString(Constants.IP));

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

    public static boolean isAvailable(String objectIP, Logger logger)
    {
        try
        {
            var processBuilder = new ProcessBuilder("fping", objectIP, "-c3", "-q");

            processBuilder.redirectErrorStream(true);

            var process = processBuilder.start();

            // Read the output of the command
            var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;

            while((line = reader.readLine()) != null)
            {
                if(line.contains("/0%"))
                {
                    return true;
                }
            }
        } catch(Exception exception)
        {
            logger.error(String.format("null pointer exception: %s",exception));
        }
        return false;
    }
}

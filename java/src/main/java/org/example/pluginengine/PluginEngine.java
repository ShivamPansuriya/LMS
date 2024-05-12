package org.example.pluginengine;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.example.utils.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class PluginEngine extends AbstractVerticle
{
    @Override
    public void start() throws Exception
    {
        vertx.eventBus().localConsumer("run.engine",message ->
        {
            var identifier = message.body().toString().split(Constants.MESSAGE_SEPARATOR);

            var ms = "getContext"+Constants.MESSAGE_SEPARATOR +message.body().toString();

            var contextArray = new JsonArray();


            var context = new JsonObject();

            vertx.eventBus().request("get",ms,reply->
            {
                if(reply.succeeded())
                {
                    var discoveryCredential = new JsonObject(reply.result().body().toString());

                    context.put("request.type", identifier[0]);

                    context.put("device.type", "linux");

                    context.put("object.port", discoveryCredential.getString("object.port"));

                    context.put("object.ip", discoveryCredential.getString("object.ip"));

                    for(var credential : discoveryCredential.getJsonArray("credentials"))
                    {
                        var credentialJson = new JsonObject(credential.toString());

                        context.put("object.password", credentialJson.getString("object.password"));

                        context.put("object.host", credentialJson.getString("object.host"));
                    }

                    contextArray.add(context);
                }
            });
            var result = runContext(contextArray.toString());

            message.reply(result);
        });
    }

    private JsonArray runContext(String context){

        try {

            String encodedString = Base64.getEncoder().encodeToString(context.getBytes());

            ProcessBuilder processBuilder = new ProcessBuilder("/home/shivam/motadata-lite/motadata-lite", encodedString);

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // Read the output of the command
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            var buffer = Buffer.buffer();

            while ((line = reader.readLine()) != null) {
                buffer.appendString(line);
            }

            byte[] decodedBytes = Base64.getDecoder().decode(buffer.toString());

            // Convert the byte array to a string
            String decodedString = new String(decodedBytes);

            var result = new JsonArray(decodedString);

            return result;

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

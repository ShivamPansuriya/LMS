package org.example.pluginengine;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.example.discovery.Discovery;
import org.example.utils.Constants;
import org.example.utils.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class PluginEngine extends AbstractVerticle
{
    static final Logger logger = LoggerFactory.getLogger(PluginEngine.class);

    @Override
    public void start()
    {
        vertx.eventBus().localConsumer("run.engine",message ->
        {
            try
            {

                var encodedString = Base64.getEncoder().encodeToString(message.body().toString().getBytes());

                var processBuilder = new ProcessBuilder("/home/shivam/motadata-lite/motadata-lite", encodedString);

                processBuilder.redirectErrorStream(true);

                var process = processBuilder.start();

                // Read the output of the command
                var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;

                var buffer = Buffer.buffer();

                while ((line = reader.readLine()) != null)
                {
                    buffer.appendString(line);
                }

                byte[] decodedBytes = Base64.getDecoder().decode(buffer.toString());

                // Convert the byte array to a string
                var decodedString = new String(decodedBytes);

                message.reply(decodedString);

            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }

        });


    }

}

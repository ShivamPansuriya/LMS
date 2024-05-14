package com.motadata.pluginengine;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import com.motadata.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Base64;

public class PluginEngine extends AbstractVerticle
{
    static final Logger logger = LoggerFactory.getLogger(PluginEngine.class);

    @Override
    public void start(Promise<Void> startPromise)
    {
        var eventBus = vertx.eventBus();

        eventBus.localConsumer(Constants.RUN,message ->
        {
            try
            {

                var encodedString = Base64.getEncoder().encodeToString(message.body().toString().getBytes());

                var currentDir = System.getProperty("user.dir");

                var processBuilder = new ProcessBuilder(currentDir + Constants.PLUGIN_APPLICATION_PATH, encodedString);

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
                message.fail(501,new JsonObject().put(Constants.STATUS,Constants.STATUS_FAIL).put(Constants.ERROR,new JsonObject().put(Constants.ERROR,exception.toString()).put(Constants.ERROR_CODE,501).put(Constants.ERROR_MESSAGE,"unable to run plugin engine")).toString());

                logger.error(exception.toString());
            }

        });

        startPromise.complete();

    }

}

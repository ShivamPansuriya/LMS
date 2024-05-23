package com.motadata.utils;

import com.motadata.constants.Constants;
import com.motadata.engine.Worker;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class ProcessBuilder
{
    private ProcessBuilder()
    {}

    static final Logger logger = LoggerFactory.getLogger(Worker.class);

    public static boolean available(String ip)
    {
        try
        {
            var processBuilder = new java.lang.ProcessBuilder("fping", ip, "-c3", "-q");

            processBuilder.redirectErrorStream(true);

            var process = processBuilder.start();

            var isCompleted = process.waitFor(5, TimeUnit.SECONDS); // Wait for 5 seconds

            if(!isCompleted)
            {
                process.destroyForcibly();

                logger.error("fping process killed for ip:{} due to timeout limit",ip);
            }
            else
            {
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
            }
        }
        catch(Exception exception)
        {
            logger.error("exception: {}",exception.getMessage());
        }

        logger.warn("device {} is not reachable",ip);

        return false;
    }

    public static JsonArray spawnProcess(JsonArray context)
    {
        try
        {
            var encodedString = Base64.getEncoder().encodeToString(context.toString().getBytes());

            var currentDir = System.getProperty("user.dir");

            var processBuilder = new java.lang.ProcessBuilder(currentDir + Constants.PLUGIN_APPLICATION_PATH, encodedString);

            processBuilder.redirectErrorStream(true);

            var process = processBuilder.start();

            var isCompleted = process.waitFor(Config.PROCESS_TIMEOUT, TimeUnit.SECONDS); // Wait for 60 seconds

            if(!isCompleted)
            {
                process.destroyForcibly();

                logger.error("plugin process killed for request:{} due to timeout limit",context);

                return new JsonArray();
            }
            else
            {
                // Read the output of the command
                var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;

                var buffer = Buffer.buffer();

                while((line = reader.readLine()) != null)
                {
                    buffer.appendString(line);
                }

                var decodedBytes = Base64.getDecoder().decode(buffer.toString());

                // Convert the byte array to a string
                return new JsonArray(new String(decodedBytes));
            }
        }
        catch (Exception exception)
        {
            logger.error(exception.toString());

            return new JsonArray();
        }
    }
}

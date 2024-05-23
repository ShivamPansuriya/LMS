package com.motadata.utils;

import com.motadata.constants.Constants;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;

public class Config
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    private Config()
    {}

    public static int PORT;

    public static String HOST;

    public static long SSH_TIMEOUT;

    public static int PROCESS_TIMEOUT;

    public static int POLL_TIME;

    public static int ZMQ_PORT;

    static
    {
        var config = loadConfig();

        HOST = config.getString(Constants.HOST_IP,"localhost");

        PORT = config.getInteger(Constants.HOST_PORT,8000);

        SSH_TIMEOUT = Math.max(15L,Math.min(config.getLong(Constants.SSH_TIMEOUT,60L) , 140L));

        PROCESS_TIMEOUT = Math.max(35,Math.min(config.getInteger(Constants.PROCESS_TIMEOUT,60) , 60));

        POLL_TIME = config.getInteger(Constants.POLL_TIME,60);

        ZMQ_PORT = config.getInteger(Constants.ZMQ_PORT,9090);

    }

    private static JsonObject loadConfig()
    {
        try(var inputStream = new FileInputStream("config.json"))
        {
            var buffer = inputStream.readAllBytes();

            var data = new String(buffer);

            return new JsonObject(data);

        }
        catch(Exception exception)
        {
            LOGGER.error("Error reading configuration file: ",exception);

            return new JsonObject();
        }
    }

}
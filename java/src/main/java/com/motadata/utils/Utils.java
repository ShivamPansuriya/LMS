package com.motadata.utils;

import com.motadata.constants.Constants;
import com.motadata.engine.Worker;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class Utils
{
    private static final AtomicLong counter = new AtomicLong(0);

    public static long generateID()
    {
        return counter.getAndIncrement();
    }

    public static Future<Void> writeToFile(Vertx vertx, JsonObject data)
    {
        Promise<Void> promise = Promise.promise();

        var ip = data.getString(Constants.IP);

        var now = LocalDateTime.now();

        data.put(Constants.TIMESTAMP, now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        var fileName = ip + ".txt";

        var buffer = Buffer.buffer(data.encodePrettily()).appendString(",\n");

        vertx.fileSystem().openBlocking(fileName,new OpenOptions().setAppend(true).setCreate(true)).write(buffer).onComplete(handler->
        {
            promise.complete();

        }).onFailure(handler->
        {
            promise.fail(handler.getCause());
        });

        return promise.future();
    }

    public static JsonArray createContext(JsonArray contexts, String requestType, String device)
    {
        var contextArray = new JsonArray();

        try
        {
            for(var object: contexts)
            {
                var context = new JsonObject(object.toString());

                var resultContext = new JsonObject();

//                var discoveryProfile = context.getJsonObject(Constants.RESULT);

                resultContext.put(Constants.REQUEST_TYPE, requestType);

                resultContext.put(Constants.DEVICE_TYPE, device);

                resultContext.put(Constants.PORT, Long.parseLong(context.getString(Constants.PORT)));

                resultContext.put(Constants.IP, context.getString(Constants.IP));

                if(requestType.equals(Constants.DISCOVERY))
                {
                    resultContext.put(Constants.CREDENTIALS, context.getJsonArray(Constants.CREDENTIALS));
                }
                else
                {
                    resultContext.put(Constants.CREDENTIAL, context.getJsonObject(Constants.CREDENTIAL));
                }

                resultContext.put(Constants.TIMEOUT, Config.SSH_TIMEOUT);

                resultContext.put(Constants.DISCOVERY_ID, context.getString(Constants.DISCOVERY_ID));

                contextArray.add(resultContext);
            }

            return contextArray;
        }
        catch(Exception exception)
        {
            return contextArray.clear();
        }
    }

    public static JsonObject createContext(JsonObject result)
    {
        var credential = new JsonObject();

        for(var ids : result.getJsonArray(Constants.CREDENTIALS))
        {
            if(new JsonObject(ids.toString()).getString(Constants.CREDENTIAL_ID).equals(result.getString(Constants.CREDENTIAL_PROFILE_ID)))
            {
                credential = new JsonObject(ids.toString());

                break;
            }
        }

        return new JsonObject()
                .put(Constants.DEVICE_TYPE, result.getString(Constants.DEVICE_TYPE))
                .put(Constants.REQUEST_TYPE, Constants.COLLECT)
                .put(Constants.IP, result.getString(Constants.IP))
                .put(Constants.PORT, Long.parseLong(result.getString(Constants.PORT)))
                .put(Constants.DISCOVERY_ID,result.getString(Constants.DISCOVERY_ID))
                .put(Constants.TIMEOUT,Integer.parseInt(result.getString(Constants.TIMEOUT)))
                .put(Constants.CREDENTIAL, credential);
    }
}

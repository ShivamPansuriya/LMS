package com.motadata.engine;

import com.motadata.utils.Config;
import com.motadata.utils.Utils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import java.util.Base64;

public class AgentHandler extends AbstractVerticle
{
    static final Logger logger = LoggerFactory.getLogger(AgentHandler.class);
    private final ZContext context = new ZContext();

    private final ZMQ.Socket socket = context.createSocket(SocketType.PULL);

    @Override
    public void start()
    {
        socket.bind("tcp://*:"+ Config.ZMQ_PORT);

        vertx.setPeriodic(10*1000, timerId ->
        {
            while(true)
            {
                var request = socket.recvStr(ZMQ.DONTWAIT);

                if(request != null)
                {
                    var decodedBytes = Base64.getDecoder().decode(request);

                    var results = new JsonArray(new String(decodedBytes));

                    for(var object : results)
                    {
                        var result = new JsonObject(object.toString());

                        vertx.executeBlocking(handler -> {
                            Utils.writeToFile(vertx, result).onSuccess(asyncHandler ->

                                            logger.info("content is added to file: {}", result))

                                    .onFailure(asyncHandler ->

                                            logger.error("error in writing file: {}", asyncHandler.getMessage()));
                        }, false);
                    }
                }
                else
                {
                    break;
                }
            }
        });


    }


}

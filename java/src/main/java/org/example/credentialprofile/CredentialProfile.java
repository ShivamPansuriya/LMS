package org.example.credentialprofile;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import org.example.utils.Constants;

public class CredentialProfile extends AbstractVerticle
{
    @Override
    public void start()
    {
        vertx.eventBus().localConsumer(Constants.CREATE_PROFILE, message ->
        {
            try
            {
                var request = new JsonObject(message.body().toString());

                if(!request.containsKey("object.host") || !request.containsKey("object.password"))
                {
                    message.fail(400,new JsonObject().put(Constants.STATUS,Constants.STATUS_FAIL).put("message","please entre valid fields").toString());
                }
                else
                {
                    var ms = "credential" + Constants.MESSAGE_SEPARATOR + request;

                    vertx.eventBus().request("insert", ms, reply ->
                    {
                        if(reply.succeeded())
                        {
                            message.reply(reply.result().body().toString());
                        }
                        else
                        {
                            // TODO
                        }
                    });
                }
            }
            catch(Exception exception)
            {
                message.fail(400,new JsonObject().put(Constants.STATUS,Constants.STATUS_FAIL).put("message","please entre valid json format").toString());
            }
        });

        vertx.eventBus().localConsumer(Constants.GET_PROFILE, message ->
        {
            vertx.eventBus().request("get","credential" , msg ->
            {
                message.reply(msg.result().body().toString());
            });
        });
    }
}

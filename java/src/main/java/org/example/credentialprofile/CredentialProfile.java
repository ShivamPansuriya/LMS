package org.example.credentialprofile;

import io.vertx.core.AbstractVerticle;
import org.example.utils.Constants;

public class CredentialProfile extends AbstractVerticle
{
    @Override
    public void start() throws Exception
    {
        vertx.eventBus().localConsumer(Constants.CREATE_PROFILE, message ->
        {
            var ms = "credential"+Constants.MESSAGE_SEPARATOR +message.body().toString();

            vertx.eventBus().request("insert",ms , msg ->
            {
                if(msg.succeeded())
                {
                    message.reply(msg.result().body().toString());
                }
            });
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

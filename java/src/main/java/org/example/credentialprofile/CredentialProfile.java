package org.example.credentialprofile;

import io.vertx.core.AbstractVerticle;
import org.example.utils.Constants;

public class CredentialProfile extends AbstractVerticle
{
    @Override
    public void start() throws Exception
    {
        vertx.eventBus().localConsumer(Constants.CREATE_PROFILE, message -> {
            var ms = "profile"+message.body().toString();

            vertx.eventBus().request("insert",ms , msg ->{
                message.reply(msg.result());
            });
        });
    }
}

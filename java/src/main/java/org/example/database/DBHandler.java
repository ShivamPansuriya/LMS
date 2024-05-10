package org.example.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import org.example.utils.Constants;
import org.example.utils.IDFactory;

import java.util.HashMap;
import java.util.Map;

public class DBHandler extends AbstractVerticle
{
    IDFactory idFactory = new IDFactory();

    Map<Long, JsonObject> credentialProfile = new HashMap<>();

    @Override
    public void start() throws Exception
    {
        vertx.eventBus().localConsumer("insert", message -> {

            var profileID = idFactory.generateID();

            var identifier = message.body().toString().split(Constants.MESSAGE_SEPRATOR);

            switch(identifier[0]) // if used DB use switch case to create query
            {
                case "profile":
                    credentialProfile.put(profileID, new JsonObject(identifier[1]));

            }

            // if used DB first fire query and then send status
            message.reply("success");
        });

        vertx.eventBus().localConsumer("get", message -> {
            try
            {

                var identifier = message.body().toString().split(Constants.MESSAGE_SEPRATOR);

                var profileID = Long.parseLong(identifier[1]);

                JsonObject profileObject = new JsonObject();

                switch(identifier[0]) // if used DB use switch case to create query
                {
                    case "profile":
                        profileObject = credentialProfile.get(profileID);

                }

                // if used DB first fire query and then send status
                if(profileObject != null)
                {
                    message.reply(profileObject);
                }
                else {
                    message.reply(String.format("Enter valid %s ID",identifier[0]));
                }

            } catch(NumberFormatException e)
            {
                // Handle if the string cannot be parsed as a long

                //                System.err.println("Error: The string is not a valid long format.");
            }
        });


    }
}

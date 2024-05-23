package com.motadata.database;

import com.motadata.constants.Constants;
import com.motadata.utils.Utils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractConfigDB {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractConfigDB.class);

    protected final ConcurrentMap<Long, JsonObject> profiles = new ConcurrentHashMap<>();

    protected static final ConcurrentMap<Long, JsonObject> validProfiles = new ConcurrentHashMap<>();

    public long create(JsonObject data)
    {
        long id = Utils.generateID();

        profiles.put(id, data);

        logger.info("Data added to database with id {}", id);

        return id;
    }

    public void create(Long id, JsonObject data)
    {
        validProfiles.put(id, data);

        logger.info("Data added to database with id {}", id);
    }

    public JsonArray getAll(String type)
    {
        JsonArray result = new JsonArray();

        profiles.forEach((id, profile) ->
        {
            switch(type)
            {
                case Constants.PROFILES ->
                {
                    result.add(profile.copy().put(Constants.ID, id));
                }
                case Constants.VALID_PROFILES -> // this will return list of provisioned devices
                {
                    result.add(new JsonObject()
                            .put(Constants.PROVISION_ID, id)
                            .put(Constants.IP, profile.getString(Constants.IP))
                            .put(Constants.DEVICE_TYPE, profile.getString(Constants.DEVICE_TYPE)));
                }
            }
        });

        logger.debug("Get all data has been called for {}", type);

        return result;
    }

    public JsonObject get(Long id, String name)
    {
        var result = new JsonObject();

        switch(name)
        {
            case Constants.PROFILES->
            {
                logger.debug("Get has been called for id {} and type {}", id, name);

                result =  profiles.getOrDefault(id, new JsonObject()).copy();
            }

            case Constants.VALID_PROFILES->
            {
                logger.debug("Get has been called for id {} and type {}", id, name);

                result =  validProfiles.getOrDefault(id, new JsonObject()).copy();
            }
        }
        return result;
    }

    public void update(JsonObject profile, long id, String name)
    {
        switch(name)
        {
            case Constants.PROFILES->
            {
                profiles.replace(id, profile);

                logger.debug("profile {} updated", id);
            }

            case Constants.VALID_PROFILES->
            {
                validProfiles.replace(id, profile);

                logger.debug("valid profile {} updated", id);
            }

        }
    }

    public void delete(long id, String name)
    {
        switch(name)
        {
            case Constants.PROFILES->
            {
                profiles.remove(id);

                logger.debug("profile {} deleted", id);
            }

            case Constants.VALID_PROFILES->
            {
                validProfiles.remove(id);

                logger.debug("valid profile {} deleted", id);
            }
        }
    }

    public boolean present(long id)
    {
        return profiles.containsKey(id);
    }
}

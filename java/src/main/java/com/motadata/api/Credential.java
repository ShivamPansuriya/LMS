package com.motadata.api;

import com.motadata.constants.Constants;
import com.motadata.database.CredentialDB;
import com.motadata.utils.Profile;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Credential implements Profile
{
    static final Logger logger = LoggerFactory.getLogger(Credential.class);

    private final Router router;

    private final Router credentialRouter;

    private final CredentialDB database = CredentialDB.getInstance();

    public Credential(Vertx vertx, Router router)
    {
        this.router = router;

        this.credentialRouter = Router.router(vertx);
    }
    public void setupRoute()
    {
        router.route("/credential/*").subRouter(credentialRouter);

        credentialRouter.route(HttpMethod.POST,"/").handler(this::createProfile);

        credentialRouter.route(HttpMethod.GET,"/").handler(this::getAllProfile);

        credentialRouter.route(HttpMethod.GET,"/:id").handler(this::getProfile);

        credentialRouter.route(HttpMethod.PUT,"/:id").handler(this::updateProfile);

        credentialRouter.route(HttpMethod.DELETE,"/:id").handler(this::deleteProfile);
    }

    @Override
    public void getProfile(RoutingContext ctx)
    {
        var response = new JsonObject();

        var profile = database.get(Long.parseLong(ctx.request().getParam(Constants.ID)), Constants.PROFILES);

        if(profile.isEmpty())
        {
            response.put(Constants.STATUS,Constants.STATUS_FAIL);

            response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "READ ERROR").put(Constants.ERROR_CODE, Constants.RESOURCE_NOT_PRESENT).put(Constants.ERROR_MESSAGE, String.format("error in reading %s, because no such profile exists",Constants.CREDENTIAL)));

            ctx.response().setStatusCode(response.getJsonObject(Constants.ERROR).getInteger(Constants.ERROR_CODE)).putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());
        }
        else
        {
            response.put(Constants.STATUS,Constants.STATUS_SUCCESS);

            response.put(Constants.RESULT,profile);

            ctx.response().putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());
        }

    }

    @Override
    public void getAllProfile(RoutingContext ctx)
    {
        var response = new JsonObject().put(Constants.RESULT, database.getAll(Constants.PROFILES));

        response.put(Constants.STATUS,Constants.STATUS_SUCCESS);

        ctx.response().putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());
    }

    @Override
    public void createProfile(RoutingContext ctx)
    {
        ctx.request().bodyHandler(buffer ->
        {

            var request = buffer.toJsonObject();

            var response = new JsonObject();

            if(request.containsKey(Constants.USERNAME) || request.containsKey(Constants.PASSWORD) || request.containsKey(Constants.NAME))
            {
                if(!request.getString(Constants.USERNAME).isEmpty() && !request.getString(Constants.PASSWORD).isEmpty() && !request.getString(Constants.NAME).isEmpty())
                {
                    var profiles = database.getAll(Constants.PROFILES);

                    for(var profile : profiles)
                    {
                        if(request.getString(Constants.NAME).equals(new JsonObject(profile.toString()).getString(Constants.NAME)))
                        {
                            response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "WRITE ERROR").put(Constants.ERROR_CODE, Constants.BAD_REQUEST).put(Constants.ERROR_MESSAGE, String.format("error in saving %s, because profile name is already used",Constants.CREDENTIAL)));

                            break;
                        }
                    }
                    if(response.containsKey(Constants.ERROR))
                    {
                        response.put(Constants.STATUS, Constants.STATUS_FAIL);
                    }
                    else
                    {
                        var id = database.create(request);

                        response.put(Constants.CREDENTIAL_ID,id);

                        response.put(Constants.STATUS, Constants.STATUS_SUCCESS);
                    }
                }
                else
                {
                    logger.error("Credentials are Invalid !!");

                    response.put(Constants.ERROR,"Empty Fields")

                            .put(Constants.ERROR_CODE, Constants.BAD_REQUEST)

                            .put(Constants.ERROR_MESSAGE, "Fields Can't Be Empty")

                            .put(Constants.STATUS, Constants.STATUS_FAIL);
                }
            }
            else
            {
                logger.error("Credentials are Missing in the Request !!");

                response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR,"No Credentials Provided")

                        .put(Constants.ERROR_CODE, Constants.BAD_REQUEST)

                        .put(Constants.ERROR_MESSAGE, "Provide username, password and name")

                        .put(Constants.STATUS, Constants.STATUS_FAIL));
            }

            if(response.containsKey(Constants.ERROR))
            {
                ctx.response().setStatusCode(response.getJsonObject(Constants.ERROR).getInteger(Constants.ERROR_CODE)).putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());
            }
            else
            {
                ctx.response().putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());
            }
        });

    }

    @Override
    public void updateProfile(RoutingContext ctx)
    {
        ctx.request().bodyHandler(buffer ->
        {

            var request = buffer.toJsonObject();

            var response = new JsonObject();

            if(request.containsKey(Constants.USERNAME) || request.containsKey(Constants.PASSWORD) || request.containsKey(Constants.NAME))
            {
                if(!request.getString(Constants.USERNAME).isEmpty() && !request.getString(Constants.PASSWORD).isEmpty() && !request.getString(Constants.NAME).isEmpty())
                {
                    var profile = database.get(Long.parseLong(ctx.request().getParam(Constants.ID)),Constants.PROFILES);

                    if(profile.isEmpty())
                    {
                        response.put(Constants.STATUS, Constants.STATUS_FAIL);

                        response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "UPDATE ERROR").put(Constants.ERROR_CODE, Constants.RESOURCE_NOT_PRESENT).put(Constants.ERROR_MESSAGE, String.format("error in updating %s, because no such profile exists",Constants.CREDENTIAL)));
                    }
                    else
                    {
                        database.update(request,Long.parseLong(ctx.request().getParam(Constants.ID)),"profiles");

                        response.put(Constants.STATUS, Constants.STATUS_SUCCESS);

                        response.put(Constants.MESSAGE,"update successful");

                    }
                }
                else
                {
                    logger.error("Credentials are Invalid !!");

                    response.put(Constants.ERROR,"Empty Fields")

                            .put(Constants.ERROR_CODE, Constants.BAD_REQUEST)

                            .put(Constants.ERROR_MESSAGE, "Fields Can't Be Empty")

                            .put(Constants.STATUS, Constants.STATUS_FAIL);
                }
            }
            else
            {
                logger.error("Credentials are Missing in the Request !!");

                response.put(Constants.ERROR,"No Credentials Provided")

                        .put(Constants.ERROR_CODE, Constants.BAD_REQUEST)

                        .put(Constants.ERROR_MESSAGE, "Provide username, password and name")

                        .put(Constants.STATUS, Constants.STATUS_FAIL);
            }

            if(response.containsKey(Constants.ERROR))
            {
                ctx.response().setStatusCode(response.getJsonObject(Constants.ERROR).getInteger(Constants.ERROR_CODE)).putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());
            }
            else
            {
                ctx.response().putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());
            }
        });
    }

    @Override
    public void deleteProfile(RoutingContext ctx)
    {
        var response = new JsonObject();

        var profile = database.get(Long.parseLong(ctx.request().getParam(Constants.ID)), Constants.PROFILES);

        if(profile.isEmpty())
        {
            response.put(Constants.STATUS,Constants.STATUS_FAIL);

            response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "DELETE ERROR").put(Constants.ERROR_CODE, Constants.RESOURCE_NOT_PRESENT).put(Constants.ERROR_MESSAGE, String.format("error in deleting %s, because no such profile exists",Constants.CREDENTIAL)));

            ctx.response().setStatusCode(response.getJsonObject(Constants.ERROR).getInteger(Constants.ERROR_CODE)).putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());
        }
        else
        {
            database.delete(Long.parseLong(ctx.request().getParam(Constants.ID)),Constants.PROFILES);

            response.put(Constants.STATUS,Constants.STATUS_SUCCESS);

            response.put(Constants.MESSAGE,"Delete Successful");

            ctx.response().putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());
        }

    }

}

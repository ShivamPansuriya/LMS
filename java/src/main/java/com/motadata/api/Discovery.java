package com.motadata.api;

import com.motadata.constants.Constants;
import com.motadata.database.CredentialDB;
import com.motadata.database.DiscoveryDB;
import com.motadata.utils.Profile;
import com.motadata.utils.Utils;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discovery implements Profile
{
    static final Logger logger = LoggerFactory.getLogger(Discovery.class);

    private final Router router;

    private final EventBus eventBus;

    private final DiscoveryDB database = DiscoveryDB.getInstance();

    private final Router DiscoveryRouter;

    public Discovery(Vertx vertx, Router router)
    {
        this.router = router;

        this.eventBus = vertx.eventBus();

        this.DiscoveryRouter = Router.router(vertx);
    }

    public void setupRoute()
    {
        router.route("/discovery/*").subRouter(DiscoveryRouter);

        DiscoveryRouter.route(HttpMethod.POST, "/").handler(this::createProfile);

        DiscoveryRouter.route(HttpMethod.GET, "/").handler(this::getAllProfile);

        DiscoveryRouter.route(HttpMethod.GET, "/:id").handler(this::getProfile);

        DiscoveryRouter.route(HttpMethod.PUT, "/:id").handler(this::updateProfile);

        DiscoveryRouter.route(HttpMethod.DELETE, "/:id").handler(this::deleteProfile);

        DiscoveryRouter.route(HttpMethod.POST, "/run/:id").handler(this::runDiscovery);

        DiscoveryRouter.route(HttpMethod.GET, "/run/:id").handler(this::discoveryResult);
    }

    @Override
    public void getProfile(RoutingContext ctx)
    {
        var response = new JsonObject();

        var profile = database.get(Long.parseLong(ctx.request().getParam(Constants.ID)),Constants.PROFILES);

        if(profile.isEmpty())
        {
            response.put(Constants.STATUS,Constants.STATUS_FAIL);

            response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "READ ERROR").put(Constants.ERROR_CODE, Constants.RESOURCE_NOT_PRESENT).put(Constants.ERROR_MESSAGE, String.format("error in reading %s, because no such profile exists",Constants.DISCOVERY)));

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
        ctx.request().bodyHandler(buffer -> {

            var request = buffer.toJsonObject();

            var response = new JsonObject();

            if(request.containsKey(Constants.IP) || request.containsKey(Constants.PORT) || request.containsKey(Constants.NAME) || request.containsKey(Constants.CREDENTIALS))
            {
                if(!request.getString(Constants.IP).isEmpty() && !request.getString(Constants.PORT).isEmpty() && !request.getString(Constants.NAME).isEmpty() && !request.getString(Constants.CREDENTIALS).isEmpty())
                {
                    var profiles = database.getAll(Constants.PROFILES);

                    for(var profile : profiles)
                    {
                        if(request.getString(Constants.NAME).equals(new JsonObject(profile.toString()).getString(Constants.NAME)))
                        {
                            response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "WRITE ERROR").put(Constants.ERROR_CODE, Constants.BAD_REQUEST).put(Constants.ERROR_MESSAGE, String.format("error in saving %s, because profile name is already used",Constants.DISCOVERY)));

                            break;
                        }
                    }
                    var credentialDatabase = CredentialDB.getInstance();

                    for(var id : request.getJsonArray(Constants.CREDENTIALS))
                    {
                        if(!credentialDatabase.present(Long.parseLong(id.toString())))
                        {
                            response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "WRITE ERROR").put(Constants.ERROR_CODE, Constants.BAD_REQUEST).put(Constants.ERROR_MESSAGE, String.format("error in saving %s, because credentials are invalid",Constants.DISCOVERY)));

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

                        response.put(Constants.DISCOVERY_ID,id);

                        response.put(Constants.STATUS, Constants.STATUS_SUCCESS);
                    }
                }
                else
                {
                    logger.error("Discovery fields are Invalid !!");

                    response.put(Constants.ERROR, "Empty Fields")

                            .put(Constants.ERROR_CODE, Constants.BAD_REQUEST)

                            .put(Constants.ERROR_MESSAGE, "Fields Can't Be Empty")

                            .put(Constants.STATUS, Constants.STATUS_FAIL);
                }
            }
            else
            {
                logger.error("Discovery fields are Missing in the Request !!");

                response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "No Discovery profile Provided")

                        .put(Constants.ERROR_CODE, Constants.BAD_REQUEST)

                        .put(Constants.ERROR_MESSAGE, "Provide ip, port and name")

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
        ctx.request().bodyHandler(buffer -> {

            var request = buffer.toJsonObject().put(Constants.DISCOVERY_ID, ctx.request().getParam(Constants.ID));

            var response = new JsonObject();

            if(request.containsKey(Constants.IP) || request.containsKey(Constants.PORT) || request.containsKey(Constants.NAME))
            {
                if(!request.getString(Constants.IP).isEmpty() && !request.getString(Constants.PORT).isEmpty() && !request.getString(Constants.NAME).isEmpty())
                {
                    var profile = database.get(Long.parseLong(ctx.request().getParam(Constants.ID)), Constants.PROFILES);

                    if(profile.isEmpty())
                    {
                        response.put(Constants.STATUS, Constants.STATUS_FAIL);

                        response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "UPDATE ERROR").put(Constants.ERROR_CODE, Constants.RESOURCE_NOT_PRESENT).put(Constants.ERROR_MESSAGE, String.format("error in updating %s, because no such profile exists",Constants.DISCOVERY)));
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
                    logger.error("Discovery fields are Invalid !!");

                    response.put(Constants.ERROR, "Empty Fields")

                            .put(Constants.ERROR_CODE, Constants.BAD_REQUEST)

                            .put(Constants.ERROR_MESSAGE, "Fields Can't Be Empty")

                            .put(Constants.STATUS, Constants.STATUS_FAIL);
                }
            }
            else
            {
                logger.error("Discovery field are Missing in the Request !!");

                response.put(Constants.ERROR, "Discovery filed not provided Provided")

                        .put(Constants.ERROR_CODE, Constants.BAD_REQUEST)

                        .put(Constants.ERROR_MESSAGE, "Provide ip, port and name")

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

            response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "DELETE ERROR").put(Constants.ERROR_CODE, Constants.RESOURCE_NOT_PRESENT).put(Constants.ERROR_MESSAGE, String.format("error in deleting %s, because no such profile exists",Constants.DISCOVERY)));

            ctx.response().setStatusCode(response.getJsonObject(Constants.ERROR).getInteger(Constants.ERROR_CODE)).putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());
        }
        else
        {
            database.delete(Long.parseLong(ctx.request().getParam(Constants.ID)), Constants.PROFILES);

            database.delete(Long.parseLong(ctx.request().getParam(Constants.ID)), Constants.VALID_PROFILES);

            response.put(Constants.STATUS,Constants.STATUS_SUCCESS);

            response.put(Constants.MESSAGE,"Delete Successful");

            ctx.response().putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());
        }
    }

    public void runDiscovery(RoutingContext ctx)
    {
        var response = new JsonObject();

        var requestID  = Long.parseLong(ctx.request().getParam(Constants.ID));

        if(database.present(requestID))
        {
            if(database.get(requestID, Constants.VALID_PROFILES).isEmpty())
            {
                var profile = database.get(requestID,Constants.PROFILES);

                profile.put(Constants.DISCOVERY_ID, requestID);

                var credentialIds = profile.getJsonArray(Constants.CREDENTIALS);

                var credentials = new JsonArray();

                for(var credentialId : credentialIds)
                {
                    var credentialProfile = CredentialDB.getInstance().get(Long.parseLong(credentialId.toString()),Constants.PROFILES);

                    credentialProfile.put(Constants.CREDENTIAL_ID, Long.parseLong(credentialId.toString()));

                    if(!credentialProfile.isEmpty())
                    {
                        credentials.add(credentialProfile);
                    }
                }
                profile.put(Constants.CREDENTIALS, credentials);

                if(!profile.getJsonArray(Constants.CREDENTIALS).isEmpty())
                {
                    var context = Utils.createContext(new JsonArray().add(profile), Constants.DISCOVERY, Constants.LINUX);

                    if(!context.isEmpty())
                    {
                        eventBus.send(Constants.RUN_DISCOVERY, context);
                    }
                    else
                    {
                        logger.error("error in preparing context");

                        response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "DISCOVERY RUN ERROR").put(Constants.ERROR_CODE, Constants.RESOURCE_NOT_PRESENT).put(Constants.ERROR_MESSAGE, "unable to create request"));
                    }
                }
                else
                {
                    response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "DISCOVERY RUN ERROR").put(Constants.ERROR_CODE, Constants.RESOURCE_NOT_PRESENT).put(Constants.ERROR_MESSAGE, "All the required credential has been deleted"));
                }

                if(response.containsKey(Constants.ERROR))
                {
                    response.put(Constants.STATUS, Constants.STATUS_FAIL);

                    ctx.response().setStatusCode(response.getJsonObject(Constants.ERROR).getInteger(Constants.ERROR_CODE)).putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());
                }
                else
                {
                    response.put(Constants.STATUS, Constants.STATUS_SUCCESS);

                    ctx.response().putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(new JsonObject().put(Constants.STATUS, Constants.STATUS_SUCCESS).put(Constants.MESSAGE, "please check for status after a minute").toString());
                }
            }
            else
            {
                response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "DISCOVERY RUN ERROR").put(Constants.ERROR_CODE, Constants.RESOURCE_NOT_PRESENT).put(Constants.ERROR_MESSAGE, "Discovery has already been run successfully"));

                response.put(Constants.STATUS, Constants.STATUS_FAIL);

                ctx.response().setStatusCode(response.getJsonObject(Constants.ERROR).getInteger(Constants.ERROR_CODE)).putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());

            }
        }
        else
        {
            response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "DISCOVERY RUN ERROR").put(Constants.ERROR_CODE, Constants.RESOURCE_NOT_PRESENT).put(Constants.ERROR_MESSAGE, "No such discovery ID present"));

            response.put(Constants.STATUS, Constants.STATUS_FAIL);

            ctx.response().setStatusCode(response.getJsonObject(Constants.ERROR).getInteger(Constants.ERROR_CODE)).putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());

        }

    }

    public void discoveryResult(RoutingContext ctx)
    {
        var response = new JsonObject();

        if(database.present(Long.parseLong(ctx.request().getParam(Constants.ID))))
        {
            var result = database.get(Long.parseLong(ctx.request().getParam(Constants.ID)),Constants.VALID_PROFILES);

            if(result.isEmpty())
            {
                response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "Discovery Run").put(Constants.ERROR_CODE, Constants.RESOURCE_NOT_PRESENT).put(Constants.ERROR_MESSAGE, "Discovery run fail"));

                response.put(Constants.STATUS, Constants.STATUS_FAIL);

                ctx.response().setStatusCode(response.getJsonObject(Constants.ERROR).getInteger(Constants.ERROR_CODE)).putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());
            }
            else
            {
                response.put(Constants.MESSAGE, "Discovery run success");

                response.put(Constants.STATUS, Constants.STATUS_SUCCESS);

                ctx.response().putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());
            }
        }
        else
        {
            response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "Discovery Run").put(Constants.ERROR_CODE, Constants.RESOURCE_NOT_PRESENT).put(Constants.ERROR_MESSAGE, "no such discovery profile present"));

            response.put(Constants.STATUS, Constants.STATUS_FAIL);

            ctx.response().setStatusCode(response.getJsonObject(Constants.ERROR).getInteger(Constants.ERROR_CODE)).putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());

        }
    }

}

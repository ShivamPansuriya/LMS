package com.motadata.api;

import com.motadata.constants.Constants;
import com.motadata.database.DiscoveryDB;
import com.motadata.database.ProvisionDB;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Provision
{
    static final Logger logger = LoggerFactory.getLogger(Provision.class);

    private final Router router;

    private final Router provisionRouter;

    private final ProvisionDB database = ProvisionDB.getInstance();

    public Provision(Vertx vertx, Router router)
    {
        this.router = router;

        this.provisionRouter = Router.router(vertx);
    }

    public void setupRoute()
    {
        router.route("/provision/*").subRouter(provisionRouter);

        provisionRouter.route(HttpMethod.POST, "/:id").handler(this::setProvision);

        provisionRouter.route(HttpMethod.DELETE, "/:id").handler(this::deleteProvision);

        provisionRouter.route(HttpMethod.GET, "/").handler(this::getAllProfile);
    }

    public void getAllProfile(RoutingContext ctx)
    {
        var response = new JsonObject().put(Constants.RESULT, database.getAll(Constants.VALID_PROFILES));

        response.put(Constants.STATUS, Constants.STATUS_SUCCESS);

        ctx.response().putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());
    }

    public void setProvision(RoutingContext ctx)
    {
        var response = new JsonObject();

        if(DiscoveryDB.getInstance().present(Long.parseLong(ctx.request().getParam(Constants.ID))))
        {
            var object = database.get(Long.parseLong(ctx.request().getParam(Constants.ID)),Constants.VALID_PROFILES);

            if(!object.containsKey(Constants.PROVISION_ID))
            {
                var id = database.create(object);

                object.put(Constants.PROVISION_ID, id);

                database.update(object, Long.parseLong(ctx.request().getParam(Constants.ID)),Constants.VALID_PROFILES);

                response.put(Constants.PROVISION_ID, id);

                if(response.containsKey(Constants.ERROR))
                {
                    response.put(Constants.STATUS, Constants.STATUS_FAIL);

                    response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "WRITE ERROR").put(Constants.ERROR_CODE, Constants.RESOURCE_NOT_PRESENT).put(Constants.ERROR_MESSAGE, "please run discovery first"));

                    ctx.response().setStatusCode(response.getJsonObject(Constants.ERROR).getInteger(Constants.ERROR_CODE)).putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());

                    logger.error("fail to provision, reason: {}", response.getJsonObject(Constants.ERROR));
                }
                else
                {
                    response.put(Constants.STATUS, Constants.STATUS_SUCCESS);

                    ctx.response().putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());

                }
            }
            else
            {
                response.put(Constants.STATUS, Constants.STATUS_FAIL);

                response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "WRITE ERROR").put(Constants.ERROR_CODE, Constants.BAD_REQUEST).put(Constants.ERROR_MESSAGE, "device has already been provisioned"));

                ctx.response().setStatusCode(response.getJsonObject(Constants.ERROR).getInteger(Constants.ERROR_CODE)).putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());

                logger.error("fail to provision, reason: {}", response.getJsonObject(Constants.ERROR));
            }
        }
        else
        {
            response.put(Constants.STATUS, Constants.STATUS_FAIL);

            response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "WRITE ERROR").put(Constants.ERROR_CODE, Constants.BAD_REQUEST).put(Constants.ERROR_MESSAGE, "no such discovery profile present"));

            ctx.response().setStatusCode(response.getJsonObject(Constants.ERROR).getInteger(Constants.ERROR_CODE)).putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());

            logger.error("fail to provision, reason: {}", response.getJsonObject(Constants.ERROR));
        }

    }

    public void deleteProvision(RoutingContext ctx)
    {
        var response = new JsonObject();

        var id = Long.parseLong(ctx.request().getParam(Constants.ID));

        if(database.present(id))
        {
            database.delete(Long.parseLong(database.get(id,Constants.PROFILES).getString(Constants.DISCOVERY_ID)), Constants.VALID_PROFILES);

            database.delete(id, Constants.PROFILES);

            response.put(Constants.STATUS, Constants.STATUS_SUCCESS);

            response.put(Constants.MESSAGE, "Delete Successful");

            ctx.response().putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());
        }
        else
        {
            response.put(Constants.STATUS, Constants.STATUS_FAIL);

            response.put(Constants.ERROR, new JsonObject().put(Constants.ERROR, "DELETE ERROR").put(Constants.ERROR_CODE, Constants.BAD_REQUEST).put(Constants.ERROR_MESSAGE, "no such device provisioned"));

            ctx.response().setStatusCode(response.getJsonObject(Constants.ERROR).getInteger(Constants.ERROR_CODE)).putHeader(Constants.CONTENT_TYPE, Constants.JSON).end(response.toString());

            logger.error("invalid delete request, reason: {}", response.getJsonObject(Constants.ERROR));
        }
    }


}


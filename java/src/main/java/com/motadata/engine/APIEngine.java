package com.motadata.engine;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import com.motadata.utils.Constants;
import io.vertx.ext.web.handler.ErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APIEngine extends AbstractVerticle
{
    static final Logger logger = LoggerFactory.getLogger(APIEngine.class);

    @Override
    public void start(Promise<Void> startPromise)
    {
        var eventBus = vertx.eventBus();

        var mainRouter = Router.router(vertx);

        var discoveryRouter = Router.router(vertx);

        var credentialRouter = Router.router(vertx);

        var provisionRouter = Router.router(vertx);

//        <<--------------------------sub routes-------------------------->>
        mainRouter.route("/credential/*").subRouter(credentialRouter);

        mainRouter.route("/discovery/*").subRouter(discoveryRouter);

        mainRouter.route("/provision/*").subRouter(provisionRouter);

        // <-------------------------------main router API------------------------------>>
        mainRouter.route("/").handler(ctx ->
        {
            ctx.json(new JsonObject().put(Constants.STATUS, Constants.STATUS_SUCCESS).put(Constants.MESSAGE, "Welcome to Network Monitoring System!"));
        });

        // error handler if routing fails
        mainRouter.route().failureHandler(ErrorHandler.create(vertx));

        // <<-----------------------------credential profile APIs------------------------------>
        // create new credential profile
        credentialRouter.route(HttpMethod.POST,"/post").handler(ctx-> ctx.request().bodyHandler(buffer -> eventBus.request(Constants.CREATE_CREDENTIAL,buffer.toString(), reply->
        {
            logger.debug(Constants.CONTAINERS, ctx.request().method(), ctx.request().path(), ctx.request().remoteAddress());

            if(reply.succeeded())
            {
                ctx.json(new JsonObject(reply.result().body().toString()));
            }
            else {
                ctx.response().setStatusCode(400);

                ctx.json(new JsonObject(reply.cause().getMessage()));
            }
        })));

        // get all credential profiles
        credentialRouter.route(HttpMethod.GET,"/get").handler(ctx-> eventBus.request(Constants.GET_CREDENTIAL,"", reply->
        {
            logger.debug(Constants.CONTAINERS, ctx.request().method(), ctx.request().path(), ctx.request().remoteAddress());

            if(reply.succeeded())
            {
                ctx.json(new JsonObject(reply.result().body().toString()));
            }
            else
            {
                ctx.response().setStatusCode(400);

                ctx.json(new JsonObject(reply.cause().getMessage()));
            }

        }));

        // update credential profile
        credentialRouter.route(HttpMethod.PUT,"/put").handler(ctx-> ctx.request().bodyHandler(buffer -> eventBus.request(Constants.UPDATE_CREDENTIAL,buffer.toString(), reply->
        {
            logger.debug(Constants.CONTAINERS, ctx.request().method(), ctx.request().path(), ctx.request().remoteAddress());

            if(reply.succeeded())
            {
                ctx.json(new JsonObject(reply.result().body().toString()));
            }
            else
            {
                ctx.response().setStatusCode(400);

                ctx.json(new JsonObject(reply.cause().getMessage()));
            }

        })));

        // delete credential profile
        credentialRouter.route(HttpMethod.DELETE,"/delete").handler(ctx-> ctx.request().bodyHandler(buffer -> eventBus.request(Constants.DELETE_CREDENTIAL,buffer.toString(), reply->
        {
            logger.debug(Constants.CONTAINERS, ctx.request().method(), ctx.request().path(), ctx.request().remoteAddress());

            if(reply.succeeded())
            {
                ctx.json(new JsonObject(reply.result().body().toString()));
            }
            else
            {
                ctx.response().setStatusCode(400);

                ctx.json(new JsonObject(reply.cause().getMessage()));
            }

        })));


        // <----------------------------------discovery APIs-------------------------------------->

        // create new discovery profile
        discoveryRouter.route(HttpMethod.POST,"/post").handler(ctx-> ctx.request().bodyHandler(buffer -> eventBus.request(Constants.CREATE_DISCOVERY,buffer.toString(), reply->
        {
            logger.debug(Constants.CONTAINERS, ctx.request().method(), ctx.request().path(), ctx.request().remoteAddress());

            if(reply.succeeded())
            {
                ctx.json(new JsonObject(reply.result().body().toString()));
            }
            else
            {
                ctx.response().setStatusCode(400);

                ctx.json(new JsonObject(reply.cause().getMessage()));
            }
        })));

        // get all discovery profiles
        discoveryRouter.route(HttpMethod.GET,"/get").handler(ctx-> eventBus.request(Constants.GET_DISCOVERY,"", reply->
        {
            logger.debug(Constants.CONTAINERS, ctx.request().method(), ctx.request().path(), ctx.request().remoteAddress());

            if(reply.succeeded())
            {
                ctx.json(new JsonObject(reply.result().body().toString()));
            }
            else
            {
                ctx.response().setStatusCode(400);

                ctx.json(new JsonObject(reply.cause().getMessage()));
            }

        }));

        // run discovery
        discoveryRouter.route(HttpMethod.GET,"/run/:id").handler(ctx-> eventBus.request(Constants.RUN_DISCOVERY, new JsonObject().put(Constants.ID,ctx.request().getParam("id")).toString(), reply ->
        {
            logger.debug(Constants.CONTAINERS, ctx.request().method(), ctx.request().path(), ctx.request().remoteAddress());

            if(reply.succeeded())
            {
                ctx.json(new JsonObject(reply.result().body().toString()));
            }
            else
            {
                ctx.response().setStatusCode(400);

                ctx.json(new JsonObject(reply.cause().getMessage()));
            }

        }));

        // update discovery profile
        discoveryRouter.route(HttpMethod.PUT,"/put").handler(ctx-> ctx.request().bodyHandler(buffer -> eventBus.request(Constants.UPDATE_DISCOVERY,buffer.toString(), reply->
        {
            logger.debug(Constants.CONTAINERS, ctx.request().method(), ctx.request().path(), ctx.request().remoteAddress());

            if(reply.succeeded())
            {
                ctx.json(new JsonObject(reply.result().body().toString()));
            }
            else
            {
                ctx.response().setStatusCode(400);

                ctx.json(new JsonObject(reply.cause().getMessage()));
            }

        })));

        // delete discovery profile
        discoveryRouter.route(HttpMethod.DELETE,"/delete").handler(ctx-> ctx.request().bodyHandler(buffer -> eventBus.request(Constants.DELETE_DISCOVERY,buffer.toString(), reply->
        {
            logger.debug(Constants.CONTAINERS, ctx.request().method(), ctx.request().path(), ctx.request().remoteAddress());

            if(reply.succeeded())
            {
                ctx.json(new JsonObject(reply.result().body().toString()));
            }
            else
            {
                ctx.response().setStatusCode(400);

                ctx.json(new JsonObject(reply.cause().getMessage()));
            }

        })));

        // <<---------------------------Provision APIs------------------------------>>

        // provision of device
        provisionRouter.route(HttpMethod.POST,"/run/:id").handler(ctx-> eventBus.request(Constants.PROVISION, new JsonObject().put(Constants.ID,ctx.request().getParam("id")).toString(), reply->
        {
            logger.debug(Constants.CONTAINERS, ctx.request().method(), ctx.request().path(), ctx.request().remoteAddress());

            if(reply.succeeded())
            {
                ctx.json(new JsonObject(reply.result().body().toString()));
            }
            else
            {
                ctx.response().setStatusCode(400);

                ctx.json(new JsonObject(reply.cause().getMessage()));
            }
        }));

        //get provision data (currently not in use)
        provisionRouter.route(HttpMethod.GET,"/get/:ip").handler(ctx-> eventBus.request(Constants.GET_POLL_DATA, new JsonObject().put(Constants.IP,ctx.request().getParam("ip")).toString(), result->
        {
            logger.debug(Constants.CONTAINERS, ctx.request().method(), ctx.request().path(), ctx.request().remoteAddress());

            if(result.succeeded())
            {
                ctx.json(new JsonObject(result.result().body().toString()));
            }
            else
            {
                ctx.response().setStatusCode(500);

                ctx.json(new JsonObject(result.cause().getMessage()));
            }
        }));

        // un-provision of device
        provisionRouter.route(HttpMethod.DELETE,"/delete/:id").handler(ctx-> eventBus.request(Constants.UNPROVISION, new JsonObject().put(Constants.ID,ctx.request().getParam("id")).toString(), result->
        {
            logger.debug(Constants.CONTAINERS, ctx.request().method(), ctx.request().path(), ctx.request().remoteAddress());

            if(result.succeeded())
            {
                ctx.json(new JsonObject(result.result().body().toString()));
            }
            else
            {
                ctx.response().setStatusCode(400);

                ctx.json(new JsonObject(result.cause().getMessage()));
            }
        }));


        vertx.createHttpServer(new HttpServerOptions().setPort(8000).setHost("localhost")).requestHandler(mainRouter).listen().onComplete(result->
        {
            if(result.succeeded())
            {
                logger.info("server started on port 8000");

                startPromise.complete();
            }
            else
            {
                logger.error("can't start server: %s \n", result.cause());

                startPromise.fail(result.cause());
            }
        });
    }
}

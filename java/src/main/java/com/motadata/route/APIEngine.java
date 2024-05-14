package com.motadata.route;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import com.motadata.utils.Constants;
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

        mainRouter.route("/credential/*").subRouter(credentialRouter);

        mainRouter.route("/discovery/*").subRouter(discoveryRouter);

        mainRouter.route("/provision/*").subRouter(provisionRouter);

        // main router API
        mainRouter.route("/").handler(ctx ->
        {
            ctx.json(new JsonObject().put("STATUS", "SUCCESS").put("MESSAGE", "Welcome to Network Monitoring System!"));
        });

        // credential profile APIs
        credentialRouter.route(HttpMethod.POST,"/post").handler(ctx-> ctx.request().bodyHandler(buffer -> eventBus.request(Constants.CREATE_CREDENTIAL,buffer.toString(), reply->
        {
            if(reply.succeeded())
            {
                ctx.json(new JsonObject(reply.result().body().toString()));
            }
            else {
                ctx.response().setStatusCode(400);

                ctx.json(new JsonObject(reply.cause().getMessage()));
            }
        })));

        credentialRouter.route(HttpMethod.GET,"/get").handler(ctx-> eventBus.request(Constants.GET_CREDENTIAL,"", reply->
        {
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

        credentialRouter.route(HttpMethod.PUT,"/put").handler(ctx-> ctx.request().bodyHandler(buffer -> eventBus.request(Constants.UPDATE_CREDENTIAL,buffer.toString(), reply->
        {
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

        credentialRouter.route(HttpMethod.DELETE,"/delete").handler(ctx-> ctx.request().bodyHandler(buffer -> eventBus.request(Constants.DELETE_CREDENTIAL,buffer.toString(), reply->
        {
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


        // discovery APIs

        discoveryRouter.route(HttpMethod.POST,"/post").handler(ctx-> ctx.request().bodyHandler(buffer -> eventBus.request(Constants.CREATE_DISCOVERY,buffer.toString(), reply->
        {
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

        discoveryRouter.route(HttpMethod.GET,"/get").handler(ctx-> eventBus.request(Constants.GET_DISCOVERY,"", reply->
        {
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

        discoveryRouter.route(HttpMethod.GET,"/run/:id").handler(ctx-> eventBus.request(Constants.RUN_DISCOVERY, new JsonObject().put(Constants.ID,ctx.request().getParam("id")).toString(), reply ->
        {
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

        discoveryRouter.route(HttpMethod.PUT,"/put").handler(ctx-> ctx.request().bodyHandler(buffer -> eventBus.request(Constants.UPDATE_DISCOVERY,buffer.toString(), reply->
        {
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

        discoveryRouter.route(HttpMethod.DELETE,"/delete").handler(ctx-> ctx.request().bodyHandler(buffer -> eventBus.request(Constants.DELETE_DISCOVERY,buffer.toString(), reply->
        {
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

        //Provision APIs

        provisionRouter.route(HttpMethod.GET,"/run/:id").handler(ctx-> eventBus.request(Constants.PROVISION, new JsonObject().put(Constants.ID,ctx.request().getParam("id")).toString(), reply->
        {
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

        provisionRouter.route(HttpMethod.GET,"/get/:ip").handler(ctx-> eventBus.request(Constants.GET_POOL_DATA, new JsonObject().put(Constants.IP,ctx.request().getParam("ip")).toString(), result->
        {
            if(result.succeeded())
            {
                ctx.json(new JsonObject(result.result().body().toString()));
            }
            else
            {
                ctx.response().setStatusCode(500);

                ctx.json(result.cause().getMessage());
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

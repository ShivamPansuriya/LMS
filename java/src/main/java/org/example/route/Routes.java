package org.example.route;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.example.utils.Constants;

public class Routes extends AbstractVerticle
{
    @Override
    public void start() throws Exception
    {
        var mainRouter = Router.router(vertx);

        Router discoveryRouter = Router.router(vertx);

        Router credentialRouter = Router.router(vertx);

        mainRouter.route("/credential/*").subRouter(credentialRouter);

        mainRouter.route("/discovery/*").subRouter(discoveryRouter);

        // main router API
        mainRouter.route("/").handler(ctx -> {
            ctx.json(new JsonObject().put("STATUS", "SUCCESS").put("MESSAGE", "Welcome to Network Monitoring System!"));
        });

        // credential profile APIs
        credentialRouter.route(HttpMethod.POST,"/post").handler(ctx->
        {
            ctx.request().bodyHandler(buffer -> {

                vertx.eventBus().request(Constants.CREATE_PROFILE,buffer.toString(), reply->
                {
                    if(reply.succeeded())
                    {
                        ctx.json(new JsonObject(reply.result().body().toString()));
                    }
                });
            });
        });

        credentialRouter.route(HttpMethod.GET,"/get").handler(ctx->
        {
                vertx.eventBus().request(Constants.GET_PROFILE,"", reply->
                {
                    if(reply.succeeded())
                    {
                        ctx.json(new JsonObject(reply.result().body().toString()));
                    }

                });

        });

        // discovery APIs

        discoveryRouter.route(HttpMethod.POST,"/post").handler(ctx->
        {
            ctx.request().bodyHandler(buffer -> {

                vertx.eventBus().request(Constants.CREATE_DISCOVERY,buffer.toString(), reply->
                {
                    if(reply.succeeded())
                    {
                        ctx.json(new JsonObject(reply.result().body().toString()));
                    }
                });
            });
        });

        discoveryRouter.route(HttpMethod.GET,"/get").handler(ctx->
        {
            vertx.eventBus().request(Constants.GET_DISCOVERY,"", reply->
            {
                if(reply.succeeded())
                {
                    ctx.json(new JsonObject(reply.result().body().toString()));
                }

            });

        });

        discoveryRouter.route(HttpMethod.GET,"/run").handler(ctx->
        {

            ctx.request().bodyHandler(buffer -> {

                vertx.eventBus().request(Constants.RUN_DISCOVERY, buffer.toString(), reply -> {
                    if(reply.succeeded())
                    {
                        ctx.json(new JsonObject(reply.result().body().toString()));
                    }

                });
            });
        });


        vertx.createHttpServer(new HttpServerOptions().setPort(8000).setHost("localhost")).requestHandler(mainRouter).listen().onComplete(status->{
            if(status.succeeded())
            {
                System.out.println("server started on port 8000");
            }
            else
            {
                System.out.printf("can't start server: %s \n", status.cause());
            }
        });
    }
}

package org.example.route;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import org.example.utils.Constants;

public class Routes extends AbstractVerticle
{
    @Override
    public void start() throws Exception
    {
        var router = Router.router(vertx);

        var credentialProfileRoute = router.route("/credentialprofile");

        router.route("/").handler(ctx->{
            ctx.response().end("hello");
        });
        credentialProfileRoute.method(HttpMethod.POST).handler(ctx->
        {
           vertx.eventBus().request(Constants.CREATE_PROFILE,ctx.request().body().toString(), reply->
           {
                ctx.json(reply.result().body());
           });
        });



        vertx.createHttpServer(new HttpServerOptions().setPort(8000).setHost("10.20.40.239")).requestHandler(router).listen().onComplete(status->{
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

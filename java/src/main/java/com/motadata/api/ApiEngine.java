package com.motadata.api;

import com.motadata.constants.Constants;
import com.motadata.utils.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiEngine extends AbstractVerticle
{

    static final Logger logger = LoggerFactory.getLogger(ApiEngine.class);
    @Override
    public void start(Promise<Void> startPromise)
    {
        var mainRouter = Router.router(vertx);

        //        <<--------------------------sub routes-------------------------->>
        new Credential(vertx,mainRouter).setupRoute();

        new Discovery(vertx,mainRouter).setupRoute();

        new Provision(vertx,mainRouter).setupRoute();

        var port = Config.PORT;

        var ip = Config.HOST;


        vertx.createHttpServer(new HttpServerOptions().setPort(port).setHost(ip)).requestHandler(mainRouter).listen().onComplete(result->
        {
            if(result.succeeded())
            {
                logger.info("server started at {}:{}",ip,port);

                startPromise.complete();
            }
            else
            {
                startPromise.fail(result.cause());
            }
        });

    }
}

package com.motadata.utils;

import io.vertx.ext.web.RoutingContext;

public interface Profile
{
    void getProfile(RoutingContext ctx);

    void getAllProfile(RoutingContext ctx);

    void updateProfile(RoutingContext ctx);

    void createProfile(RoutingContext ctx);

    void deleteProfile(RoutingContext ctx);
}

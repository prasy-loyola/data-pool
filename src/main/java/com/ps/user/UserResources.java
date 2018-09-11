package com.ps.user;

import com.google.gson.JsonObject;
import com.ps.db.DbUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.UUID;

@Path("users")
public class UserResources {


    @GET
    @Path("newUserId")
    @Produces(MediaType.APPLICATION_JSON)
    public String createNewUserId() {
        String userId = UUID.randomUUID().toString();

        DbUtils.addDataToDB(userId,new HashMap<>());
        JsonObject object = new JsonObject();
        object.addProperty("userId",userId);
        object.addProperty("message","user created successfully");
        return object.toString();
    }
}

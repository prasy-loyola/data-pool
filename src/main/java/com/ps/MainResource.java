package com.ps;

import com.google.gson.JsonObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("")
public class MainResource {

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public String heartBeat(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message","Server is up!!!!!!!!!");
        return jsonObject.toString();
    }
}

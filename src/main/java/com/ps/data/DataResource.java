package com.ps.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ps.Config;
import com.ps.utils.ExcelUtils;
import jdk.nashorn.internal.objects.annotations.Getter;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("data")
public class DataResource {
    private static DataPool<Map<String, String>> dataPool = new DataPool<>(Config.USER_TIMEOUT);

    static {
        try {
            ExcelUtils.getAllData(Config.WORKBOOK_NAME, Config.WORKSHEET_NAME)
                    .stream().forEach(stringStringMap -> dataPool.registerData(stringStringMap));
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    @GET
    @Path("index")
    @Produces(MediaType.TEXT_PLAIN)
    public String checkServer() {
        return "Server is running";
    }

    @GET
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDataFromUserDataPool(@QueryParam("role") String roleName) {

        try {
            return dataPool.getDataAsResponseJsonString(
                    hashMapStream -> hashMapStream.filter(hashMapStreamob -> hashMapStreamob.get(Config.DEFAULT_ID_FIELD).trim().equals(roleName))
                            .findFirst()
            );
        } catch (Exception e) {
            throw new NotFoundException(String.format("User with role: '%s' not available", roleName), e);
        }
    }


    @GET
    @Path("querydata")
    @Produces(MediaType.APPLICATION_JSON)
    public String queryDataPoool(@QueryParam("key") String key, @QueryParam("value") String value) {
        key = key == null ? Config.DEFAULT_ID_FIELD : key;
        try {
            String finalKey = key;
            return dataPool.getDataAsResponseJsonString(
                    hashMapStream -> hashMapStream.filter(hashMapStreamob -> hashMapStreamob.get(finalKey).trim().equals(value.trim()))
                            .findFirst()
            );
        } catch (Exception e) {
            throw new NotFoundException(String.format("User with key: '%s' and value '%s' not available", key, value), e);
        }
    }

    @GET
    @Path("{uuid}/release")
    @Produces(MediaType.APPLICATION_JSON)
    public String releaseData(@PathParam("uuid") String uuid) {
        return dataPool.releaseData(uuid);
    }


    @POST
    @Path("register")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String registerData(String data) {
        Gson gson = new Gson();
        Map dataAsMap = gson.fromJson(data, Map.class);
        Map<String, String> dataAsStringMap = new HashMap<>();
        dataAsMap.forEach((k, v) ->
        {
            dataAsStringMap.put(String.valueOf(k), String.valueOf(v));
        });

        dataPool.registerData(dataAsStringMap);
        JsonObject object = new JsonObject();
        object.addProperty("message", "Data added to data pool. Note: data with multiple field heirarchy is not supported yet.");
        object.addProperty("data", dataAsStringMap.toString());
        return object.toString();

    }

    @GET
    @Path("/releaseAll")
    @Produces(MediaType.APPLICATION_JSON)
    public String releaseAllData() {
        return dataPool.releaseAllData();
    }


}

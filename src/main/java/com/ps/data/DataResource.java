package com.ps.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ps.Config;
import com.ps.db.DbUtils;
import com.ps.utils.ExcelUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("{userId}/data")
public class DataResource {
    private static Map<String,DataPool<Map<String, String>>> dataPools = new HashMap<>();

    @PathParam("userId")
    private
    String userId;

    static {
        try {
//            ExcelUtils.getAllData(Config.WORKBOOK_NAME, Config.WORKSHEET_NAME)
//                    .stream().forEach(stringStringMap -> getDataPool("").registerData(stringStringMap));
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    private static DataPool<Map<String, String>> getDataPool(String userId) {
        if(!dataPools.containsKey(userId)){
            DataPool<Map<String, String>> dataPool = new DataPool<>(Config.USER_TIMEOUT);
            DbUtils.getAllDataForUser(userId).forEach(stringStringMap -> dataPool.registerData(stringStringMap));
            dataPools.put(userId,dataPool);
        }
        return dataPools.get(userId);
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
            return getDataPool(getUserId()).getDataAsResponseJsonString(
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
            return getDataPool(getUserId()).getDataAsResponseJsonString(
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
        return getDataPool(getUserId()).releaseData(uuid);
    }


    @POST
    @Path("register")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String registerData(String data) {
        Gson gson = new Gson();
        JsonElement element = gson.fromJson(data,JsonElement.class);
        List<JsonElement> list = new ArrayList<>();
        if(element.isJsonArray())
        {
            element.getAsJsonArray().forEach(list::add);
        } else if (element.isJsonObject()){
            list.add(element);
        }


        list.forEach(jsonElement -> getDataPool(getUserId()).registerData(gson.fromJson(jsonElement,Map.class)));
        list.forEach(jsonElement -> DbUtils.addUserToDB(getUserId(),gson.fromJson(jsonElement,Map.class)));
        JsonObject object = new JsonObject();
        object.addProperty("message", "Data added to data pool. Note: data with multiple field heirarchy is not supported yet.");
        object.addProperty("data", list.toString());
        return object.toString();

    }

    @GET
    @Path("releaseAll")
    @Produces(MediaType.APPLICATION_JSON)
    public String releaseAllData() {
        return getDataPool(getUserId()).releaseAllData();
    }

    private String getUserId() {
        return userId;
    }

    private void setUserId(String userId) {
        this.userId = userId;
    }
}

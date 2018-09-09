package com.ps.data;

import com.google.gson.*;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ConcurrentHashSet;

import javax.ws.rs.NotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataPool<T> {
    Logger logger = Logger.getLogger(DataPool.class);
    Map<String, LockableData<T>> lockedData = new ConcurrentHashMap<>();
    private Set<T> allData = new ConcurrentHashSet<>();
    private long lockTimeoutInMilliSeconds;


    public DataPool(long lockTimeoutInMilliSeconds) {
        this.lockTimeoutInMilliSeconds = lockTimeoutInMilliSeconds;

    }

    public void registerData(T data) {
        allData.add(data);
    }

    private String lockData(T data) {
        String uuid = UUID.randomUUID().toString();

        allData.remove(data);
        lockedData.put(uuid, new LockableData<>(data));
        return uuid;
    }

    public String releaseData(String uuid) {
        LockableData<T> removedData = lockedData.remove(uuid);
        if (removedData != null) {
            registerData(removedData.getObject());
        }
        return String.format("{\"releasedData\";\"%s\"}", uuid);
    }

    private long now() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public String releaseAllData() {
        List<String> releasedUuid = new ArrayList<>();
        lockedData.forEach((uuid, tLockableData) -> {
            releasedUuid.add(uuid);
            registerData(tLockableData.getObject());
        });
        lockedData = new ConcurrentHashMap<>();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("releasedData", new Gson().toJsonTree(releasedUuid));
        return jsonObject.toString();
    }

    public String getDataAsResponseJsonString(Function<Stream<T>, Optional<T>> filterFunction) {
        T neededData = null;

        //Check if the data with needed condition is present in the available data first
        Optional<T> availableData = filterFunction.apply(allData.parallelStream());

        //Check that the available data is not already locked
        Stream<T> lockedDataObjectsStream = lockedData.values().stream().map(LockableData::getObject);
        Optional<T> dataNotIOnLockedDataSet = availableData.filter(t -> !lockedDataObjectsStream.anyMatch(d -> d == t));


        if (dataNotIOnLockedDataSet.isPresent()) {
            neededData = dataNotIOnLockedDataSet.get();
            return getDataAsJsonResponse(lockData(neededData), neededData);
        } else {
            //if the neede data is not present in the unlocked data pool, check whether any other data in the locked data is expired
            List<LockableData<T>> objectsExceedingTimeout = lockedData.values().stream()
                    .filter(tLockableData -> now() - tLockableData.getLockedTime() > lockTimeoutInMilliSeconds)
                    .sorted((o1, o2) -> (int) ((o1.getLockedTime() - o2.getLockedTime()) / 1000))
                    .collect(Collectors.toList());

            Optional<T> neededObjectInLockedObjects = filterFunction.apply(objectsExceedingTimeout.stream().map(LockableData::getObject));

            if (neededObjectInLockedObjects.isPresent()) {
                neededData = neededObjectInLockedObjects.get();
                final T finalNeededData = neededData;
                LockableData<T> objectExceedingTimeout = lockedData.values().stream().filter(tLockableData -> tLockableData.getObject().equals(finalNeededData)).findFirst().get();
                lockedData.values().remove(objectExceedingTimeout);
                logger.info("Releasing object as exceeding the threshold timeout" + lockTimeoutInMilliSeconds + " milliseconds.");


            }
        }
        if (neededData != null) {
            neededData = dataNotIOnLockedDataSet.get();
            return getDataAsJsonResponse(lockData(neededData), neededData);
        } else {
            throw new NotFoundException("User not found");
        }


    }


    private String getDataAsJsonResponse(String uuid, T data) {
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("id", uuid);
        responseJson.addProperty("data", new Gson().toJson(data));
        return responseJson.toString();
    }

}

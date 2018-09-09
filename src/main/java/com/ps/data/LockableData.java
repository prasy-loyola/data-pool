package com.ps.data;

import java.util.Calendar;

public class LockableData<T> {
    public LockableData(T data) {
        this.object=data;
        this.lockedTime= Calendar.getInstance().getTimeInMillis();
    }

    private T object;
    private long lockedTime;

    public T getObject() {
        return object;
    }

    public long getLockedTime() {
        return lockedTime;
    }
}

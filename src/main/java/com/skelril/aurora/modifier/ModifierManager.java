/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.modifier;

import java.util.EnumMap;
import java.util.Map;

public class ModifierManager {

    // The long represents the end time
    private Map<ModifierType, Long> times = new EnumMap<>(ModifierType.class);

    public void extend(ModifierType type, long amount) {
        Long time = times.get(type);
        long curTime = System.currentTimeMillis();
        if (time != null && time > curTime) {
            time += amount;
        } else {
            time = curTime + amount;
        }
        set(type, time);
    }

    public void set(ModifierType type, long expiry) {
        times.put(type, expiry);
    }

    public long get(ModifierType type) {
        Long time = times.get(type);
        return time == null ? 0 : time;
    }

    public boolean isActive(ModifierType type) {
        return status(type) != 0;
    }

    public long status(ModifierType type) {
        return Math.max(get(type) - System.currentTimeMillis(), 0);
    }
}

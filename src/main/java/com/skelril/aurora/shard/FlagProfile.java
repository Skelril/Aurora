/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard;

import com.sk89q.worldguard.protection.flags.Flag;

import java.util.HashMap;
import java.util.Map;

public class FlagProfile {

    private Map<Flag<?>, Object> mapping = new HashMap<>();

    /**
     * Set a flag's value.
     *
     * @param <T> The flag type
     * @param <V> The type of the flag's value
     * @param flag The flag to check
     * @param val The value to set
     */
    public <T extends Flag<V>, V> void setFlag(T flag, V val) {
        if (val == null) {
            mapping.remove(flag);
        } else {
            mapping.put(flag, val);
        }
    }

    public Map<Flag<?>, Object> construct() {
        Map<Flag<?>, Object> newMap = new HashMap<>();;
        for (Map.Entry<Flag<?>, Object> entry : mapping.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue());
        }
        return newMap;
    }
}

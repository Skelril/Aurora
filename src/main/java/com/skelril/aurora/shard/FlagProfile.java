/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * This file is part of Aurora.
 *
 * Aurora is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Aurora.  If not, see <http://www.gnu.org/licenses/>.
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

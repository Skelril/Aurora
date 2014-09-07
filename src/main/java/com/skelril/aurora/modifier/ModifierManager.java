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

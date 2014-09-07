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

package com.skelril.aurora.prayer;

import com.skelril.aurora.prayer.PrayerFX.AbstractEffect;
import com.skelril.aurora.prayer.PrayerFX.AbstractTriggeredEffect;
import org.bukkit.entity.Player;

public class Prayer implements Comparable<Prayer> {

    private final Player player;
    private final AbstractEffect abstractEffect;
    private final long startTime;
    private long maxDuration;


    protected Prayer(Player player, AbstractEffect abstractEffect, long maxDuration) {

        this.player = player;
        this.abstractEffect = abstractEffect;
        this.startTime = System.currentTimeMillis();
        this.maxDuration = maxDuration;
    }

    public Player getPlayer() {

        return player;
    }

    public AbstractEffect getEffect() {

        return abstractEffect;
    }

    public long getStartTime() {

        return startTime;
    }

    public long getMaxDuration() {

        return maxDuration;
    }

    public void setMaxDuration(long maxDuration) {

        this.maxDuration = maxDuration;
    }

    public boolean hasTrigger() {

        return abstractEffect instanceof AbstractTriggeredEffect;
    }

    public Class getTriggerClass() {

        return hasTrigger() ? ((AbstractTriggeredEffect) abstractEffect).getTriggerClass() : null;
    }

    @Override
    public int compareTo(Prayer prayer) {

        if (prayer == null) return 0;

        if (this.getEffect().getType().getValue() == prayer.getEffect().getType().getValue()) return 0;
        if (this.getEffect().getType().getValue() > prayer.getEffect().getType().getValue()) return 1;
        return -1;
    }
}

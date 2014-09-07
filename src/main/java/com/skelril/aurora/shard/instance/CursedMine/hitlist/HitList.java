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

package com.skelril.aurora.shard.instance.CursedMine.hitlist;

import com.skelril.aurora.shard.instance.CursedMine.CursedMineInstance;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HitList {

    private Map<String, HitListRecord> hitList = new HashMap<>();

    public void addPlayer(Player player, CursedMineInstance inst) {
        long expiryTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10);
        hitList.put(player.getName(), new HitListRecord(expiryTime, inst));
    }

    public void remPlayer(Player player) {
        hitList.remove(player.getName());
    }

    public boolean isOnHitList(Player player) {
        return hitList.containsKey(player.getName());
    }

    public CursedMineInstance getAssigningInstance(Player player) {
        if (!isOnHitList(player)) {
            return null;
        }
        return hitList.get(player.getName()).getInst();
    }

    public void check() {
        Iterator<Map.Entry<String, HitListRecord>> it = hitList.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().hasExpired()) it.remove();
        }
    }
}

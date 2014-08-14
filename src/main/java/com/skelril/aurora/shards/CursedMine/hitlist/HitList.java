/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards.CursedMine.hitlist;

import com.skelril.aurora.shards.CursedMine.CursedMineInstance;
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

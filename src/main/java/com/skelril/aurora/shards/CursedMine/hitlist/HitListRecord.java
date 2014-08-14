/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards.CursedMine.hitlist;

import com.skelril.aurora.shards.CursedMine.CursedMineInstance;

public class HitListRecord {

    private long expiryTime;
    private CursedMineInstance inst;

    public HitListRecord(long expiryTime, CursedMineInstance inst) {
        this.expiryTime = expiryTime;
        this.inst = inst;
    }

    public boolean hasExpired() {
        return expiryTime < System.currentTimeMillis();
    }

    public CursedMineInstance getInst() {
        return inst;
    }
}

/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.Catacombs;

import com.skelril.OpenBoss.EntityDetail;
import com.skelril.aurora.combat.bosses.detail.SBossDetail;
import com.skelril.aurora.shard.ShardInstance;

public class CatacombEntityDetail extends SBossDetail {

    private final int wave;

    public CatacombEntityDetail(ShardInstance<?> instance, int wave) {
        super(instance);
        this.wave = wave;
    }

    public int getWave() {
        return wave;
    }

    public static CatacombEntityDetail getFrom(EntityDetail detail) {
        if (detail instanceof CatacombEntityDetail) {
            return (CatacombEntityDetail) detail;
        }
        return null;
    }
}

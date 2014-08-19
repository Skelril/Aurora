/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.combat.bosses.detail;

import com.skelril.OpenBoss.EntityDetail;
import com.skelril.aurora.shard.ShardInstance;

public class SBossDetail implements EntityDetail {
    public final ShardInstance<?> instance;

    public SBossDetail(ShardInstance<?> instance) {
        this.instance = instance;
    }

    public ShardInstance<?> getInstance() {
        return instance;
    }
}

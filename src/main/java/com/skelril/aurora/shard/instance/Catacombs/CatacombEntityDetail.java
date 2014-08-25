/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.Catacombs;

import com.skelril.OpenBoss.EntityDetail;
import com.skelril.aurora.combat.bosses.detail.SBossDetail;
import com.skelril.aurora.shard.ShardInstance;
import org.bukkit.entity.LivingEntity;

import java.lang.ref.WeakReference;

public class CatacombEntityDetail extends SBossDetail {

    private final int wave;
    private WeakReference<LivingEntity> marked = new WeakReference<>(null);

    public CatacombEntityDetail(ShardInstance<?> instance, int wave) {
        super(instance);
        this.wave = wave;
    }

    public int getWave() {
        return wave;
    }

    public LivingEntity getMarked() {
        return marked.get();
    }

    public void setMarked(LivingEntity marked) {
        this.marked = new WeakReference<>(marked);
    }

    public static CatacombEntityDetail getFrom(EntityDetail detail) {
        if (detail instanceof CatacombEntityDetail) {
            return (CatacombEntityDetail) detail;
        }
        return null;
    }
}

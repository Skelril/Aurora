/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards.FreakyFour;

import org.bukkit.entity.*;

public enum FreakyFourBoss {
    CHARLOTTE(Spider.class),
    FRIMUS(Blaze.class),
    DA_BOMB(Creeper.class),
    SNIPEE(Skeleton.class);

    private Class<? extends LivingEntity> clazz;

    private FreakyFourBoss(Class<? extends LivingEntity> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends LivingEntity> getEntityClass() {
        return clazz;
    }
}

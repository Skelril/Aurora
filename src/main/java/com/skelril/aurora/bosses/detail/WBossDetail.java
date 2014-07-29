/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.bosses.detail;

import com.skelril.OpenBoss.EntityDetail;

public class WBossDetail implements EntityDetail {

    private int level;

    public WBossDetail(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public static int getLevel(EntityDetail detail) {
        if (detail instanceof WBossDetail) {
            return ((WBossDetail) detail).getLevel();
        }
        return 1;
    }
}

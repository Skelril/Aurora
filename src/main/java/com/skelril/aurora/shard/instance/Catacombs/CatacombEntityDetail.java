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

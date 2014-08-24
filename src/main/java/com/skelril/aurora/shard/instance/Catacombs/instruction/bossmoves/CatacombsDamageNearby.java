/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.Catacombs.instruction.bossmoves;

import com.skelril.OpenBoss.Boss;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.aurora.combat.bosses.instruction.DamageNearby;
import com.skelril.aurora.shard.instance.Catacombs.CatacombsInstance;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class CatacombsDamageNearby extends DamageNearby {

    @Override
    public boolean checkTarget(Boss boss, LivingEntity entity) {
        CatacombsInstance inst = CatacombsInstance.getInst(boss.getDetail());
        return entity instanceof Player && inst.contains(entity);
    }

    @Override
    public double getDamage(EntityDetail detail) {
        return ChanceUtil.getRandom(20);
    }

    @Override
    public void damage(Boss boss, LivingEntity entity) {
        super.damage(boss, entity);
        if (entity instanceof Player) {
            ChatUtil.sendWarning((Player) entity, "The boss sends some of the damage back to you");
        }
    }
}

/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.items.implementations;

import com.skelril.aurora.events.custom.item.ChanceActivationEvent;
import com.skelril.aurora.items.generic.AbstractXPArmor;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.extractor.entity.CombatantPair;
import com.skelril.aurora.util.extractor.entity.EDBEExtractor;
import com.skelril.aurora.util.item.EffectUtil;
import com.skelril.aurora.util.item.ItemUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import static com.zachsthings.libcomponents.bukkit.BasePlugin.callEvent;

public class ElderArmorImpl extends AbstractXPArmor {
    @Override
    public boolean hasArmor(Player player) {
        return ItemUtil.hasElderArmor(player);
    }

    @Override
    public int modifyXP(int startingAmt) {
        return startingAmt;
    }

    private static EDBEExtractor<LivingEntity, Player, Projectile> ancientExtractor = new EDBEExtractor<>(
            LivingEntity.class,
            Player.class,
            Projectile.class
    );

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void powerBurst(EntityDamageByEntityEvent event) {

        CombatantPair<LivingEntity, Player, Projectile> result = ancientExtractor.extractFrom(event);

        if (result == null) return;

        Player defender = result.getDefender();
        LivingEntity attacker = result.getAttacker();
        if (!(attacker instanceof Player) && hasArmor(defender)) {
            ChanceActivationEvent activationEvent = new ChanceActivationEvent(
                    defender,
                    defender.getLocation(),
                    17,
                    ChanceActivationEvent.ChanceType.ARMOR
            );
            callEvent(activationEvent);
            if (activationEvent.isCancelled() || !ChanceUtil.getChance(activationEvent.getChance())) return;
            EffectUtil.Ancient.powerBurst(defender, event.getDamage());
        }
    }
}

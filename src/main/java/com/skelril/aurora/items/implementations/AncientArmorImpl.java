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

public class AncientArmorImpl extends AbstractXPArmor {
    @Override
    public boolean hasArmor(Player player) {
        return ItemUtil.hasAncientArmor(player);
    }

    @Override
    public int modifyXP(int startingAmt) {
        return ChanceUtil.getRandom(ChanceUtil.getRandom(startingAmt * 2));
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

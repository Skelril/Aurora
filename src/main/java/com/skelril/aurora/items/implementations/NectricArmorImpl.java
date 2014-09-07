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

import com.skelril.aurora.items.generic.AbstractItemFeatureImpl;
import com.skelril.aurora.prayer.Prayer;
import com.skelril.aurora.prayer.PrayerComponent;
import com.skelril.aurora.prayer.PrayerFX.NecrosisFX;
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

public class NectricArmorImpl extends AbstractItemFeatureImpl {

    private static EDBEExtractor<LivingEntity, Player, Projectile> necrosisExtractor = new EDBEExtractor<>(
            LivingEntity.class,
            Player.class,
            Projectile.class
    );

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void necrosis(EntityDamageByEntityEvent event) {

        CombatantPair<LivingEntity, Player, Projectile> result = necrosisExtractor.extractFrom(event);

        if (result == null) return;

        Player defender = result.getDefender();
        if (ItemUtil.hasNectricArmor(defender) && ChanceUtil.getChance(4)) {
            LivingEntity attacker = result.getAttacker();
            if (attacker instanceof Player) {
                NecrosisFX necrosis = new NecrosisFX();
                necrosis.setBeneficiary(defender);
                Prayer prayer = PrayerComponent.constructPrayer((Player) attacker, necrosis, 5000);
                prayers.influencePlayer((Player) attacker, prayer);
                defender.chat("Taste necrosis!");
            } else {
                EffectUtil.Necros.deathStrike(defender, Math.max(5, event.getDamage()));
            }
        }
    }
}

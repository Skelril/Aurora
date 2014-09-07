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
import com.skelril.aurora.events.wishingwell.PlayerItemWishEvent;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.items.generic.AbstractItemFeatureImpl;
import com.skelril.aurora.items.generic.weapons.SpecWeaponImpl;
import com.skelril.aurora.items.specialattack.SpecialAttack;
import com.skelril.aurora.items.specialattack.attacks.melee.master.Blind;
import com.skelril.aurora.items.specialattack.attacks.melee.master.DoomBlade;
import com.skelril.aurora.items.specialattack.attacks.melee.master.HealingLight;
import com.skelril.aurora.items.specialattack.attacks.melee.master.UltimateStrength;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.item.ItemUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import static com.zachsthings.libcomponents.bukkit.BasePlugin.callEvent;

public class MasterSwordImpl extends AbstractItemFeatureImpl implements SpecWeaponImpl {

    private static final int BASE_CHANCE = 17;

    @Override
    public boolean activate(LivingEntity owner, LivingEntity target) {
        if (!(owner instanceof Player)) {
            return ChanceUtil.getChance(BASE_CHANCE);
        }
        ChanceActivationEvent activationEvent = new ChanceActivationEvent(
                (Player) owner,
                target.getLocation(),
                BASE_CHANCE,
                ChanceActivationEvent.ChanceType.WEAPON
        );
        callEvent(activationEvent);
        return !activationEvent.isCancelled() && ChanceUtil.getChance(activationEvent.getChance());
    }

    @Override
    public SpecialAttack getSpecial(LivingEntity owner, LivingEntity target) {
        switch (ChanceUtil.getRandom(4)) {
            case 1:
                return new Blind(owner, target);
            case 2:
                return new DoomBlade(owner, target);
            case 3:
                return new HealingLight(owner, target);
            case 4:
                return new UltimateStrength(owner, target);
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSacrifice(PlayerItemWishEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemStack();
        if (ItemUtil.isItem(item, CustomItems.MASTER_SWORD)) {
            item.setDurability((short) 0);
            player.getInventory().addItem(item);
            event.setItemStack(null);
        }
    }
}

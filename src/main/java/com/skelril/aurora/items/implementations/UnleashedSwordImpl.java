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

import com.skelril.aurora.events.wishingwell.PlayerItemWishEvent;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.items.generic.AbstractItemFeatureImpl;
import com.skelril.aurora.items.generic.weapons.SpecWeaponImpl;
import com.skelril.aurora.items.specialattack.SpecialAttack;
import com.skelril.aurora.items.specialattack.attacks.hybrid.unleashed.EvilFocus;
import com.skelril.aurora.items.specialattack.attacks.hybrid.unleashed.LifeLeech;
import com.skelril.aurora.items.specialattack.attacks.hybrid.unleashed.Speed;
import com.skelril.aurora.items.specialattack.attacks.melee.unleashed.DoomBlade;
import com.skelril.aurora.items.specialattack.attacks.melee.unleashed.HealingLight;
import com.skelril.aurora.items.specialattack.attacks.melee.unleashed.Regen;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.item.ItemUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

public class UnleashedSwordImpl extends AbstractItemFeatureImpl implements SpecWeaponImpl {
    @Override
    public boolean activate(LivingEntity owner, LivingEntity target) {
        return true;
    }

    @Override
    public SpecialAttack getSpecial(LivingEntity owner, LivingEntity target) {
        switch (ChanceUtil.getRandom(6)) {
            case 1:
                return new EvilFocus(owner, target);
            case 2:
                return new HealingLight(owner, target);
            case 3:
                return new Speed(owner, target);
            case 4:
                return new Regen(owner, target);
            case 5:
                return new DoomBlade(owner, target);
            case 6:
                return new LifeLeech(owner, target);
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSacrifice(PlayerItemWishEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemStack();
        int c;
        int o = 1;
        int m = item.getType().getMaxDurability();
        ItemStack[] i;
        if (ItemUtil.isItem(item, CustomItems.UNLEASHED_SWORD)) {
            //if (!isInRewardsRoom) {
            //    o = 2;
            //}
            c = ItemUtil.countItemsOfName(player.getInventory().getContents(), CustomItems.IMBUED_CRYSTAL.toString());
            i = ItemUtil.removeItemOfName(player.getInventory().getContents(), CustomItems.IMBUED_CRYSTAL.toString());
            player.getInventory().setContents(i);
            while (item.getDurability() > 0 && c >= o) {
                item.setDurability((short) Math.max(0, item.getDurability() - (m / 9)));
                c -= o;
            }
            player.getInventory().addItem(item);
            int amount = Math.min(c, 64);
            while (amount > 0) {
                player.getInventory().addItem(CustomItemCenter.build(CustomItems.IMBUED_CRYSTAL, amount));
                c -= amount;
                amount = Math.min(c, 64);
            }
            event.setItemStack(null);
        }
    }
}

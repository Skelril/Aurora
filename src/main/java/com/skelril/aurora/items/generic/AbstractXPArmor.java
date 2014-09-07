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

package com.skelril.aurora.items.generic;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractXPArmor extends AbstractItemFeatureImpl {

    public abstract boolean hasArmor(Player player);

    public abstract int modifyXP(int startingAmt);

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onXPPickUp(PlayerExpChangeEvent event) {

        Player player = event.getPlayer();

        int origin = event.getAmount();
        int exp = modifyXP(origin);

        if (hasArmor(player)) {
            ItemStack[] armor = player.getInventory().getArmorContents();
            do {
                double ratio = 0;
                ItemStack is = null;
                for (ItemStack armorPiece : armor) {
                    double cRatio = (double) armorPiece.getDurability() / armorPiece.getType().getMaxDurability();
                    if (cRatio > ratio) {
                        ratio = cRatio;
                        is = armorPiece;
                    }
                }
                if (is == null) break;
                if (exp > is.getDurability()) {
                    exp -= is.getDurability();
                    is.setDurability((short) 0);
                } else {
                    is.setDurability((short) (is.getDurability() - exp));
                    exp = 0;
                }
            } while (exp > 0);
            player.getInventory().setArmorContents(armor);
            event.setAmount(Math.min(exp, origin));
        }
    }
}

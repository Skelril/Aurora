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

import com.sk89q.worldedit.blocks.ItemID;
import com.skelril.aurora.events.custom.item.HymnSingEvent;
import com.skelril.aurora.items.generic.AbstractItemFeatureImpl;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import org.bukkit.Location;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

public class ChickenHymnImpl extends AbstractItemFeatureImpl {
    @EventHandler(ignoreCancelled = true)
    public void onHymnSing(HymnSingEvent event) {
        Player player = event.getPlayer();
        HymnSingEvent.Hymn hymn = event.getHymn();

        if (hymn != HymnSingEvent.Hymn.CHICKEN) return;
        player.getNearbyEntities(4, 4, 4).stream()
                .filter(e -> (e instanceof Item || e instanceof Chicken)).limit(30).forEach(e -> {
            Location l = e.getLocation();
            if (e instanceof Item) {
                for (int i = 0; i < 3; i++) {
                    Chicken chicken = l.getWorld().spawn(l, Chicken.class);
                    chicken.setRemoveWhenFarAway(true);
                }
                e.remove();
                ChatUtil.send(player, "The item transforms into chickens!");
            } else if (((Chicken) e).getRemoveWhenFarAway()) {
                if (ChanceUtil.getChance(3)) {
                    l.getWorld().dropItem(l, new ItemStack(ItemID.COOKED_CHICKEN));
                }
                e.remove();
            }
        });
    }
}

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

import com.skelril.aurora.events.custom.item.HymnSingEvent;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.items.generic.AbstractItemFeatureImpl;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.item.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

import static com.zachsthings.libcomponents.bukkit.BasePlugin.callEvent;

public class HymnImpl extends AbstractItemFeatureImpl {

    private Map<CustomItems, HymnSingEvent.Hymn> hymns = new HashMap<>();

    public void addHymn(CustomItems item, HymnSingEvent.Hymn hymn) {
        hymns.put(item, hymn);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            for (Map.Entry<CustomItems, HymnSingEvent.Hymn> entry : hymns.entrySet()) {
                if (ItemUtil.isItem(itemStack, entry.getKey())) {
                    ChatUtil.send(player, "You sing the hymn...");
                    callEvent(new HymnSingEvent(player, entry.getValue()));
                    break;
                }
            }
        }
    }
}

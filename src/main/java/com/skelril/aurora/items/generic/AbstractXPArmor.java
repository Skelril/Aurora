/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.items.generic;

import com.skelril.aurora.util.CollectionUtil;
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
            ItemStack is = CollectionUtil.getElement(armor);
            if (exp > is.getDurability()) {
                exp -= is.getDurability();
                is.setDurability((short) 0);
            } else {
                is.setDurability((short) (is.getDurability() - exp));
                exp = 0;
            }
            player.getInventory().setArmorContents(armor);
            event.setAmount(Math.min(exp, origin));
        }
    }
}

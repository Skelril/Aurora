/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.items.implementations;

import com.skelril.aurora.events.wishingwell.PlayerItemWishEvent;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.items.generic.AbstractItemFeatureImpl;
import com.skelril.aurora.util.item.ItemUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

public class PhantomGoldImpl extends AbstractItemFeatureImpl {

    private Economy econ;

    public PhantomGoldImpl(Economy econ) {
        this.econ = econ;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSacrifice(PlayerItemWishEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemStack();
        if (ItemUtil.isItem(item, CustomItems.PHANTOM_GOLD)) {
            int amount = 50;
            econ.depositPlayer(player, amount * item.getAmount());
            event.setItemStack(null);
        }
    }
}

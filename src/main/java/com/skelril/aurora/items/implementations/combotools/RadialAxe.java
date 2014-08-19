/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.items.implementations.combotools;

import com.skelril.aurora.items.algorithem.RadialExecutor;
import com.skelril.aurora.items.custom.CustomItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

public class RadialAxe extends ComboAxe {

    private static RadialExecutor executor = new RadialExecutor(CustomItems.RADIAL_AXE) {
        @Override
        public boolean accepts(int type, int data) {
            return acceptedTypes.contains(type);
        }
    };

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(PlayerInteractEvent event) {
        executor.process(event);
    }
}

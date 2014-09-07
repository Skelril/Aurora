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

package com.skelril.aurora.items.implementations.combotools;

import com.skelril.aurora.items.algorithem.LinearExecutor;
import com.skelril.aurora.items.custom.CustomItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

public class LinearPickaxe extends ComboPickaxe {

    private static LinearExecutor executor = new LinearExecutor(CustomItems.LINEAR_PICKAXE) {
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

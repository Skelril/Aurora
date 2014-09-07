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

package com.skelril.aurora;

import com.sk89q.commandbook.CommandBook;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.Server;
import org.bukkit.event.Listener;

import java.util.logging.Logger;

@ComponentInformation(friendlyName = "Cleansweep", desc = "Cleanup")
public class CleansweepComponent extends BukkitComponent implements Listener {

    private CommandBook inst = CommandBook.inst();
    private Logger log = inst.getLogger();
    private Server server = CommandBook.server();

    @Override
    public void enable() {

        //inst.registerEvents(this);
    }

    /*
     *
     * Cleans out all containers
     *
     * WARNING: THIS CODE IS EXTREMELY DANGEROUS AND WILL CLEAR WITHOUT DISCRIMINATION
     *
     *
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {

        for (BlockState bState : event.getChunk().getTileEntities()) {
            if (bState instanceof Chest) {
                ((Chest) bState).getInventory().clear();
                bState.update(true);
            }

            if (bState instanceof Dispenser) {
                ((Dispenser) bState).getInventory().clear();
                bState.update(true);
            }

            if (bState instanceof Furnace) {
                ((Furnace) bState).getInventory().clear();
                bState.update(true);
            }

            if (bState instanceof BrewingStand) {
                ((BrewingStand) bState).getInventory().clear();
                bState.update(true);
            }
        }
    }
    */
}
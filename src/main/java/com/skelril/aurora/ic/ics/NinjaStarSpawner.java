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

package com.skelril.aurora.ic.ics;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.*;
import com.sk89q.craftbook.util.LocationUtil;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.items.custom.CustomItems;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;

import java.util.logging.Logger;

public class NinjaStarSpawner extends AbstractSelfTriggeredIC {

    private static final CommandBook inst = CommandBook.inst();
    private static final Logger log = inst.getLogger();
    private static final Server server = CommandBook.server();

    private int quantity;

    public NinjaStarSpawner(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {

        try {
            quantity = Integer.parseInt(getSign().getLine(2));
        } catch (NumberFormatException ex) {
            quantity = 1;
        }
    }

    @Override
    public String getTitle() {

        return "Star Spawner";
    }

    @Override
    public String getSignTitle() {

        return "STAR SPAWNER";
    }

    @Override
    public void trigger(ChipState chip) {

        drop();
    }

    @Override
    public void think(ChipState chip) {

        if (!chip.getInput(0)) {
            trigger(chip);
        }
    }

    public void drop() {

        Location k = LocationUtil.getCenterOfBlock(LocationUtil.getNextFreeSpace(getBackBlock(), BlockFace.UP));
        final Item item = k.getWorld().dropItem(k, CustomItemCenter.build(CustomItems.NINJA_STAR, quantity));
        server.getScheduler().runTaskLater(inst, () -> {
            if (item.isValid()) {
                item.remove();
            }
        }, 20 * 15);
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new NinjaStarSpawner(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Spawns Ninja Stars.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[]{"Quantity", ""};
        }
    }
}
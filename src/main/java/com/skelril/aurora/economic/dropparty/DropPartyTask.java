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

package com.skelril.aurora.economic.dropparty;

import com.sk89q.worldedit.regions.CuboidRegion;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.LocationUtil;
import com.skelril.aurora.util.checker.RegionChecker;
import com.skelril.aurora.util.timer.IntegratedRunnable;
import com.skelril.aurora.util.timer.TimedRunnable;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Iterator;
import java.util.List;

public class DropPartyTask {

    private TimedRunnable runnable;
    private World world;
    private CuboidRegion rg;
    private List<ItemStack> items;
    private RegionChecker checker;
    private int xpAmt = 0;
    private int xpSize = 0;

    public DropPartyTask(World world, CuboidRegion rg, List<ItemStack> items, RegionChecker checker) {
        this.world = world;
        this.rg = rg;
        this.items = items;
        this.checker = checker;
        this.runnable = new TimedRunnable(create(), (int) (items.size() * .15) + 1);
    }

    public void start(Plugin plugin, BukkitScheduler scheduler) {
        start(plugin, scheduler, 0, 20);
    }

    public void start(Plugin plugin, BukkitScheduler scheduler, long delay, long interval) {
        runnable.setTask(scheduler.runTaskTimer(plugin, runnable, delay, interval));
    }

    public World getWorld() {
        return world;
    }

    public void setXPChance(int amt) {
        xpAmt = amt;
    }

    public void setXPSize(int size) {
        xpSize = size;
    }

    private IntegratedRunnable create() {
        return new IntegratedRunnable() {
            @Override
            public boolean run(int times) {
                Iterator<ItemStack> it = items.iterator();

                for (int k = 10; it.hasNext() && k > 0; k--) {

                    // Pick a random Location
                    Location l = LocationUtil.pickLocation(world, rg.getMaximumY(), checker);
                    if (!world.getChunkAt(l).isLoaded()) world.getChunkAt(l).load(true);
                    world.dropItem(l, it.next());

                    // Remove the drop
                    it.remove();

                    // Drop the xp
                    if (xpAmt > 0) {
                        // Throw in some xp cause why not
                        for (int s = ChanceUtil.getRandom(xpAmt); s > 0; --s) {
                            ExperienceOrb e = world.spawn(l, ExperienceOrb.class);
                            e.setExperience(xpSize);
                        }
                    }
                }

                // Cancel if we've ran out of drop party pulses or if there is nothing more to drop
                if (items.isEmpty()) {
                    runnable.cancel();
                }
                return true;
            }

            @Override
            public void end() {

            }
        };
    }
}

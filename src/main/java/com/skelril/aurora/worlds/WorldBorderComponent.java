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

package com.skelril.aurora.worlds;

import com.sk89q.commandbook.CommandBook;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.util.ChatUtil;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.InjectComponent;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import com.zachsthings.libcomponents.config.ConfigurationBase;
import com.zachsthings.libcomponents.config.Setting;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

@ComponentInformation(friendlyName = "World Border", desc = "A World Border enforcer")
@Depend(components = {AdminComponent.class})
public class WorldBorderComponent extends BukkitComponent implements Runnable {

    private final CommandBook inst = CommandBook.inst();
    private final Logger log = inst.getLogger();
    private final Server server = CommandBook.server();

    @InjectComponent
    AdminComponent adminComponent;

    private LocalConfiguration config;
    private World world;

    @Override
    public void enable() {

        config = configure(new LocalConfiguration());

        this.world = Bukkit.getWorld("City");
        server.getScheduler().runTaskTimer(inst, this, 0, 20 * 8);
    }

    @Override
    public void reload() {

        super.reload();
        configure(config);
    }

    @Override
    public void run() {

        Location pLoc = new Location(world, 0, 0, 0);
        Location origin;

        for (Player player : server.getOnlinePlayers()) {

            pLoc = player.getLocation(pLoc);
            origin = pLoc.clone();

            int x = pLoc.getBlockX();
            int bx = player.getWorld().equals(world) ? config.maxX : 10000;
            int sx = player.getWorld().equals(world) ? config.minX : -10000;
            int y = pLoc.getBlockY();
            int by = config.maxY;
            int z = pLoc.getBlockZ();
            int bz = player.getWorld().equals(world) ? config.maxZ : 10000;
            int sz = player.getWorld().equals(world) ? config.minZ : -10000;

            if (x > bx) {
                pLoc.setX(bx);
            } else if (x < sx) {
                pLoc.setX(sx);
            }

            if (y > by && player.getAllowFlight() && !adminComponent.isAdmin(player)) {
                pLoc.setY(by);
            }

            if (z > bz) {
                pLoc.setZ(bz);
            } else if (z < sz) {
                pLoc.setZ(sz);
            }

            if (!pLoc.equals(origin)) {
                Entity v = player.getVehicle();
                if (v == null) {
                    player.teleport(pLoc);
                } else {
                    v.eject();
                    v.teleport(pLoc);
                    player.teleport(v);
                    v.setPassenger(player);
                }
                ChatUtil.send(player, "You have reached the end of the accessible area of this world.");
            }
        }
    }

    private static class LocalConfiguration extends ConfigurationBase {

        @Setting("max.x")
        public int maxX = 100;
        @Setting("min.x")
        public int minX = -100;
        @Setting("max.y")
        public int maxY = 300;
        @Setting("max.z")
        public int maxZ = 100;
        @Setting("min.z")
        public int minZ = -100;
    }
}
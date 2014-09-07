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
import com.skelril.aurora.events.entity.ProjectileTickEvent;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@ComponentInformation(friendlyName = "Projectile Watcher", desc = "Projectile Watcher.")
public class ProjectileWatchingComponent extends BukkitComponent implements Listener {

    private final CommandBook inst = CommandBook.inst();
    private final Logger log = CommandBook.logger();
    private final Server server = CommandBook.server();

    private Map<Integer, BukkitTask> projectileTask = new HashMap<>();
    private Map<Integer, Location> projectileLoc = new HashMap<>();

    @Override
    public void enable() {

        //noinspection AccessStaticViaInstance
        inst.registerEvents(this);
    }

    // Entity
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBowFire(EntityShootBowEvent event) {

        Entity p = event.getProjectile();

        startTracker(p, event.getForce());

        ItemStack bow = event.getBow();

        if (bow != null) {
            p.setMetadata("launcher", new FixedMetadataValue(inst, bow));
        }
        p.setMetadata("launch-force", new FixedMetadataValue(inst, event.getForce()));
    }

    // Not entity
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArrowFire(ProjectileLaunchEvent event) {

        startTracker(event.getEntity(), -1);
    }

    public boolean hasChangedLocation(Entity p) {

        return !projectileLoc.containsKey(p.getEntityId())
                || !projectileLoc.get(p.getEntityId()).equals(p.getLocation());
    }

    public void startTracker(final Entity projectile, final float force) {

        if (projectileTask.containsKey(projectile.getEntityId()) || !(projectile instanceof Projectile)) return;

        BukkitTask task = server.getScheduler().runTaskTimer(inst, () -> {
            Location loc = projectile.getLocation();

            if (projectile.isDead() || !hasChangedLocation(projectile)) {
                projectileLoc.remove(projectile.getEntityId());
                projectileTask.get(projectile.getEntityId()).cancel();
            } else {
                server.getPluginManager().callEvent(new ProjectileTickEvent((Projectile) projectile, force));
                projectileLoc.put(projectile.getEntityId(), loc);
            }
        }, 0, 1); // Start at 0 ticks and repeat every 1 ticks
        projectileTask.put(projectile.getEntityId(), task);
    }
}

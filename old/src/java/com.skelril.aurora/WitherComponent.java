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
import com.skelril.aurora.admin.AdminComponent;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.InjectComponent;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.Server;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.logging.Logger;

@ComponentInformation(friendlyName = "Wither", desc = "Watch dem withers")
@Depend(components = {AdminComponent.class})
public class WitherComponent extends BukkitComponent implements Listener {

    private final CommandBook inst = CommandBook.inst();
    private final Logger log = CommandBook.logger();
    private final Server server = CommandBook.server();

    @InjectComponent
    private AdminComponent adminComponent;

    @Override
    public void enable() {

        //noinspection AccessStaticViaInstance
        //inst.registerEvents(this);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCreatePortal(EntityDeathEvent event) {

        LivingEntity e = event.getEntity();
        if (e.getType() != null && e.getType().equals(EntityType.WITHER)) {
            if (e.getKiller() == null || adminComponent.isAdmin(e.getKiller())) event.getDrops().clear();
        }
    }
}

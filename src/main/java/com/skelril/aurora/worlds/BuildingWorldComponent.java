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

import com.sk89q.commandbook.locations.LocationManager;
import com.sk89q.commandbook.locations.LocationManagerFactory;
import com.sk89q.commandbook.locations.NamedLocation;
import com.sk89q.commandbook.locations.RootLocationManager;
import com.sk89q.commandbook.session.SessionComponent;
import com.skelril.aurora.admin.AdminComponent;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import com.zachsthings.libcomponents.config.ConfigurationBase;
import com.zachsthings.libcomponents.config.Setting;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.sk89q.commandbook.CommandBook.registerEvents;
import static com.sk89q.commandbook.locations.FlatFileLocationsManager.LocationsFactory;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

@ComponentInformation(friendlyName = "Building World Core", desc = "Operate the building worlds.")
@Depend(components = {AdminComponent.class, SessionComponent.class})
public class BuildingWorldComponent extends BukkitComponent implements Listener {

    private World primus;
    private Set<World> worlds = new HashSet<>();
    private RootLocationManager<NamedLocation> manager;


    private LocalConfiguration config;

    @Override
    public void enable() {
        config = configure(new LocalConfiguration());
        server().getScheduler().runTaskLater(inst(), this::setup, 1);
        registerEvents(this);
    }

    @Override
    public void reload() {
        super.reload();
        configure(config);
    }

    private void setup() {
        primus = Bukkit.getWorld(config.primusWorld);

        // Update Building Worlds
        worlds.clear();
        worlds.addAll(config.worlds.stream().map(Bukkit::getWorld).collect(Collectors.toList()));

        // Create the manager
        File workingDir = new File(inst().getDataFolder(), "/building");
        LocationManagerFactory<LocationManager<NamedLocation>> pFactory = new LocationsFactory(workingDir, "portals");
        manager = new RootLocationManager<>(pFactory, true);
    }

    public class LocalConfiguration extends ConfigurationBase {
        @Setting("primus.world")
        public String primusWorld = "Primus";
        @Setting("building-worlds")
        public List<String> worlds = Arrays.asList(
                "Sion"
        );
    }

    public NamedLocation getStored(Player player, World world) {
        if (!worlds.contains(world)) {
            return null;
        }
        return manager.get(world, player.getUniqueId().toString());
    }

    private Player getPassenger(Entity entity) {
        Entity passenger = entity.getPassenger();
        if (passenger instanceof Player) {
            return (Player) passenger;
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent event) {
        TravelAgent agent = event.getPortalTravelAgent();
        Player passenger = getPassenger(event.getEntity());
        if (passenger == null) return;
        Location to = commonPortal(event.getFrom());

        if (to != null) {
            agent.setCanCreatePortal(false);
            event.setPortalTravelAgent(agent);
            event.setTo(to);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        TravelAgent agent = event.getPortalTravelAgent();
        Location to = commonPortal(event.getFrom());

        if (!event.getCause().equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL)) return;
        if (to != null) {
            Player player = event.getPlayer();
            manager.create(player.getUniqueId().toString(), event.getFrom(), player);
            agent.setCanCreatePortal(false);
            event.setPortalTravelAgent(agent);
            event.setTo(to);
        }
    }

    private Location commonPortal(Location from) {
        World fromWorld = from.getWorld();

        Location to = null;

        if (worlds.contains(fromWorld)) {
            to = primus.getSpawnLocation();
        }
        return to;
    }
}

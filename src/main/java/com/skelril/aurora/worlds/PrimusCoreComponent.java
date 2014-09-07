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

import com.sk89q.commandbook.locations.NamedLocation;
import com.sk89q.commandbook.session.SessionComponent;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.events.shard.PartyActivateEvent;
import com.skelril.aurora.events.wishingwell.PlayerAttemptItemWishEvent;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.util.EnvironmentUtil;
import com.skelril.aurora.util.item.ItemUtil;
import com.skelril.aurora.util.item.PartyBookReader;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.InjectComponent;
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
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.sk89q.commandbook.CommandBook.registerEvents;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.callEvent;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

@ComponentInformation(friendlyName = "Primus Core", desc = "Operate the Primus World.")
@Depend(components = {AdminComponent.class, SessionComponent.class, BuildingWorldComponent.class})
public class PrimusCoreComponent extends BukkitComponent implements Listener {

    private WorldGuardPlugin WG;

    @InjectComponent
    private BuildingWorldComponent buildingWorld;

    private World primus, wilderness, buildingMain;

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
        WG = WorldGuardPlugin.inst();

        primus = Bukkit.getWorld(config.primusWorld);
        wilderness = Bukkit.getWorld(config.wildernessWorld);
        buildingMain = Bukkit.getWorld(config.mainWorld);
    }

    public class LocalConfiguration extends ConfigurationBase {
        @Setting("primus.world")
        public String primusWorld = "Primus";
        @Setting("wilderness.world")
        public String wildernessWorld = "Wilderness";
        @Setting("wilderness.portal-region")
        public String wildernessPortal = "wilderness-portal";
        @Setting("building.main-world.world")
        public String mainWorld = "Sion";
        @Setting("building.main-world.portal-region")
        public String mainWorldPortal = "sion-portal";
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
        Location to = commonPortal(passenger, event.getFrom());

        if (to != null) {
            agent.setCanCreatePortal(false);
            event.setPortalTravelAgent(agent);
            event.setTo(to);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        TravelAgent agent = event.getPortalTravelAgent();
        Location to = commonPortal(event.getPlayer(), event.getFrom());

        if (!event.getCause().equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL)) return;
        if (to != null) {
            agent.setCanCreatePortal(false);
            event.setPortalTravelAgent(agent);
            event.setTo(to);
        }
    }

    private Location commonPortal(Player player, Location from) {
        World fromWorld = from.getWorld();

        Location to = null;

        if (fromWorld.equals(primus)) {
            for (ProtectedRegion region : WG.getRegionManager(primus).getApplicableRegions(from)) {
                if (region.getId().equals(config.wildernessPortal)) {
                    to = wilderness.getSpawnLocation();
                    break;
                }
                if (region.getId().equals(config.mainWorldPortal)) {
                    NamedLocation stored = buildingWorld.getStored(player, buildingMain);
                    if (stored == null) {
                        to = buildingMain.getSpawnLocation();
                    } else {
                        to = stored.getLocation();
                    }
                    break;
                }
            }
        }
        return to;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerAttemptItemWish(PlayerAttemptItemWishEvent event) {

        ItemStack stack = event.getItemStack();

        boolean isPartyBook = ItemUtil.isItem(stack, CustomItems.PARTY_BOOK);

        boolean worldB = primus.equals(event.getLocation().getWorld());
        boolean waterB = EnvironmentUtil.isWater(event.getLocation().getBlock());

        if (!worldB && isPartyBook) {
            event.setResult(PlayerAttemptItemWishEvent.Result.ALLOW_IGNORE);
            return;
        }

        if (!worldB || !waterB) return;

        event.setResult(PlayerAttemptItemWishEvent.Result.ALLOW);
        if (isPartyBook) {
            event.setResult(PlayerAttemptItemWishEvent.Result.ALLOW_IGNORE);
        }

        PartyBookReader partyBook = PartyBookReader.getFrom(stack);
        if (partyBook == null) return;
        List<Player> players = new ArrayList<>();
        for (String player : partyBook.getAllPlayers()) {
            Player aPlayer = Bukkit.getPlayerExact(player);
            if (aPlayer != null) {
                players.add(aPlayer);
            }
        }
        callEvent(new PartyActivateEvent(partyBook.getShard(), players));
    }
}

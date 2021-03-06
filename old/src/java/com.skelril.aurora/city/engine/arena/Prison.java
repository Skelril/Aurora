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

package com.skelril.aurora.city.engine.arena;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.aurora.WishingWellComponent;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.events.PrayerApplicationEvent;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class Prison extends AbstractRegionedArena implements GenericArena, Listener {

    private final CommandBook inst = CommandBook.inst();
    private final Logger log = inst.getLogger();
    private final Server server = CommandBook.server();

    private AdminComponent adminComponent;

    private ProtectedRegion office;

    // Block - Is unlocked
    private Location rewardChest;

    public Prison(World world, ProtectedRegion[] regions, AdminComponent adminComponent) {

        super(world, regions[0]);

        this.office = regions[1];

        this.adminComponent = adminComponent;

        findRewardChest();     // Setup office

        //noinspection AccessStaticViaInstance
        inst.registerEvents(this);
    }

    private void findRewardChest() {

        com.sk89q.worldedit.Vector min = office.getMinimumPoint();
        com.sk89q.worldedit.Vector max = office.getMaximumPoint();

        int minX = min.getBlockX();
        int minZ = min.getBlockZ();
        int minY = min.getBlockY();
        int maxX = max.getBlockX();
        int maxZ = max.getBlockZ();
        int maxY = max.getBlockY();

        BlockState block;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = maxY; y >= minY; --y) {
                    block = getWorld().getBlockAt(x, y, z).getState();
                    if (!block.getChunk().isLoaded()) block.getChunk().load();
                    if (block.getTypeId() == BlockID.CHEST) {
                        rewardChest = block.getLocation();
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void run() {

        equalize();
    }

    @Override
    public void disable() {

        // Nothing to do here
    }

    @Override
    public String getId() {

        return getRegion().getId();
    }

    @Override
    public void equalize() {
        getContained(Player.class).forEach(adminComponent::standardizePlayer);
    }

    @Override
    public ArenaType getArenaType() {

        return ArenaType.MONITORED;
    }

    private Set<String> players = new HashSet<>();
    private static List<PlayerTeleportEvent.TeleportCause> accepted = new ArrayList<>();

    static {
        accepted.add(PlayerTeleportEvent.TeleportCause.UNKNOWN);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {

        if (contains(event.getTo()) && !accepted.contains(event.getCause())) {
            event.setCancelled(true);

            final String name = event.getPlayer().getName();
            if (players.contains(name)) {
                return;
            } else {
                players.add(name);
                server.getScheduler().runTaskLater(inst, () -> players.remove(name), 1);
            }
            ChatUtil.sendWarning(event.getPlayer(), "You cannot teleport to that location.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (event.isFlying() && contains(player) && !adminComponent.isAdmin(player)) {
            event.setCancelled(true);
            ChatUtil.send(player, "You cannot fly here!");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrayerApplication(PrayerApplicationEvent event) {

        if (event.getCause().getEffect().getType().isHoly() && contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        BlockState state = event.getClickedBlock().getLocation().getBlock().getState();
        if (state.getTypeId() == BlockID.CHEST && rewardChest.equals(state.getLocation())) {

            List<ItemStack> loot = WishingWellComponent.getCalculatedLoot(server.getConsoleSender(), 10, 4000);
            int lootSplit = ChanceUtil.getRangedRandom(64 * 2, 64 * 4);
            if (ChanceUtil.getChance(135)) lootSplit *= 10;
            else if (ChanceUtil.getChance(65)) lootSplit *= 2;

            event.setUseInteractedBlock(Event.Result.DENY);
            event.getPlayer().getInventory().addItem(new ItemStack(ItemID.GOLD_BAR, lootSplit));
            event.getPlayer().getInventory().addItem(loot.toArray(new ItemStack[loot.size()]));

            event.getPlayer().teleport(new Location(getWorld(), 256.18, 81, 136));
            ChatUtil.send(event.getPlayer(), "You have successfully raided the jail!");

            //noinspection deprecation
            event.getPlayer().updateInventory();
        }
    }
}

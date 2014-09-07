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

package com.skelril.aurora.shard;

import com.sk89q.commandbook.locations.TeleportSession;
import com.sk89q.commandbook.session.SessionComponent;
import com.sk89q.commandbook.util.entity.player.PlayerUtil;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.admin.AdminState;
import com.skelril.aurora.events.PlayerInstanceDeathEvent;
import com.skelril.aurora.events.shard.PartyActivateEvent;
import com.skelril.aurora.exceptions.UnknownPluginException;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.KeepAction;
import com.skelril.aurora.util.ProfileUtil;
import com.skelril.aurora.util.database.IOUtil;
import com.skelril.aurora.util.player.PlayerRespawnProfile_1_7_10;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.InjectComponent;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static com.sk89q.commandbook.CommandBook.*;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.callEvent;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

@ComponentInformation(friendlyName = "Shard Instance Manager", desc = "Shard Instancing")
public class ShardManagerComponent extends BukkitComponent implements Listener {

    @InjectComponent
    private AdminComponent admin;
    @InjectComponent
    private SessionComponent sessions;

    private HashMap<UUID, PlayerRespawnProfile_1_7_10> playerState = new HashMap<>();

    private WorldEditPlugin WE;
    private WorldGuardPlugin WG = WGBukkit.getPlugin();
    private ShardManager manager;
    private BukkitWorld shardWorld;

    @Override
    public void enable() {
        try {
            setUpWorldEdit();
        } catch (UnknownPluginException e) {
            e.printStackTrace();
        }
        server().getScheduler().runTaskLater(inst(), () -> {
            shardWorld = new BukkitWorld(Bukkit.getWorld("Exemplar"));
            manager = new ShardManager(shardWorld, WG.getRegionManager(shardWorld.getWorld()));
        }, 1);
        reloadData();
        server().getScheduler().runTaskTimer(inst(), this::writeData, 5, 5 * 20);
        registerCommands(Commands.class);
        registerEvents(this);
    }

    private void setUpWorldEdit() throws UnknownPluginException {
        Plugin plugin = server().getPluginManager().getPlugin("WorldEdit");

        // WorldEdit may not be loaded
        if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
            throw new UnknownPluginException("WorldEdit");
        }

        WE = (WorldEditPlugin) plugin;
    }

    @Override
    public void disable() {
        super.disable();
        writeData();
    }

    public ShardManager getManager() {
        return manager;
    }

    public World getShardWorld() {
        return shardWorld.getWorld();
    }

    public com.sk89q.worldedit.world.World getShardWEWorld() {
        return shardWorld;
    }

    public void setRespawnProfile(PlayerRespawnProfile_1_7_10 profile) {
        Validate.notNull(profile, "The profile cannot be null.");
        playerState.put(profile.getOwner(), profile);
    }

    public PlayerRespawnProfile_1_7_10 getRespawnProfile(UUID owner) {
        return playerState.get(owner);
    }

    public PlayerRespawnProfile_1_7_10 remRespawnProfile(UUID owner) {
        return playerState.remove(owner);
    }

    public boolean isUnallocatedInstanceArea(Location location) {
        if (location.getWorld().equals(shardWorld.getWorld())) {
            for (ProtectedRegion region : WG.getRegionManager(location.getWorld()).getApplicableRegions(location)) {
                if (manager.isActiveRegion(region.getId())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (!admin.isAdmin(player, AdminState.ADMIN) && isUnallocatedInstanceArea(event.getTo())) {
            event.setCancelled(true);
            leaveInstance(player);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!admin.isAdmin(player, AdminState.ADMIN) && isUnallocatedInstanceArea(event.getBlock().getLocation())) {
            event.setCancelled(true);
            leaveInstance(player);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!admin.isAdmin(player, AdminState.ADMIN) && isUnallocatedInstanceArea(event.getBlock().getLocation())) {
            event.setCancelled(true);
            leaveInstance(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        World world = getShardWorld();
        if (!event.getPlayer().getWorld().equals(world)) return;
        Player player = event.getPlayer();
        if (WG.getRegionManager(world).getApplicableRegions(player.getLocation()).size() > 0) {
            leaveInstance(player);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        World world = getShardWorld();
        if (!event.getEntity().getWorld().equals(world)) return;

        // Externally set
        if (playerState.containsKey(player.getUniqueId())) return;

        PlayerInstanceDeathEvent deathEvent = new PlayerInstanceDeathEvent(
                player,
                new PlayerRespawnProfile_1_7_10(
                        player,
                        event.getDroppedExp(),
                        KeepAction.KEEP,
                        KeepAction.KEEP,
                        KeepAction.KEEP,
                        KeepAction.KEEP
                )
        );

        callEvent(deathEvent);
        event.getDrops().clear();

        PlayerRespawnProfile_1_7_10 profile = deathEvent.getProfile();
        switch (profile.getArmorAction()) {
            case DROP:
                Collections.addAll(event.getDrops(), profile.getArmorContents());
                break;
        }

        switch (profile.getInvAction()) {
            case DROP:
                Collections.addAll(event.getDrops(), profile.getInventoryContents());
                break;
        }

        event.setDroppedExp((int) deathEvent.getProfile().getDroppedExp());

        playerState.put(player.getUniqueId(), deathEvent.getProfile());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // Restore their inventory if they have one stored
        PlayerRespawnProfile_1_7_10 identity = playerState.get(player.getUniqueId());
        if (identity != null) {
            try {
                ProfileUtil.restore(player, identity);

                Location respawn = event.getRespawnLocation();
                World shardWorld = getShardWorld();
                if (respawn.getWorld().equals(shardWorld)) {
                    if (respawn.distanceSquared(shardWorld.getSpawnLocation()) < 12) {
                        event.setRespawnLocation(getPrimusSpawn());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                playerState.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInstanceActivate(PartyActivateEvent event) {
        if (event.hasInstance()) {
            ShardInstance<?> inst = event.getInstance();
            for (Player player : event.getPlayers()) {
                // Make sure player's can't teleport other players via "/call" & "/bring"
                TeleportSession tpSession = sessions.getSession(TeleportSession.class, player);
                if (tpSession.totalBringRequest() > 0) {
                    tpSession.clearBringable();
                    ChatUtil.sendWarning(player, "All request to teleport to you have been terminated.");
                }
                sessions.getSession(ShardSession.class, player).setLastInstance(inst);
            }
        }
    }

    private File getWorkingDir() {
        return new File(inst().getDataFolder().getPath() + "/shards/");
    }

    public synchronized void writeData() {
        File playerStateFile = new File(getWorkingDir(), "/respawns.dat");
        if (playerStateFile.exists()) {
            Object playerStateFileO = IOUtil.readBinaryFile(playerStateFile);
            if (playerState.equals(playerStateFileO)) {
                return;
            }
        }
        IOUtil.toBinaryFile(getWorkingDir(), "respawns", playerState);
    }

    public synchronized void reloadData() {
        File playerStateFile = new File(getWorkingDir(), "/respawns.dat");
        if (playerStateFile.exists()) {
            Object playerStateFileO = IOUtil.readBinaryFile(playerStateFile);
            if (playerStateFileO instanceof HashMap) {
                //noinspection unchecked
                playerState = (HashMap<UUID, PlayerRespawnProfile_1_7_10>) playerStateFileO;
                logger().info("Loaded: " + playerState.size() + " shard respawn records.");
            } else {
                logger().warning("Invalid respawn record file encountered: " + playerStateFile.getName() + "!");
                logger().warning("Attempting to use backup file...");
                playerStateFile = new File(getWorkingDir().getPath() + "/old-" + playerStateFile.getName());
                if (playerStateFile.exists()) {
                    playerStateFileO = IOUtil.readBinaryFile(playerStateFile);
                    if (playerStateFileO instanceof HashMap) {
                        //noinspection unchecked
                        playerState = (HashMap<UUID, PlayerRespawnProfile_1_7_10>) playerStateFileO;
                        logger().info("Backup file loaded successfully!");
                        logger().info("Loaded: " + playerState.size() + " shard respawn records.");
                    } else {
                        logger().warning("Backup file failed to load!");
                    }
                }
            }
        }
    }

    public Location getPrimusSpawn() {
        return Bukkit.getWorlds().get(0).getSpawnLocation();
    }

    public boolean leaveInstance(Player player) {
        PlayerRespawnProfile_1_7_10 profile = playerState.remove(player.getUniqueId());
        if (profile != null) {
            ProfileUtil.restore(player, profile);
        }
        return player.teleport(getPrimusSpawn());
    }

    public class Commands {

        @Command(aliases = {"leave"},
                usage = "", desc = "Leave an instance",
                flags = "", min = 0, max = 0)
        public void leaveCmd(CommandContext args, CommandSender sender) throws CommandException {
            Player player = PlayerUtil.checkPlayer(sender);
            if (!player.getWorld().equals(getShardWorld())) {
                throw new CommandException("You must be in an instance to use this command.");
            }
            if (leaveInstance(player)) {
                ChatUtil.send(player, "You've left the instance.");
            }
        }

        @Command(aliases = {"rejoin"},
                usage = "", desc = "Rejoin an instance",
                flags = "", min = 0, max = 0)
        public void rejoinCmd(CommandContext args, CommandSender sender) throws CommandException {
            Player player = PlayerUtil.checkPlayer(sender);
            ShardInstance<?> inst = sessions.getSession(ShardSession.class, player).getLastInstance();
            if (inst == null || !inst.isActive()) {
                throw new CommandException("You do not have a previous instance available.");
            }
            if (!inst.getMaster().getType().allowsRejoin()) {
                throw new CommandException("That instance does not permit rejoin, please make a new one.");
            }

            inst.teleportTo(WE.wrapPlayer(player));
        }

        private WorldEditPlugin getWE() {
            return (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        }

        @Command(aliases = {"/relpos"},
                usage = "", desc = "Get your relative position to the minimum point of your selection",
                flags = "", min = 0, max = 0)
        public void relposCmd(CommandContext args, CommandSender sender) throws CommandException {
            Player player = PlayerUtil.checkPlayer(sender);
            Selection selection = getWE().getSelection(player);
            if (selection == null) {
                throw new CommandException("You must first make a selection!");
            }
            Location offset = player.getLocation().subtract(selection.getMinimumPoint());
            ChatUtil.send(sender, "X: " + offset.getBlockX() + ", Y: " + offset.getBlockY() + ", Z: " + offset.getBlockZ());
        }
    }
}

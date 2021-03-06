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
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.admin.AdminState;
import com.skelril.aurora.economic.ImpersonalComponent;
import com.skelril.aurora.events.PlayerAdminModeChangeEvent;
import com.skelril.aurora.events.PrayerApplicationEvent;
import com.skelril.aurora.events.apocalypse.ApocalypseLocalSpawnEvent;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.modifier.ModifierType;
import com.skelril.aurora.util.*;
import com.skelril.aurora.util.item.ItemUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Lever;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.skelril.aurora.modifier.ModifierComponent.getModifierManager;

public class GoldRush extends AbstractRegionedArena implements MonitoredArena, Listener {

    private final CommandBook inst = CommandBook.inst();
    private final Logger log = inst.getLogger();
    private final Server server = CommandBook.server();

    private Economy economy;
    private AdminComponent adminComponent;
    private ImpersonalComponent impersonalComponent;

    private ProtectedRegion lobby;

    private ProtectedRegion roomOne, roomTwo, roomThree;

    private ProtectedRegion doorOne, doorTwo;

    // Block - Should be flipped
    private ConcurrentHashMap<Location, Boolean> leverBlocks = new ConcurrentHashMap<>();
    private List<Location> floodBlocks = new ArrayList<>();
    private List<Location> chestBlocks = new ArrayList<>();

    // Block - Is unlocked
    private List<Location> locks = new ArrayList<>();
    private Location rewardChest;

    // Session
    private long startTime = System.currentTimeMillis();
    private int lootSplit = 0;
    private int floodBlockType = BlockID.WATER;
    private List<String> players = new ArrayList<>();
    private boolean leversTriggered = false;

    public GoldRush(World world, ProtectedRegion[] regions,
                    AdminComponent adminComponent, ImpersonalComponent impersonalComponent) {

        super(world, regions[0]);

        this.lobby = regions[1];

        this.roomOne = regions[2];
        this.roomTwo = regions[3];
        this.roomThree = regions[4];

        this.doorOne = regions[5];
        this.doorTwo = regions[6];

        this.adminComponent = adminComponent;
        this.impersonalComponent = impersonalComponent;

        findChestAndKeys();         // Setup room one
        findLeversAndFloodBlocks(); // Setup room two
        findRewardChest();          // Setup room three

        setupEconomy();

        //noinspection AccessStaticViaInstance
        inst.registerEvents(this);
    }

    @Override
    public void forceRestoreBlocks() {

        resetChestAndKeys();
        drainAll();
        resetFloodType();
        resetLevers();
        setDoor(doorOne, BlockID.IRON_BLOCK);
        setDoor(doorTwo, BlockID.IRON_BLOCK);
    }

    private void clearFloor() {

        getWorld().getEntitiesByClass(Item.class).stream().filter(i -> i.isValid()
                && getRegion().contains(BukkitUtil.toVector(i.getLocation()))).forEach(Item::remove);
    }

    private void findRewardChest() {

        com.sk89q.worldedit.Vector min = roomThree.getMinimumPoint();
        com.sk89q.worldedit.Vector max = roomThree.getMaximumPoint();

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

    private void findChestAndKeys() {

        com.sk89q.worldedit.Vector min = roomOne.getMinimumPoint();
        com.sk89q.worldedit.Vector max = roomOne.getMaximumPoint();

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
                        ((Chest) block).getInventory().clear();
                        block.update(true);
                        chestBlocks.add(block.getLocation());
                    } else if (block.getTypeId() == BlockID.WALL_SIGN) {
                        ((Sign) block).setLine(2, "- Locked -");
                        ((Sign) block).setLine(3, "Unlocked");
                        block.update(true);
                        locks.add(block.getLocation());
                    }
                }
            }
        }
    }

    private static final ItemStack goldBar = new ItemStack(ItemID.GOLD_BAR);
    private static final ItemStack[] keys = new ItemStack[]{
            new ItemStack(BlockID.CLOTH, 1, (short) 11),
            new ItemStack(BlockID.CLOTH, 1, (short) 14)
    };

    private void populateChest() {

        Chest chestState;

        for (Location chest : chestBlocks) {

            if (!chest.getChunk().isLoaded()) chest.getChunk().load();
            if (chest.getBlock().getTypeId() != BlockID.CHEST) continue;

            chestState = (Chest) chest.getBlock().getState();
            Inventory inventory = chestState.getBlockInventory();
            for (int i = ChanceUtil.getRandom(9 * 3); i > 0; --i) {

                ItemStack targetStack = goldBar.clone();
                targetStack.setAmount(ChanceUtil.getRandom(3));

                if (ChanceUtil.getChance(300)) {
                    inventory.addItem(CustomItemCenter.build(CustomItems.PIXIE_DUST, ChanceUtil.getRandom(12)));
                }
                if (ChanceUtil.getChance(1000)) {
                    targetStack = CustomItemCenter.build(CustomItems.PHANTOM_GOLD, ChanceUtil.getRandom(6));
                }
                if (ChanceUtil.getChance(10000)) {
                    inventory.addItem(CustomItemCenter.build(CustomItems.PHANTOM_HYMN));
                }

                inventory.setItem(ChanceUtil.getRandom(inventory.getSize()) - 1, targetStack);
            }
            chestState.update(true);
        }

        for (int i = 0; i < 2; i++) {
            Block block = CollectionUtil.getElement(chestBlocks).getBlock();
            if (!block.getChunk().isLoaded()) block.getChunk().load();
            Chest chest = (Chest) block.getState();
            chest.getInventory().setItem(ChanceUtil.getRandom(chest.getBlockInventory().getSize() - 1), keys[i]);
            chest.update(true);
        }
    }

    public boolean checkKeys() {

        for (Location lock : locks) {

            if (!lock.getBlock().getChunk().isLoaded()) return false;
            Sign aSign = (Sign) lock.getBlock().getState();
            if (aSign.getLine(2).startsWith("-")) return false;
        }
        return true;
    }

    private void resetChestAndKeys() {

        Chest chestState;
        for (Location chest : chestBlocks) {

            if (!chest.getChunk().isLoaded()) chest.getChunk().load();
            if (chest.getBlock().getTypeId() != BlockID.CHEST) continue;

            chestState = (Chest) chest.getBlock().getState();
            chestState.getBlockInventory().clear();
            chestState.update(true);
        }

        Sign signState;
        for (Location lock : locks) {

            if (!lock.getChunk().isLoaded()) lock.getChunk().load();
            signState = (Sign) lock.getBlock().getState();
            signState.setLine(2, "- Locked -");
            signState.setLine(3, "Unlocked");
            signState.update(true);
        }
    }

    private void findLeversAndFloodBlocks() {

        com.sk89q.worldedit.Vector min = roomTwo.getMinimumPoint();
        com.sk89q.worldedit.Vector max = roomTwo.getMaximumPoint();

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
                    if (block.getTypeId() == BlockID.LEVER) {
                        Lever lever = (Lever) block.getData();
                        lever.setPowered(false);
                        block.setData(lever);
                        block.update(true);
                        leverBlocks.put(block.getLocation(), !ChanceUtil.getChance(3));
                        for (int i = y; i < maxY; i++) {
                            block = getWorld().getBlockAt(x, i, z).getState();
                            if (block.getTypeId() == BlockID.AIR) {
                                floodBlocks.add(block.getLocation());
                                break;
                            }
                        }
                        break; // One lever a column only
                    }
                }
            }
        }
    }

    private boolean checkLevers() {

        for (Map.Entry<Location, Boolean> lever : leverBlocks.entrySet()) {

            if (!lever.getKey().getBlock().getChunk().isLoaded()) return false;
            Lever aLever = (Lever) lever.getKey().getBlock().getState().getData();
            if (aLever.isPowered() != lever.getValue()) return false;
        }
        return true;
    }

    private void resetLevers() {

        BlockState state;
        for (Location entry : leverBlocks.keySet()) {

            if (!entry.getBlock().getChunk().isLoaded()) entry.getBlock().getChunk().load();
            state = entry.getBlock().getState();
            Lever lever = (Lever) state.getData();
            lever.setPowered(false);
            state.setData(lever);
            state.update(true);
            leverBlocks.put(entry, !ChanceUtil.getChance(3));
        }
    }

    private long lastSwitch = System.currentTimeMillis();

    private void randomizeLevers() {

        BlockState state;
        Location mutable;
        if (System.currentTimeMillis() - lastSwitch >= TimeUnit.SECONDS.toMillis(14)) {
            for (Location entry : leverBlocks.keySet()) {

                if (!entry.getBlock().getChunk().isLoaded()) entry.getBlock().getChunk().load();
                state = entry.getBlock().getState();
                Lever lever = (Lever) state.getData();
                lever.setPowered(false);
                state.setData(lever);
                state.update(true);
                leverBlocks.put(entry, !ChanceUtil.getChance(3));
            }
            lastSwitch = System.currentTimeMillis();
            randomizeLevers();
        } else if (System.currentTimeMillis() - lastSwitch == 0) {
            for (Map.Entry<Location, Boolean> entry : leverBlocks.entrySet()) {

                mutable = entry.getKey().clone();
                mutable.add(0, -1, 0);

                if (!mutable.getBlock().getChunk().isLoaded()) mutable.getChunk().load();
                state = mutable.getBlock().getState();
                state.setTypeId(BlockID.REDSTONE_LAMP_OFF);
                state.update(true);
            }
            server.getScheduler().runTaskLater(inst, () -> {

                BlockState aState;
                Location aMutable;
                for (Map.Entry<Location, Boolean> entry : leverBlocks.entrySet()) {
                    aMutable = entry.getKey().clone();
                    aMutable.add(0, -1, 0);

                    if (!aMutable.getBlock().getChunk().isLoaded()) aMutable.getChunk().load();
                    aState = aMutable.getBlock().getState();
                    if (entry.getValue()) aState.setTypeId(BlockID.REDSTONE_LAMP_ON);
                    else aState.setTypeId(BlockID.REDSTONE_LAMP_OFF);
                    aState.update(true);
                }
                server.getScheduler().runTaskLater(inst, this::randomizeLevers, 15);
            }, 15);
        } else {
            for (Location entry : leverBlocks.keySet()) {

                mutable = entry.clone();
                mutable.add(0, -1, 0);

                if (!mutable.getChunk().isLoaded()) mutable.getChunk().load();
                state = mutable.getBlock().getState();
                state.setTypeId(BlockID.REDSTONE_LAMP_OFF);
                state.update(true);
            }
        }
    }

    private void checkFloodType() {
        for (Player player : getContained(Player.class)) {
            if (!players.contains(player.getName())) continue;
            if (ItemUtil.findItemOfName(player.getInventory().getContents(), CustomItems.PHANTOM_HYMN.toString())) {
                drainAll(); // Force away all water
                floodBlockType = BlockID.LAVA;
                break;
            }
        }
    }

    private void resetFloodType() {
        floodBlockType = BlockID.WATER;
    }

    private long lastFlood = System.currentTimeMillis();

    private void flood() {

        final int playerMod = Math.max(1, players.size() / 2);
        if (System.currentTimeMillis() - startTime >= TimeUnit.SECONDS.toMillis((3 * 60) / playerMod)) {

            for (Location floodBlock : floodBlocks) {
                floodBlock.getBlock().setTypeId(floodBlockType);
            }

            if (System.currentTimeMillis() - lastFlood >= TimeUnit.SECONDS.toMillis(30 / Math.max(1, playerMod))) {
                com.sk89q.worldedit.Vector min = roomTwo.getMinimumPoint();
                com.sk89q.worldedit.Vector max = roomTwo.getMaximumPoint();

                int minX = min.getBlockX();
                int minZ = min.getBlockZ();
                int minY = min.getBlockY();
                int maxX = max.getBlockX();
                int maxZ = max.getBlockZ();
                int maxY = max.getBlockY();

                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        for (int y = minY; y <= maxY; y++) {
                            Block block = getWorld().getBlockAt(x, y, z);
                            if (!block.getChunk().isLoaded()) block.getChunk().load();
                            if (block.getTypeId() == BlockID.AIR) {
                                block.setTypeIdAndData(floodBlockType, (byte) 0, false);
                                break;
                            }
                        }
                    }
                }
                lastFlood = System.currentTimeMillis();
            }
        }
    }

    private void setDoor(ProtectedRegion door, int typeId) {

        com.sk89q.worldedit.Vector min = door.getMinimumPoint();
        com.sk89q.worldedit.Vector max = door.getMaximumPoint();

        int minX = min.getBlockX();
        int minZ = min.getBlockZ();
        int minY = min.getBlockY();
        int maxX = max.getBlockX();
        int maxZ = max.getBlockZ();
        int maxY = max.getBlockY();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    Block block = getWorld().getBlockAt(x, y, z);
                    if (!block.getChunk().isLoaded()) block.getChunk().load();
                    block.setTypeId(typeId);
                }
            }
        }
    }

    private void drainAll() {

        com.sk89q.worldedit.Vector min = roomTwo.getMinimumPoint();
        com.sk89q.worldedit.Vector max = roomTwo.getMaximumPoint();

        int minX = min.getBlockX();
        int minZ = min.getBlockZ();
        int minY = min.getBlockY();
        int maxX = max.getBlockX();
        int maxZ = max.getBlockZ();
        int maxY = max.getBlockY();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = maxY; y >= minY; --y) {
                    Block block = getWorld().getBlockAt(x, y, z);
                    if (EnvironmentUtil.isLiquid(block.getTypeId())) {
                        block.setTypeId(BlockID.AIR);
                    }
                }
            }
        }
    }

    private void killAll() {

        getContained(Player.class).stream()
                .filter(player -> !lobby.contains(BukkitUtil.toVector(player.getLocation())))
                .forEach(player -> player.setHealth(0));
    }

    public void start() {

        startTime = System.currentTimeMillis(); // Reset start clock
        leversTriggered = false;                // Reset lever check
        forceRestoreBlocks();                   // Reset blocks
        clearFloor();                           // Clean floor
        populateChest();                        // Add content

    }

    public int moveLobby() {

        long timeSpent = System.currentTimeMillis() - startTime;
        if (timeSpent > TimeUnit.MINUTES.toMillis(7)) killAll();
        if (!players.isEmpty()) return (int) ((TimeUnit.MINUTES.toMillis(7) - timeSpent) / 1000);
        for (Player aPlayer : server.getOnlinePlayers()) {

            if (LocationUtil.isInRegion(getWorld(), lobby, aPlayer)) {

                // Teleport
                Location location;
                do {
                    location = BukkitUtil.toLocation(getWorld(),
                            LocationUtil.pickLocation(roomOne.getMinimumPoint(), roomOne.getMaximumPoint()));
                    location.setY(roomOne.getMinimumPoint().getBlockY() + 1);
                } while (location.getBlock().getTypeId() != BlockID.AIR);
                aPlayer.teleport(location, PlayerTeleportEvent.TeleportCause.UNKNOWN);

                // Reset vitals
                aPlayer.setHealth(20);
                aPlayer.setFoodLevel(20);
                aPlayer.setSaturation(5F);
                aPlayer.setExhaustion(0F);

                // Add
                players.add(aPlayer.getName());

                // Partner talk
                ChatUtil.sendWarning(aPlayer, "[Partner] I've disabled the security systems for now.");
                ChatUtil.sendWarning(aPlayer, "[Partner] For your sake kid I hope you can move quickly.");
            }
        }
        if (!players.isEmpty()) {
            lootSplit = 0;
            for (String player : players) {
                lootSplit += Math.max(ChanceUtil.getRangedRandom(11.52, 34.56), economy.getBalance(player) * .0015);
            }
            lootSplit /= players.size();
            if (ChanceUtil.getChance(35)) lootSplit *= 10;
            if (ChanceUtil.getChance(15)) lootSplit *= 2;
            if (getModifierManager().isActive(ModifierType.QUAD_GOLD_RUSH)) lootSplit *= 4;
            start(); // Start if someone was teleported
        }
        return 0;
    }

    @Override
    public void run() {

        if (!LocationUtil.containsPlayer(getWorld(), getRegion())) return;
        equalize();
        if (checkKeys()) {
            if (LocationUtil.containsPlayer(getWorld(), roomOne)) {
                setDoor(doorOne, BlockID.AIR);
            } else {
                setDoor(doorOne, BlockID.IRON_BLOCK);
                if (checkLevers() || leversTriggered) {
                    drainAll();
                    setDoor(doorTwo, BlockID.AIR);
                } else {
                    randomizeLevers();
                    checkFloodType();
                    flood();
                }
            }
        }
    }

    @Override
    public void disable() {

        forceRestoreBlocks();
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

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {

        if (players.contains(event.getPlayer().getName()) || (contains(event.getTo()) && !(contains(event.getFrom())))) {

            if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.UNKNOWN)) return;

            event.setCancelled(true);
            ChatUtil.sendWarning(event.getPlayer(), "You cannot teleport to that location.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (event.isFlying() && players.contains(player.getName())) {
            event.setCancelled(true);
            ChatUtil.send(player, "You cannot fly here!");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAdminModeChange(PlayerAdminModeChangeEvent event) {

        if (!event.getNewAdminState().equals(AdminState.MEMBER) && players.contains(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLocalSpawn(ApocalypseLocalSpawnEvent event) {

        if (players.contains(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrayerApplication(PrayerApplicationEvent event) {

        if (event.getCause().getEffect().getType().isHoly() && players.contains(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        if (players.contains(event.getPlayer().getName())) {
            ChatUtil.send(event.getPlayer(), ChatColor.DARK_RED, "Are you sure that's a good idea right now?");
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (players.contains(event.getPlayer().getName())) {
            ChatUtil.send(event.getPlayer(), ChatColor.DARK_RED, "Are you sure that's a good idea right now?");
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        BlockState state = event.getClickedBlock().getLocation().getBlock().getState();
        if (state.getTypeId() == BlockID.STONE_BUTTON && lobby.contains(BukkitUtil.toVector(state.getBlock()))) {
            int waitingTime = moveLobby();
            if (waitingTime != 0) {
                ChatUtil.sendWarning(event.getPlayer(), "There is already a robbery in progress.");
                ChatUtil.sendWarning(event.getPlayer(), "The current robbery will end within: "
                        + waitingTime + " seconds.");
            }
        } else if (state.getTypeId() == BlockID.WALL_SIGN && locks.contains(state.getLocation())) {
            Sign sign = (Sign) state;
            if (sign.getLine(1).toLowerCase().contains("blue")) {
                if (event.getPlayer().getInventory().containsAtLeast(keys[0], 1)) {
                    event.getPlayer().getInventory().removeItem(keys[0]);
                    ((Sign) state).setLine(2, "Locked");
                    ((Sign) state).setLine(3, "- Unlocked -");
                    sign.update(true);

                    //noinspection deprecation
                    event.getPlayer().updateInventory();
                }
            } else if (sign.getLine(1).toLowerCase().contains("red")) {
                if (event.getPlayer().getInventory().containsAtLeast(keys[1], 1)) {
                    event.getPlayer().getInventory().removeItem(keys[1]);
                    ((Sign) state).setLine(2, "Locked");
                    ((Sign) state).setLine(3, "- Unlocked -");
                    sign.update(true);

                    //noinspection deprecation
                    event.getPlayer().updateInventory();
                }
            }
        } else if (state.getTypeId() == BlockID.WALL_SIGN) {
            Sign sign = (Sign) state;
            if (sign.getLine(1).equals("Play Gold Rush")) {

                String playerName = event.getPlayer().getName();

                if (ItemUtil.countFilledSlots(event.getPlayer()) > 0) {
                    ChatUtil.sendError(event.getPlayer(), "[Partner] Don't bring anything with ya kid, it'll weigh you down.");
                    return;
                } else if (!economy.has(playerName, 100)) {
                    ChatUtil.sendError(event.getPlayer(), "[Partner] Kid you don't have enough cash, your balance will never pay bail.");
                    return;
                }

                Location location;
                do {
                    location = BukkitUtil.toLocation(getWorld(),
                            LocationUtil.pickLocation(lobby.getMinimumPoint(), lobby.getMaximumPoint()));
                    location.setY(lobby.getMinimumPoint().getBlockY() + 1);
                } while (location.getBlock().getTypeId() != BlockID.AIR);
                event.getPlayer().teleport(location);
                ChatUtil.send(event.getPlayer(), "[Partner] Ey there kid, just press that button over there to start.");
            }
        } else if (state.getTypeId() == BlockID.LEVER && leverBlocks.containsKey(state.getLocation())) {
            server.getScheduler().runTaskLater(inst, () -> {
                if (checkLevers()) leversTriggered = true;
            }, 1);
        } else if (state.getTypeId() == BlockID.CHEST && contains(state.getLocation())) {
            if (!players.contains(event.getPlayer().getName())) {
                event.setUseInteractedBlock(Event.Result.DENY);
                ChatUtil.sendWarning(event.getPlayer(), "[Partner] Thought you'd scam me huh kid?");
                ChatUtil.sendWarning(event.getPlayer(), "[Partner] Well I'll teach you kid!");
                ChatUtil.sendWarning(event.getPlayer(), "The alarm goes off.");
                event.getPlayer().setHealth(0);
                return;
            }
            if (rewardChest.equals(state.getLocation())) {
                players.remove(event.getPlayer().getName());
                event.setUseInteractedBlock(Event.Result.DENY);
                event.getPlayer().teleport(LocationUtil.grandBank(getWorld()));

                ChatUtil.send(event.getPlayer(), "You have successfully robbed the bank!\n");
                ChatUtil.send(event.getPlayer(), "[Partner] I've put your split of the money in your account.");
                ChatUtil.send(event.getPlayer(), "[Partner] Don't question my logic...\n");
                ChatUtil.send(event.getPlayer(), "You obtain: "
                        + ChatUtil.makeCountString(ChatColor.YELLOW, economy.format(lootSplit), " as your split."));

                economy.depositPlayer(event.getPlayer().getName(), lootSplit);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        final Player player = event.getPlayer();
        if (contains(player) || players.contains(player.getName())) {
            server.getScheduler().runTaskLater(inst, () -> {
                ChatUtil.sendWarning(player, "[Partner] Thought you'd scam me huh kid?");
                ChatUtil.sendWarning(player, "[Partner] Well I'll teach you kid!");
                ChatUtil.sendWarning(player, "The alarm goes off.");
                player.setHealth(0);
            }, 1);
        }
    }

    @EventHandler
    public void onSignPlace(SignChangeEvent event) {

        if (event.getLine(1).equals("Play Gold Rush")) impersonalComponent.check(event.getBlock(), true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        String playerName = event.getEntity().getName();
        if (contains(event.getEntity()) && players.contains(playerName)) {
            double amt = Math.min(economy.getBalance(playerName), Math.max(100, economy.getBalance(playerName) * .005));
            economy.withdrawPlayer(playerName, amt);
            ChatUtil.sendWarning(event.getEntity(), "You are forced to pay a fine of: " + economy.format(amt) + '.');
            players.remove(playerName);
            String deathMessage;
            switch (ChanceUtil.getRandom(6)) {
                case 1:
                    deathMessage = " needs to find a new profession";
                    break;
                case 2:
                    deathMessage = " is now at the mercy of Hallow";
                    break;
                case 3:
                    deathMessage = " is now folding and hanging... though mostly hanging...";
                    break;
                case 4:
                    if (event.getDeathMessage().contains("drown")) {
                        deathMessage = " discovered H2O is not always good for ones health";
                        break;
                    }
                case 5:
                    if (event.getDeathMessage().contains("starved")) {
                        deathMessage = " should take note of the need to bring food with them";
                        break;
                    }
                default:
                    deathMessage = " was killed by police while attempting to rob a bank";
                    break;
            }
            event.setDeathMessage(playerName + deathMessage);
        }
    }

    private boolean setupEconomy() {

        RegisteredServiceProvider<Economy> economyProvider = server.getServicesManager().getRegistration(net.milkbowl
                .vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
}

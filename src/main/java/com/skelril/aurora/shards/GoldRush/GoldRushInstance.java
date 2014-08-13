/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards.GoldRush;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.aurora.economic.store.AdminStoreComponent;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.modifiers.ModifierType;
import com.skelril.aurora.shards.BukkitShardInstance;
import com.skelril.aurora.util.*;
import com.skelril.aurora.util.item.ItemUtil;
import com.skelril.aurora.util.player.PlayerRespawnProfile_1_7_10;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Lever;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.skelril.aurora.modifiers.ModifierComponent.getModifierCenter;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

public class GoldRushInstance extends BukkitShardInstance<GoldRushShard> implements Runnable {

    // Constants
    private static final double FEE_MINIMUM = 100;
    private static final double FEE_MULTIPLIER = .005;


    private CuboidRegion roomOne, roomTwo;

    private CuboidRegion doorOne, doorTwo;

    // Block - Should be flipped
    private ConcurrentHashMap<Location, Boolean> leverBlocks = new ConcurrentHashMap<>();
    private List<Location> floodBlocks = new ArrayList<>();
    private List<Location> chestBlocks = new ArrayList<>();

    // Block - Is unlocked
    private List<Location> locks = new ArrayList<>();
    private Location rewardChest;

    // Session
    private long startTime = -1;
    private Map<UUID, Double> origonalCharge = new HashMap<>();
    private double lootSplit = 0;
    private int playerMod = 0;
    private int floodBlockType = BlockID.WATER;
    private boolean keysTriggered = false;
    private boolean checkingKeys = true;
    private boolean leversTriggered = false;
    private boolean checkingLevers = true;
    private long lastLeverSwitch = System.currentTimeMillis();
    private long lastFlood = System.currentTimeMillis();

    public GoldRushInstance(GoldRushShard shard, World world, ProtectedRegion region) {
        super(shard, world, region);
        setup();
    }

    private void setup() {
        com.sk89q.worldedit.Vector offset = getRegion().getMinimumPoint();

        rewardChest = new Location(getBukkitWorld(), offset.getX() + 15, offset.getY() + 2, offset.getZ() + 6);

        roomOne = new CuboidRegion(offset.add(1, 1, 36), offset.add(29, 7, 74));
        roomTwo = new CuboidRegion(offset.add(11, 1, 17), offset.add(19, 7, 35));

        doorOne = new CuboidRegion(offset.add(14, 1, 36), offset.add(16, 3, 36));
        doorTwo = new CuboidRegion(offset.add(14, 1, 16), offset.add(16, 3, 16));

        findChestAndKeys();         // Setup room one
        findLeversAndFloodBlocks(); // Setup room two
    }

    @Override
    public void prepare() {
        resetChestAndKeys();
        resetLevers();
        resetFloodType();
        drainAll();
        setDoor(doorOne, BlockID.IRON_BLOCK);
        setDoor(doorTwo, BlockID.IRON_BLOCK);
    }

    public void start() {
        startTime = System.currentTimeMillis(); // Reset start clock
        populateChest();                        // Add content
    }

    public boolean isLocked() {
        return startTime != -1;
    }

    public boolean isComplete() {
        return checkKeys() && checkLevers();
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
                    block = getBukkitWorld().getBlockAt(x, y, z).getState();
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

    private static final ItemStack[] keys = new ItemStack[]{
            new ItemStack(BlockID.CLOTH, 1, (short) 11),
            new ItemStack(BlockID.CLOTH, 1, (short) 14)
    };

    public ItemStack getBlueKey() {
        return keys[0].clone();
    }

    public ItemStack getRedKey() {
        return keys[1].clone();
    }

    private void populateChest() {
        for (Location chest : chestBlocks) {
            if (chest.getBlock().getTypeId() != BlockID.CHEST) continue;

            Chest chestState = (Chest) chest.getBlock().getState();
            Inventory inventory = chestState.getBlockInventory();

            int iterationTimes = ChanceUtil.getRandom(27);
            for (int i = iterationTimes; i > 0; --i) {
                ItemStack targetStack;
                if (ChanceUtil.getChance(1000)) {
                    targetStack = CustomItemCenter.build(CustomItems.PHANTOM_GOLD, ChanceUtil.getRandom(6));
                } else {
                    targetStack = new ItemStack(ItemID.GOLD_BAR, ChanceUtil.getRandom(3));
                }
                inventory.setItem(ChanceUtil.getRandom(inventory.getSize()) - 1, targetStack);
            }

            if (ChanceUtil.getChance(300 / iterationTimes)) {
                inventory.addItem(CustomItemCenter.build(CustomItems.PIXIE_DUST, ChanceUtil.getRandom(12)));
            }
            if (ChanceUtil.getChance(10000 / iterationTimes)) {
                inventory.addItem(CustomItemCenter.build(CustomItems.PHANTOM_HYMN));
            }

            chestState.update(true);
        }

        for (int i = 0; i < 2; i++) {
            Block block = CollectionUtil.getElement(chestBlocks).getBlock();
            Chest chest = (Chest) block.getState();
            chest.getInventory().setItem(ChanceUtil.getRandom(chest.getBlockInventory().getSize() - 1), keys[i].clone());
            chest.update(true);
        }
    }

    private void resetChestAndKeys() {
        Chest chestState;
        for (Location chest : chestBlocks) {
            if (chest.getBlock().getTypeId() != BlockID.CHEST) continue;
            chestState = (Chest) chest.getBlock().getState();
            chestState.getBlockInventory().clear();
            chestState.update(true);
        }

        Sign signState;
        for (Location lock : locks) {
            signState = (Sign) lock.getBlock().getState();
            signState.setLine(2, "- Locked -");
            signState.setLine(3, "Unlocked");
            signState.update(true);
        }

        keysTriggered = false;
        checkingKeys = true;
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
                    block = getBukkitWorld().getBlockAt(x, y, z).getState();
                    if (!block.getChunk().isLoaded()) block.getChunk().load();
                    if (block.getTypeId() == BlockID.LEVER) {
                        Lever lever = (Lever) block.getData();
                        lever.setPowered(false);
                        block.setData(lever);
                        block.update(true);
                        leverBlocks.put(block.getLocation(), !ChanceUtil.getChance(3));
                        for (int i = y; i < maxY; i++) {
                            block = getBukkitWorld().getBlockAt(x, i, z).getState();
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

    public boolean checkLevers() {
        if (!checkingLevers) {
            return leversTriggered;
        }

        for (Map.Entry<Location, Boolean> lever : leverBlocks.entrySet()) {
            Lever aLever = (Lever) lever.getKey().getBlock().getState().getData();
            if (aLever.isPowered() != lever.getValue()) return false;
        }
        return true;
    }

    public void unlockLevers() {
        drainAll();
        setDoor(doorTwo, BlockID.AIR);
        leversTriggered = true;
        checkingLevers = false;
    }

    private void resetLevers() {
        leversTriggered = false;
        checkingLevers = true;
        BlockState state;
        for (Location entry : leverBlocks.keySet()) {
            state = entry.getBlock().getState();
            Lever lever = (Lever) state.getData();
            lever.setPowered(false);
            state.setData(lever);
            state.update(true);
            leverBlocks.put(entry, !ChanceUtil.getChance(3));
        }
    }

    public Location getRewardChestLoc() {
        return rewardChest;
    }

    @Override
    public void expirePlayers() {
        if (isComplete()) {
            getContained(Player.class).stream().forEach(this::payPlayer);
        } else {
            getContained(Player.class).stream().forEach(p -> p.setHealth(0));
        }
    }

    @Override
    public void teleportTo(com.sk89q.worldedit.entity.Player... players) {
        for (com.sk89q.worldedit.entity.Player player : players) {
            if (player instanceof BukkitPlayer) {
                Player aPlayer = ((BukkitPlayer) player).getPlayer();

                double balance = getMaster().getEcon().getBalance(aPlayer);
                double fee = Math.min(balance, Math.max(FEE_MINIMUM, balance * FEE_MULTIPLIER));
                if (fee > balance) {
                    ChatUtil.sendError(aPlayer, "You don't have enough money to join this instance.");
                    continue;
                } else {
                    ChatUtil.sendWarning(aPlayer, "[Partner] Ey kid, I'm going to take some cash from ya.");
                    ChatUtil.sendNotice(aPlayer, "[Partner] Ya know, just in case you get caught or somethin...");
                    ChatUtil.sendNotice(aPlayer, "[Partner] Thief's honor, I swear!");
                }

                getMaster().getEcon().withdrawPlayer(aPlayer, fee);
                origonalCharge.put(player.getUniqueId(), fee);

                // Teleport
                Location location;
                do {
                    do {
                        location = BukkitUtil.toLocation(
                                getBukkitWorld(),
                                LocationUtil.pickLocation(
                                        roomOne.getMinimumPoint(),
                                        roomOne.getMaximumPoint()
                                )
                        );
                        location.setY(roomOne.getMinimumPoint().getBlockY());
                    } while (location.getBlock().getTypeId() != BlockID.AIR);
                    aPlayer.teleport(location);
                } while (!contains(aPlayer));

                // Reset vitals
                aPlayer.setHealth(aPlayer.getMaxHealth());
                aPlayer.setFoodLevel(20);
                aPlayer.setSaturation(20F);
                aPlayer.setExhaustion(0F);

                getMaster().getManager().setRespawnProfile(
                        new PlayerRespawnProfile_1_7_10(
                            aPlayer,
                            0,
                            KeepAction.KEEP,
                            KeepAction.KEEP,
                            KeepAction.KEEP,
                            KeepAction.KEEP
                        )
                );

                aPlayer.getInventory().clear();

                // Partner talk
                server().getScheduler().runTaskLater(inst(), () -> {
                    ChatUtil.sendNotice(aPlayer, "[Partner] I've disabled the security systems for now.");
                    server().getScheduler().runTaskLater(inst(), () -> {
                        ChatUtil.sendWarning(aPlayer, "[Partner] For your sake kid I hope you can move quickly.");
                    }, 20);
                }, 20);
            }
        }
        Collection<Player> cPlayers = getContained(Player.class);
        Iterator<Player> it = cPlayers.iterator();
        while (it.hasNext()) {
            Player next = it.next();
            Double origCharge = origonalCharge.get(next.getUniqueId());
            if (origCharge == null) {
                getMaster().getManager().leaveInstance(next);
                it.remove();
                continue;
            }
            lootSplit += Math.max(ChanceUtil.getRangedRandom(11.52, 34.56), origCharge * .3);
        }
        lootSplit /= cPlayers.size();
        playerMod = Math.max(1, cPlayers.size() / 2);
        if (ChanceUtil.getChance(35)) lootSplit *= 10;
        if (ChanceUtil.getChance(15)) lootSplit *= 2;
        if (getModifierCenter().isActive(ModifierType.QUAD_GOLD_RUSH)) lootSplit *= 4;
        start();
    }

    @Override
    public void run() {
        if (!isLocked()) return; // If it's not locked things haven't been started yet
        if (System.currentTimeMillis() - startTime > TimeUnit.MINUTES.toMillis(7) || isEmpty()) {
            expire();
            return;
        }
        equalize();
        if (checkKeys()) {
            unlockKeys();
            if (LocationUtil.containsPlayer(getBukkitWorld(), roomOne)) {
                setDoor(doorOne, BlockID.AIR);
            } else {
                setDoor(doorOne, BlockID.IRON_BLOCK);
                if (checkLevers()) {
                    unlockLevers();
                } else {
                    randomizeLevers();
                    checkFloodType();
                    flood();
                }
            }
        }
    }

    public void equalize() {
        getContained(org.bukkit.entity.Player.class).forEach(getMaster().getAdmin()::standardizePlayer);
    }

    public void refundPlayer(Player player) {
        Double fee = origonalCharge.get(player.getUniqueId());
        // They didn't pay, CHEATER!!!
        if (fee == null) return;
        getMaster().getEcon().depositPlayer(player, fee);
        getMaster().getManager().leaveInstance(player);
        ChatUtil.sendNotice(player, "[Partner] These @$#&!@# restarts... Here, have your bail money...");
    }

    public boolean payPlayer(Player player) {
        Double fee = origonalCharge.get(player.getUniqueId());
        // They didn't pay, CHEATER!!!
        if (fee == null) return false;

        Economy econ = getMaster().getEcon();

        ItemStack[] itemStacks = player.getInventory().getContents();
        double goldValue = 0;
        double itemValue = 0;
        for (ItemStack is : itemStacks) {
            if (is == null) continue;
            // Static values
            switch (is.getTypeId()) {
                case ItemID.GOLD_NUGGET:
                    ++goldValue;
                case ItemID.GOLD_BAR:
                    if (ItemUtil.isItem(is, CustomItems.PHANTOM_GOLD)) {
                        goldValue += 100;
                    } else {
                        goldValue += 9;
                    }
                    continue;
                case BlockID.GOLD_BLOCK:
                    goldValue += 81;
                    continue;
                default:
                    double mkVal = AdminStoreComponent.priceCheck(is, true);
                    if (mkVal > 0) {
                        itemValue += mkVal;
                    }
            }
        }

        ChatUtil.sendNotice(player, "You obtain: ");
        ChatUtil.sendNotice(player, " - Bail: " + ChatUtil.makeCountString(ChatColor.YELLOW, econ.format(fee), "."));
        ChatUtil.sendNotice(player, " - Split: " + ChatUtil.makeCountString(ChatColor.YELLOW, econ.format(lootSplit), "."));
        if (goldValue > 0) {
            ChatUtil.sendNotice(player, " - Gold: " + ChatUtil.makeCountString(ChatColor.YELLOW, econ.format(lootSplit), "."));
        }
        if (itemValue > 0) {
            ChatUtil.sendNotice(player, " - Items: " + ChatUtil.makeCountString(ChatColor.YELLOW, econ.format(lootSplit), "."));
        }

        getMaster().getEcon().depositPlayer(player, fee + lootSplit + goldValue + itemValue);
        getMaster().getManager().leaveInstance(player);
        return true;
    }

    public List<Location> getLockLocations() {
        return locks;
    }

    public boolean checkKeys() {
        if (!checkingKeys) {
            return keysTriggered;
        }

        for (Location lock : locks) {
            Sign aSign = (Sign) lock.getBlock().getState();
            if (aSign.getLine(2).startsWith("-")) return false;
        }
        return true;
    }

    public void unlockKeys() {
        keysTriggered = true;
        checkingKeys = false;
    }

    private void setDoor(CuboidRegion door, int typeId) {

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
                    Block block = getBukkitWorld().getBlockAt(x, y, z);
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
                    Block block = getBukkitWorld().getBlockAt(x, y, z);
                    if (EnvironmentUtil.isLiquid(block.getTypeId())) {
                        block.setTypeId(BlockID.AIR);
                    }
                }
            }
        }
    }

    private void randomizeLevers() {

        BlockState state;
        Location mutable;
        if (System.currentTimeMillis() - lastLeverSwitch >= TimeUnit.SECONDS.toMillis(14)) {
            for (Location entry : leverBlocks.keySet()) {
                state = entry.getBlock().getState();
                Lever lever = (Lever) state.getData();
                lever.setPowered(false);
                state.setData(lever);
                state.update(true);
                leverBlocks.put(entry, !ChanceUtil.getChance(3));
            }
            lastLeverSwitch = System.currentTimeMillis();
            randomizeLevers();
        } else if (System.currentTimeMillis() - lastLeverSwitch == 0) {
            for (Map.Entry<Location, Boolean> entry : leverBlocks.entrySet()) {
                mutable = entry.getKey().clone();
                mutable.add(0, -1, 0);

                state = mutable.getBlock().getState();
                state.setTypeId(BlockID.REDSTONE_LAMP_OFF);
                state.update(true);
            }
            server().getScheduler().runTaskLater(inst(), () -> {
                BlockState aState;
                Location aMutable;
                for (Map.Entry<Location, Boolean> entry : leverBlocks.entrySet()) {
                    aMutable = entry.getKey().clone();
                    aMutable.add(0, -1, 0);

                    aState = aMutable.getBlock().getState();
                    if (entry.getValue()) aState.setTypeId(BlockID.REDSTONE_LAMP_ON);
                    else aState.setTypeId(BlockID.REDSTONE_LAMP_OFF);
                    aState.update(true);
                }
                server().getScheduler().runTaskLater(inst(), this::randomizeLevers, 15);
            }, 15);
        } else {
            for (Location entry : leverBlocks.keySet()) {
                mutable = entry.clone();
                mutable.add(0, -1, 0);

                state = mutable.getBlock().getState();
                state.setTypeId(BlockID.REDSTONE_LAMP_OFF);
                state.update(true);
            }
        }
    }

    private void checkFloodType() {
        for (org.bukkit.entity.Player player : getContained(org.bukkit.entity.Player.class)) {
            if (ItemUtil.findItemOfName(player.getInventory().getContents(), CustomItems.PHANTOM_HYMN.toString())) {
                drainAll(); // Force away all water
                floodBlockType = BlockID.LAVA;
                break;
            }
        }
    }

    private void flood() {
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
                            Block block = getBukkitWorld().getBlockAt(x, y, z);
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

    private void resetFloodType() {
        floodBlockType = BlockID.WATER;
    }
}

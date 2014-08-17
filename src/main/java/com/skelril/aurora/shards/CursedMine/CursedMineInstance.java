/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards.CursedMine;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.exceptions.UnsupportedPrayerException;
import com.skelril.aurora.prayer.PrayerComponent;
import com.skelril.aurora.prayer.PrayerFX.InventoryFX;
import com.skelril.aurora.prayer.PrayerType;
import com.skelril.aurora.shards.BukkitShardInstance;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.LocationUtil;
import com.skelril.aurora.util.item.ItemUtil;
import com.skelril.aurora.util.restoration.BlockRecord;
import com.skelril.aurora.util.restoration.PlayerMappedBlockRecordIndex;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

public class CursedMineInstance extends BukkitShardInstance<CursedMineShard> implements Runnable {

    private static final int[] items = new int[]{
            BlockID.IRON_BLOCK, BlockID.IRON_ORE, ItemID.IRON_BAR,
            BlockID.GOLD_BLOCK, BlockID.GOLD_ORE, ItemID.GOLD_BAR, ItemID.GOLD_NUGGET,
            BlockID.REDSTONE_ORE, BlockID.GLOWING_REDSTONE_ORE, ItemID.REDSTONE_DUST,
            BlockID.LAPIS_LAZULI_BLOCK, BlockID.LAPIS_LAZULI_ORE, ItemID.INK_SACK,
            BlockID.DIAMOND_BLOCK, BlockID.DIAMOND_ORE, ItemID.DIAMOND,
            BlockID.EMERALD_BLOCK, BlockID.EMERALD_ORE, ItemID.EMERALD
    };

    private CuboidRegion floodGate;

    private Location entryPoint;

    private final long lastActivationTime = 18000;
    private long lastActivation = 0;
    private PlayerMappedBlockRecordIndex recordSystem = new PlayerMappedBlockRecordIndex();

    private int idleTicks = 0;
    private int ticks = 0;

    public CursedMineInstance(CursedMineShard shard, World world, ProtectedRegion region) {
        super(shard, world, region);
        remove();
        setUp();
    }

    private void setUp() {
        com.sk89q.worldedit.Vector offset = getRegion().getMinimumPoint();

        entryPoint = new Location(getBukkitWorld(), offset.getX() + 71.5, offset.getY() + 59, offset.getZ() + 86.5);

        floodGate = new CuboidRegion(offset.add(66, 40, 131), offset.add(78, 40, 139));
    }

    public void revertAll() {
        recordSystem.revertAll();
    }

    public void randomRestore() {
        recordSystem.revertByTime(ChanceUtil.getRangedRandom(9000, 60000));
    }

    public void revertPlayer(Player player) {
        recordSystem.revertByPlayer(player.getName());
    }

    public void recordBlockBreak(Player player, BlockRecord record) {
        recordSystem.addItem(player.getName(), record);
    }

    public boolean hasRecordForPlayer(Player player) {
        return recordSystem.hasRecordForPlayer(player.getName());
    }

    public void activatePumps() {
        long temp = lastActivation;
        lastActivation = System.currentTimeMillis();
        if (System.currentTimeMillis() - temp <= lastActivationTime * 5) {
            lastActivation -= lastActivationTime * .4;
        }
    }
    
    @Override
    public void cleanUp() {
        revertAll();
        super.cleanUp();
    }
    
    @Override
    public void run() {
        ++ticks;
        ++idleTicks;
        if (!isEmpty()) {
            idleTicks = 0;
            equalize();
            changeWater();
        } else if (idleTicks > 60 * 5) {
            expire();
            return;
        }

        if (ticks % 4 == 0) {
            drain();
            sweepFloor();
            randomRestore();
        }
    }

    @Override
    public void teleportTo(com.sk89q.worldedit.entity.Player... players) {
        for (com.sk89q.worldedit.entity.Player player : players) {
            if (player instanceof BukkitPlayer) {
                Player bPlayer = ((BukkitPlayer) player).getPlayer();
                bPlayer.teleport(entryPoint);
            }
        }
    }
    
    public void equalize() {
        getContained(Player.class).stream().forEach(getMaster().getAdmin()::deadmin);
    }

    public void eatFood(Player player) {
        if (player.getSaturation() - 1 >= 0) {
            player.setSaturation(player.getSaturation() - 1);
        } else if (player.getFoodLevel() - 1 >= 0) {
            player.setFoodLevel(player.getFoodLevel() - 1);
        } else if (player.getHealth() - 1 >= 0) {
            player.setHealth(player.getHealth() - 1);
        }
    }

    public void poison(Player player, int duration) {
        if (ChanceUtil.getChance(player.getLocation().getBlockY() / 2)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * duration, 2));
            ChatUtil.sendWarning(player, "The ore releases a toxic gas poisoning you!");
        }
    }

    public void ghost(final Player player, int blockID) {
        try {
            AdminComponent adminComponent = getMaster().getAdmin();
            PrayerComponent prayerComponent = getMaster().getPrayer();
            if (ChanceUtil.getChance(player.getLocation().getBlockY())) {
                if (ChanceUtil.getChance(2)) {
                    switch (ChanceUtil.getRandom(6)) {
                        case 1:
                            ChatUtil.sendNotice(player, "Caspher the friendly ghost drops some bread.");
                            player.getWorld().dropItemNaturally(player.getLocation(),
                                    new ItemStack(ItemID.BREAD, ChanceUtil.getRandom(16)));
                            break;
                        case 2:
                            ChatUtil.sendNotice(player, "COOKIE gives you a cookie.");
                            player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(ItemID.COOKIE));
                            break;
                        case 3:
                            ChatUtil.sendNotice(player, "Caspher the friendly ghost appears.");
                            for (int i = 0; i < 8; i++) {
                                player.getWorld().dropItemNaturally(player.getLocation(),
                                        new ItemStack(ItemID.IRON_BAR, ChanceUtil.getRandom(64)));
                                player.getWorld().dropItemNaturally(player.getLocation(),
                                        new ItemStack(ItemID.GOLD_BAR, ChanceUtil.getRandom(64)));
                                player.getWorld().dropItemNaturally(player.getLocation(),
                                        new ItemStack(ItemID.DIAMOND, ChanceUtil.getRandom(64)));
                            }
                            break;
                        case 4:
                            ChatUtil.sendNotice(player, "John gives you a new jacket.");
                            player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(ItemID.LEATHER_CHEST));
                            break;
                        case 5:
                            ChatUtil.sendNotice(player, "Tim teleports items to you.");
                            getContained(Item.class).forEach(i -> i.teleport(player));

                            // Add in some extra drops just in case the loot wasn't very juicy
                            player.getWorld().dropItem(player.getLocation(), new ItemStack(ItemID.IRON_BAR, ChanceUtil.getRandom(64)));
                            player.getWorld().dropItem(player.getLocation(), new ItemStack(ItemID.GOLD_BAR, ChanceUtil.getRandom(64)));
                            player.getWorld().dropItem(player.getLocation(), new ItemStack(ItemID.DIAMOND, ChanceUtil.getRandom(64)));
                            break;
                        case 6:
                            ChatUtil.sendNotice(player, "Dan gives you a sparkling touch.");

                            int id;
                            switch (ChanceUtil.getRandom(3)) {
                                case 1:
                                    id = ItemID.IRON_BAR;
                                    break;
                                case 2:
                                    id = ItemID.GOLD_BAR;
                                    break;
                                case 3:
                                    id = ItemID.DIAMOND;
                                    break;
                                default:
                                    id = ItemID.REDSTONE_DUST;
                                    break;
                            }

                            prayerComponent.influencePlayer(player,
                                    PrayerComponent.constructPrayer(player, new InventoryFX(id, 64), 5000));
                            break;
                        default:
                            break;
                    }
                } else {

                    if (ItemUtil.hasAncientArmor(player) && ChanceUtil.getChance(2)) {
                        ChatUtil.sendNotice(player, ChatColor.AQUA, "Your armor blocks an incoming ghost attack.");
                        return;
                    }

                    adminComponent.depowerPlayer(player);

                    switch (ChanceUtil.getRandom(11)) {
                        case 1:
                            if (ChanceUtil.getChance(4)) {
                                if (blockID == BlockID.DIAMOND_ORE) {
                                    getMaster().getHitList().addPlayer(player, this);
                                    ChatUtil.sendWarning(player, "You ignite fumes in the air!");
                                    EditSession ess = WorldEdit.getInstance()
                                            .getEditSessionFactory()
                                            .getEditSession(new BukkitWorld(player.getWorld()), -1);
                                    try {
                                        ess.fillXZ(BukkitUtil.toVector(player.getLocation()), new BaseBlock(BlockID.FIRE), 20, 20, true);
                                    } catch (MaxChangedBlocksException ignored) {

                                    }
                                    for (int i = ChanceUtil.getRandom(24) + 20; i > 0; --i) {
                                        final boolean untele = i == 11;
                                        server().getScheduler().runTaskLater(inst(), () -> {
                                            if (untele) {
                                                recordSystem.revertByPlayer(player.getName());
                                                getMaster().getHitList().remPlayer(player);
                                            }

                                            if (!contains(player)) return;

                                            Location l = LocationUtil.findRandomLoc(player.getLocation().getBlock(), 3, true, false);
                                            l.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), 3F, true, false);
                                        }, 12 * i);
                                    }
                                } else {
                                    getMaster().getHitList().addPlayer(player, this);
                                    player.chat("Who's a good ghost?!?!");
                                    server().getScheduler().runTaskLater(inst(), () -> {
                                        player.chat("Don't hurt me!!!");
                                        server().getScheduler().runTaskLater(inst(), () -> {
                                            player.chat("Nooooooooooo!!!");
                                            try {
                                                prayerComponent.influencePlayer(player, PrayerComponent.constructPrayer(player,
                                                        PrayerType.CANNON, TimeUnit.MINUTES.toMillis(2)));
                                            } catch (UnsupportedPrayerException ex) {
                                                ex.printStackTrace();
                                            }
                                        }, 20);
                                    }, 20);
                                }
                                break;
                            }
                        case 2:
                            ChatUtil.sendWarning(player, "Dave attaches to your soul...");
                            for (int i = 20; i > 0; --i) {
                                server().getScheduler().runTaskLater(inst(), () -> {

                                    if (!contains(player)) return;

                                    player.setHealth(ChanceUtil.getRandom(ChanceUtil.getRandom(player.getMaxHealth())) - 1);
                                }, 12 * i);
                            }
                            break;
                        case 3:
                            ChatUtil.sendWarning(player, "George plays with fire, sadly too close to you.");
                            prayerComponent.influencePlayer(player, PrayerComponent.constructPrayer(player,
                                    PrayerType.FIRE, TimeUnit.SECONDS.toMillis(45)));
                            break;
                        case 4:
                            ChatUtil.sendWarning(player, "Simon says pick up sticks.");
                            for (int i = 0; i < player.getInventory().getContents().length * 1.5; i++) {
                                player.getWorld().dropItem(player.getLocation(), new ItemStack(ItemID.STICK, 64));
                            }
                            break;
                        case 5:
                            ChatUtil.sendWarning(player, "Ben dumps out your backpack.");
                            prayerComponent.influencePlayer(player, PrayerComponent.constructPrayer(player,
                                    PrayerType.BUTTERFINGERS, TimeUnit.SECONDS.toMillis(10)));
                            break;
                        case 6:
                            ChatUtil.sendWarning(player, "Merlin attacks with a mighty rage!");
                            prayerComponent.influencePlayer(player, PrayerComponent.constructPrayer(player,
                                    PrayerType.MERLIN, TimeUnit.SECONDS.toMillis(20)));
                            break;
                        case 7:
                            ChatUtil.sendWarning(player, "Dave tells everyone that your mining!");
                            Bukkit.broadcastMessage(ChatColor.GOLD + "The player: "
                                    + player.getDisplayName() + " is mining in the cursed mine!!!");
                            break;
                        case 8:
                            ChatUtil.sendWarning(player, "Dave likes your food.");
                            getMaster().getHitList().addPlayer(player, this);
                            prayerComponent.influencePlayer(player, PrayerComponent.constructPrayer(player,
                                    PrayerType.STARVATION, TimeUnit.MINUTES.toMillis(15)));
                            break;
                        case 9:
                            ChatUtil.sendWarning(player, "Hallow declares war on YOU!");
                            for (int i = 0; i < ChanceUtil.getRangedRandom(10, 30); i++) {
                                Blaze blaze = getBukkitWorld().spawn(player.getLocation(), Blaze.class);
                                blaze.setTarget(player);
                                blaze.setRemoveWhenFarAway(true);
                            }
                            break;
                        case 10:
                            ChatUtil.sendWarning(player, "A legion of hell hounds appears!");
                            for (int i = 0; i < ChanceUtil.getRangedRandom(10, 30); i++) {
                                Wolf wolf = getBukkitWorld().spawn(player.getLocation(), Wolf.class);
                                wolf.setTarget(player);
                                wolf.setRemoveWhenFarAway(true);
                            }
                            break;
                        case 11:
                            if (blockID == BlockID.EMERALD_ORE) {
                                ChatUtil.sendNotice(player, "Dave got a chemistry set!");
                                getMaster().getHitList().addPlayer(player, this);
                                prayerComponent.influencePlayer(player, PrayerComponent.constructPrayer(player,
                                        PrayerType.DEADLYPOTION, TimeUnit.MINUTES.toMillis(30)));
                            } else {
                                ChatUtil.sendWarning(player, "Dave says hi, that's not good.");
                                getMaster().getHitList().addPlayer(player, this);
                                prayerComponent.influencePlayer(player, PrayerComponent.constructPrayer(player,
                                        PrayerType.SLAP, TimeUnit.MINUTES.toMillis(30)));
                                prayerComponent.influencePlayer(player, PrayerComponent.constructPrayer(player,
                                        PrayerType.BUTTERFINGERS, TimeUnit.MINUTES.toMillis(30)));
                                prayerComponent.influencePlayer(player, PrayerComponent.constructPrayer(player,
                                        PrayerType.FIRE, TimeUnit.MINUTES.toMillis(30)));
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (UnsupportedPrayerException ex) {
            ex.printStackTrace();
        }
    }

    private static Set<Integer> replaceableTypes = new HashSet<>();

    static {
        replaceableTypes.add(BlockID.WATER);
        replaceableTypes.add(BlockID.STATIONARY_WATER);
        replaceableTypes.add(BlockID.WOOD);
        replaceableTypes.add(BlockID.AIR);
    }

    private void changeWater() {

        int id = 0;
        if (lastActivation == 0 || System.currentTimeMillis() - lastActivation >= lastActivationTime) {
            id = BlockID.WOOD;
        }
        com.sk89q.worldedit.Vector min = floodGate.getMinimumPoint();
        com.sk89q.worldedit.Vector max = floodGate.getMaximumPoint();

        int minX = min.getBlockX();
        int minZ = min.getBlockZ();
        int blockY = min.getBlockY();
        int maxX = max.getBlockX();
        int maxZ = max.getBlockZ();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                Block block = getBukkitWorld().getBlockAt(x, blockY, z);

                if (!block.getChunk().isLoaded()) continue;

                if (replaceableTypes.contains(block.getTypeId())) {
                    block.setTypeId(id);
                }
            }
        }
    }

    private boolean checkInventory(Player player) {
        if (!inst().hasPermission(player, "aurora.prayer.intervention")) return false;
        for (int aItem : items) {
            if (player.getInventory().containsAtLeast(new ItemStack(aItem), 1)) return true;
        }
        return false;
    }

    public void drain() {

        Location modifiable = new Location(getBukkitWorld(), 0, 0, 0);
        Location previousLoc;

        for (Entity e : getContained(InventoryHolder.class)) {
            Inventory eInventory = ((InventoryHolder) e).getInventory();

            if (e instanceof Player) {

                if (getMaster().getAdmin().isAdmin((Player) e)) continue;

                modifiable = e.getLocation(modifiable);

                // Emerald
                long diff = System.currentTimeMillis() - lastActivation;
                if (modifiable.getY() < 30 && (lastActivation == 0 || diff <= lastActivationTime * .35 || diff >= lastActivationTime * 5)) {
                    for (int i = 0; i < ChanceUtil.getRangedRandom(2, 5); i++) {

                        previousLoc = modifiable.clone();
                        modifiable = LocationUtil.findRandomLoc(previousLoc, 5, true, false);

                        if (modifiable.getBlock().getTypeId() != BlockID.AIR) {
                            modifiable = previousLoc;
                        }

                        getBukkitWorld().spawn(modifiable, Blaze.class);
                    }
                }
            }

            for (int i = 0; i < (ItemUtil.countFilledSlots(eInventory.getContents()) / 2) - 2 || i < 1; i++) {

                if (e instanceof Player) {
                    if (ChanceUtil.getChance(15) && checkInventory((Player) e)) {
                        ChatUtil.sendNotice((Player) e, "Divine intervention protects some of your items.");
                        continue;
                    }
                }

                // Iron
                ItemUtil.removeItemOfType((InventoryHolder) e, BlockID.IRON_BLOCK, ChanceUtil.getRandom(2), true);
                ItemUtil.removeItemOfType((InventoryHolder) e, BlockID.IRON_ORE, ChanceUtil.getRandom(4), true);
                ItemUtil.removeItemOfType((InventoryHolder) e, ItemID.IRON_BAR, ChanceUtil.getRandom(8), true);

                // Gold
                ItemUtil.removeItemOfType((InventoryHolder) e, BlockID.GOLD_BLOCK, ChanceUtil.getRandom(2), true);
                ItemUtil.removeItemOfType((InventoryHolder) e, BlockID.GOLD_ORE, ChanceUtil.getRandom(4), true);
                ItemUtil.removeItemOfType((InventoryHolder) e, ItemID.GOLD_BAR, ChanceUtil.getRandom(10), true);
                ItemUtil.removeItemOfType((InventoryHolder) e, ItemID.GOLD_NUGGET, ChanceUtil.getRandom(80), true);

                // Redstone
                ItemUtil.removeItemOfType((InventoryHolder) e, BlockID.REDSTONE_ORE, ChanceUtil.getRandom(2), true);
                ItemUtil.removeItemOfType((InventoryHolder) e, BlockID.GLOWING_REDSTONE_ORE, ChanceUtil.getRandom(2), true);
                ItemUtil.removeItemOfType((InventoryHolder) e, ItemID.REDSTONE_DUST, ChanceUtil.getRandom(34), true);

                // Lap
                ItemUtil.removeItemOfType((InventoryHolder) e, BlockID.LAPIS_LAZULI_BLOCK, ChanceUtil.getRandom(2), true);
                ItemUtil.removeItemOfType((InventoryHolder) e, BlockID.LAPIS_LAZULI_ORE, ChanceUtil.getRandom(4), true);
                eInventory.removeItem(new ItemStack(ItemID.INK_SACK, ChanceUtil.getRandom(34), (short) 4));

                // Diamond
                ItemUtil.removeItemOfType((InventoryHolder) e, BlockID.DIAMOND_BLOCK, ChanceUtil.getRandom(2), true);
                ItemUtil.removeItemOfType((InventoryHolder) e, BlockID.DIAMOND_ORE, ChanceUtil.getRandom(4), true);
                ItemUtil.removeItemOfType((InventoryHolder) e, ItemID.DIAMOND, ChanceUtil.getRandom(16), true);
            }
        }
    }

    public void sweepFloor() {

        for (Item item : getContained(Item.class)) {

            if (!contains(item)) continue;

            int id = item.getItemStack().getTypeId();
            for (int aItem : items) {
                if (aItem == id) {
                    double newAmt = item.getItemStack().getAmount() * .8;
                    if (newAmt < 1) {
                        item.remove();
                    } else {
                        item.getItemStack().setAmount((int) newAmt);
                    }
                    break;
                }
            }
        }
    }
}

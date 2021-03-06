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

package com.skelril.aurora.city.engine.area.areas.GraveYard;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.city.engine.area.AreaComponent;
import com.skelril.aurora.exceptions.UnknownPluginException;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.util.*;
import com.skelril.aurora.util.database.IOUtil;
import com.skelril.aurora.util.item.ItemUtil;
import com.skelril.aurora.util.player.PlayerState;
import com.skelril.aurora.util.restoration.BaseBlockRecordIndex;
import com.skelril.aurora.util.restoration.BlockRecord;
import com.zachsthings.libcomponents.InjectComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Lever;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

//@ComponentInformation(friendlyName = "Grave Yard", desc = "The home of the undead")
//@Depend(components = {AdminComponent.class}, plugins = {"WorldGuard"})
public class GraveYardArea extends AreaComponent<GraveYardConfig> {

    @InjectComponent
    protected AdminComponent admin;

    // Other
    protected Economy economy;

    // Temple regions
    protected ProtectedRegion temple, pressurePlateLockArea, creepers, rewards;

    // Block information
    protected static Set<BaseBlock> breakable = new HashSet<>();

    static {
        breakable.add(new BaseBlock(BlockID.LONG_GRASS, -1));
        breakable.add(new BaseBlock(BlockID.RED_FLOWER, -1));
        breakable.add(new BaseBlock(BlockID.YELLOW_FLOWER, -1));
        breakable.add(new BaseBlock(BlockID.DIRT, -1));
        breakable.add(new BaseBlock(BlockID.TORCH, -1));
        breakable.add(new BaseBlock(BlockID.STONE_BRICK, 2));
        breakable.add(new BaseBlock(BlockID.WEB, -1));
        breakable.add(new BaseBlock(BlockID.LEAVES, -1));
    }

    protected static Set<BaseBlock> autoBreakable = new HashSet<>();

    static {
        autoBreakable.add(new BaseBlock(BlockID.STEP, 5));
        autoBreakable.add(new BaseBlock(BlockID.STEP, 13));
        autoBreakable.add(new BaseBlock(BlockID.WOODEN_STEP, 8));
        autoBreakable.add(new BaseBlock(BlockID.STONE_BRICK, 2));
    }

    // Next Phantom Hymn Reset
    protected long nextTStorm;

    // Head Stones
    protected List<Location> headStones = new ArrayList<>();

    // Reward Chest
    protected List<Location> rewardChest = new ArrayList<>();

    // Pressure Plate Lock
    // Use a boolean to store the check value instead of checking for every step
    protected boolean isPressurePlateLocked = true;
    protected ConcurrentHashMap<Location, Boolean> pressurePlateLocks = new ConcurrentHashMap<>();

    // Block Restoration
    protected BaseBlockRecordIndex generalIndex = new BaseBlockRecordIndex();
    // Respawn Inventory Map
    protected HashMap<String, PlayerState> playerState = new HashMap<>();

    @Override
    public void setUp() {
        try {
            WorldGuardPlugin WG = APIUtil.getWorldGuard();
            world = server.getWorlds().get(0);

            RegionManager manager = WG.getRegionManager(world);
            String base = "carpe-diem-district-grave-yard";
            region = manager.getRegion(base);
            temple = manager.getRegion(base + "-temple");
            pressurePlateLockArea = manager.getRegion(base + "-temple-puzzle-one");
            creepers = manager.getRegion(base + "-creepers");
            rewards = manager.getRegion(base + "-temple-rewards");

            tick = 4 * 20;
            listener = new GraveYardListener(this);
            config = new GraveYardConfig();

            findHeadStones();
            findPressurePlateLockLevers();
            findRewardChest();

            reloadData();
            setupEconomy();

            spawnBlockBreakerTask();
        } catch (UnknownPluginException e) {
            log.info("WorldGuard could not be found!");
        }
    }

    @Override
    public void enable() {
        server.getScheduler().runTaskLater(inst, super::enable, 1);
    }

    @Override
    public void disable() {
        writeData(false);
    }

    @Override
    public void run() {
        restoreBlocks();
        if (isEmpty()) return;
        equalize();
        for (LivingEntity entity : getContained(LivingEntity.class)) {
            if (!entity.isValid()) continue;
            // Cave Spider killer
            if (entity instanceof CaveSpider && entity.getLocation().getBlock().getLightFromSky() >= 10) {
                for (int i = 0; i < 20; ++i) getWorld().playEffect(entity.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
                entity.remove();
                continue;
            }
            // People Code
            if (entity instanceof Player && isEvilMode(entity.getEyeLocation().getBlock())) {
                if (admin.isAdmin((Player) entity)) continue;
                fogPlayer((Player) entity);
                localSpawn((Player) entity);
            }
        }
    }

    private void spawnBlockBreakerTask() {
        server.getScheduler().runTaskTimer(inst, () -> {
            if (cachedEmpty()) return;
            for (LivingEntity e : getContained(LivingEntity.class)) {
                // Auto break stuff
                Location belowLoc = e.getLocation();
                if (!(e instanceof Player) || isInEvilRegion(belowLoc)) {
                    breakBlock(e, belowLoc.add(0, -1, 0));
                    breakBlock(e, belowLoc.add(0, -1, 0));
                    breakBlock(e, belowLoc.add(0, -1, 0));
                }
            }
        }, 0, 5);
    }

    public <T extends Entity> Collection<T> getTempleContained(Class<T> clazz) {
        return world.getEntitiesByClass(clazz).stream()
                .filter(entity -> entity.isValid() && isHostileTempleArea(entity.getLocation()))
                .collect(Collectors.toList());
    }

    public void equalize() {
        for (Player player : getContained(Player.class)) {
            try {
                admin.standardizePlayer(player);
            } catch (Exception e) {
                log.warning("The player: " + player.getName() + " may have an unfair advantage.");
            }
        }
    }

    protected boolean accept(BaseBlock baseBlock, Set<BaseBlock> baseBlocks) {
        for (BaseBlock aBaseBlock : baseBlocks) {
            if (baseBlock.equalsFuzzy(aBaseBlock)) return true;
        }
        return false;
    }

    private boolean isEvilMode(Block block) {
        // Weather/Day Check
        //noinspection SimplifiableIfStatement
        if (EnvironmentUtil.isNightTime(getWorld().getTime()) || getWorld().hasStorm()) return true;
        return isHostileTempleArea(block.getLocation()) || block.getLightLevel() == 0;
    }

    public boolean isHostileTempleArea(Location location) {
        return isInEvilRegion(location) && location.getY() < 93 && location.getBlock().getLightFromSky() < 4;
    }

    private boolean isInEvilRegion(Location location) {
        for (ProtectedRegion region : new ProtectedRegion[]{temple}) {
            if (LocationUtil.isInRegion(getWorld(), region, location)) return true;
        }
        return location.getY() < 69 && contains(location);
    }

    public void writeData(boolean doAsync) {
        Runnable run = () -> {
            generalFile:
            {
                File generalFile = new File(getWorkingDir().getPath() + "/general.dat");
                if (generalFile.exists()) {
                    Object generalFileO = IOUtil.readBinaryFile(generalFile);

                    if (generalIndex.equals(generalFileO)) {
                        break generalFile;
                    }
                }
                IOUtil.toBinaryFile(getWorkingDir(), "general", generalIndex);
            }
            respawnsFile:
            {
                File playerStateFile = new File(getWorkingDir().getPath() + "/respawns.dat");
                if (playerStateFile.exists()) {
                    Object playerStateFileO = IOUtil.readBinaryFile(playerStateFile);

                    if (playerState.equals(playerStateFileO)) {
                        break respawnsFile;
                    }
                }
                IOUtil.toBinaryFile(getWorkingDir(), "respawns", playerState);
            }
        };
        if (doAsync) {
            server.getScheduler().runTaskAsynchronously(inst, run);
        } else {
            run.run();
        }
    }

    public void reloadData() {
        File generalFile = new File(getWorkingDir().getPath() + "/general.dat");
        File playerStateFile = new File(getWorkingDir().getPath() + "/respawns.dat");
        if (generalFile.exists()) {
            Object generalFileO = IOUtil.readBinaryFile(generalFile);
            if (generalFileO instanceof BaseBlockRecordIndex) {
                generalIndex = (BaseBlockRecordIndex) generalFileO;
                log.info("Loaded: " + generalIndex.size() + " general records for the grave yard.");
            } else {
                log.warning("Invalid block record file encountered: " + generalFile.getName() + "!");
                log.warning("Attempting to use backup file...");
                generalFile = new File(getWorkingDir().getPath() + "/old-" + generalFile.getName());
                if (generalFile.exists()) {
                    generalFileO = IOUtil.readBinaryFile(generalFile);
                    if (generalFileO instanceof BaseBlockRecordIndex) {
                        generalIndex = (BaseBlockRecordIndex) generalFileO;
                        log.info("Backup file loaded successfully!");
                        log.info("Loaded: " + generalIndex.size() + " general records for the grave yard.");
                    } else {
                        log.warning("Backup file failed to load!");
                    }
                }
            }
        }
        if (playerStateFile.exists()) {
            Object playerStateFileO = IOUtil.readBinaryFile(playerStateFile);
            if (playerStateFileO instanceof HashMap) {
                //noinspection unchecked
                playerState = (HashMap<String, PlayerState>) playerStateFileO;
                log.info("Loaded: " + playerState.size() + " respawn records for the grave yard.");
            } else {
                log.warning("Invalid block record file encountered: " + playerStateFile.getName() + "!");
                log.warning("Attempting to use backup file...");
                playerStateFile = new File(getWorkingDir().getPath() + "/old-" + playerStateFile.getName());
                if (playerStateFile.exists()) {
                    playerStateFileO = IOUtil.readBinaryFile(playerStateFile);
                    if (playerStateFileO instanceof HashMap) {
                        //noinspection unchecked
                        playerState = (HashMap<String, PlayerState>) playerStateFileO;
                        log.info("Backup file loaded successfully!");
                        log.info("Loaded: " + playerState.size() + " respawn records for the grave yard.");
                    } else {
                        log.warning("Backup file failed to load!");
                    }
                }
            }
        }
    }

    public void restoreBlocks() {
        generalIndex.revertByTime(1000 * 27);
        writeData(true);
    }

    private void localSpawn(Player player) {
        localSpawn(player, false);
    }

    protected void localSpawn(Player player, boolean bypass) {
        if (!ChanceUtil.getChance(3) && !bypass) return;
        Block playerBlock = player.getLocation().getBlock();
        Location ls;
        if (LocationUtil.isInRegion(getWorld(), rewards, player)) {
            for (int i = 0; i < 3; ++i) {
                ls = LocationUtil.findRandomLoc(playerBlock, 8, true, false);
                if (!BlockType.isTranslucent(ls.getBlock().getTypeId())) {
                    ls = player.getLocation();
                }
                Zombie zombie = spawn(ls, Zombie.class, "Guardian Zombie");
                zombie.setCanPickupItems(false);
                EntityEquipment equipment = zombie.getEquipment();
                equipment.setItemInHand(new ItemStack(ItemID.DIAMOND_SWORD));
                equipment.setArmorContents(new ItemStack[]{
                        CustomItemCenter.build(CustomItems.ANCIENT_BOOTS),
                        CustomItemCenter.build(CustomItems.ANCIENT_LEGGINGS),
                        CustomItemCenter.build(CustomItems.ANCIENT_CHESTPLATE),
                        CustomItemCenter.build(CustomItems.ANCIENT_HELMET)
                });
                // Drop Chances
                equipment.setItemInHandDropChance(0);
                equipment.setHelmetDropChance(0);
                equipment.setChestplateDropChance(0);
                equipment.setLeggingsDropChance(0);
                equipment.setBootsDropChance(0);
            }
            return;
        }
        Block aBlock;
        for (int i = ChanceUtil.getRandom(16 - playerBlock.getLightLevel()); i > 0; --i) {
            ls = LocationUtil.findRandomLoc(playerBlock, 8, true, false);
            if (!BlockType.isTranslucent(ls.getBlock().getTypeId())) {
                ls = player.getLocation();
            }
            aBlock = ls.getBlock().getRelative(BlockFace.DOWN);
            // If the block is a half slab or it is wood, don't do this
            if (aBlock.getTypeId() != BlockID.STEP && aBlock.getTypeId() != BlockID.WOOD) {
                aBlock = aBlock.getRelative(BlockFace.DOWN, 2);
                if (BlockType.canPassThrough(aBlock.getTypeId())) {
                    ls.add(0, -3, 0);
                }
            }
            spawnAndArm(ls, Zombie.class, true);
        }
    }

    protected  <T extends LivingEntity> T spawnAndArm(Location location, Class<T> type, boolean allowItemPickup) {
        if (!location.getChunk().isLoaded()) return null;
        T e = spawn(location, type);
        if (e == null) return null;
        arm(e, allowItemPickup);
        return e;
    }

    private <T extends LivingEntity> T spawn(Location location, Class<T> type) {
        return spawn(location, type, "Grave Zombie");
    }

    private  <T extends LivingEntity> T spawn(Location location, Class<T> type, String name) {
        if (location == null) return null;
        T entity = location.getWorld().spawn(location, type);
        entity.setCustomName(name);
        entity.setCustomNameVisible(false);
        return entity;
    }

    private void arm(Entity e, boolean allowItemPickup) {
        if (!(e instanceof LivingEntity)) return;
        EntityEquipment equipment = ((LivingEntity) e).getEquipment();
        ((LivingEntity) e).setCanPickupItems(allowItemPickup);
        if (ChanceUtil.getChance(50)) {
            if (ChanceUtil.getChance(15)) {
                equipment.setArmorContents(ItemUtil.diamondArmor);
            } else {
                equipment.setArmorContents(ItemUtil.ironArmor);
            }
            if (ChanceUtil.getChance(4)) equipment.setHelmet(null);
            if (ChanceUtil.getChance(4)) equipment.setChestplate(null);
            if (ChanceUtil.getChance(4)) equipment.setLeggings(null);
            if (ChanceUtil.getChance(4)) equipment.setBoots(null);
        }
        if (ChanceUtil.getChance(50)) {
            ItemStack sword = new ItemStack(ItemID.IRON_SWORD);
            if (ChanceUtil.getChance(35)) sword = new ItemStack(ItemID.DIAMOND_SWORD);
            ItemMeta meta = sword.getItemMeta();
            if (ChanceUtil.getChance(2)) meta.addEnchant(Enchantment.DAMAGE_ALL, ChanceUtil.getRandom(5), false);
            if (ChanceUtil.getChance(2)) meta.addEnchant(Enchantment.DAMAGE_ARTHROPODS, ChanceUtil.getRandom(5), false);
            if (ChanceUtil.getChance(2)) meta.addEnchant(Enchantment.DAMAGE_UNDEAD, ChanceUtil.getRandom(5), false);
            if (ChanceUtil.getChance(2)) meta.addEnchant(Enchantment.FIRE_ASPECT, ChanceUtil.getRandom(2), false);
            if (ChanceUtil.getChance(2)) meta.addEnchant(Enchantment.KNOCKBACK, ChanceUtil.getRandom(2), false);
            if (ChanceUtil.getChance(2)) meta.addEnchant(Enchantment.LOOT_BONUS_MOBS, ChanceUtil.getRandom(3), false);
            sword.setItemMeta(meta);
            equipment.setItemInHand(sword);
        }
        if (allowItemPickup) {
            equipment.setItemInHandDropChance(equipment.getItemInHand() == null ? 1 : .35F);
            equipment.setHelmetDropChance(equipment.getHelmet() == null ? 1 : .35F);
            equipment.setChestplateDropChance(equipment.getChestplate() == null ? 1 : .35F);
            equipment.setLeggingsDropChance(equipment.getLeggings() == null ? 1 : .35F);
            equipment.setBootsDropChance(equipment.getBoots() == null ? 1 : .35F);
        } else {
            equipment.setItemInHandDropChance(.17F);
            equipment.setHelmetDropChance(.17F);
            equipment.setChestplateDropChance(.17F);
            equipment.setLeggingsDropChance(.17F);
            equipment.setBootsDropChance(.17F);
        }
    }

    public void makeGrave(String name, ItemStack[] itemStacks) {
        makeGraveRec(name, itemStacks, headStones.size());
    }

    private void makeGraveRec(String name, ItemStack[] itemStacks, int tries) {
        if (tries <= 0) {
            log.warning("Failed to make a grave for: " + name + "!");
            for (ItemStack stack : itemStacks) {
                getWorld().dropItem(getWorld().getSpawnLocation(), stack);
            }
            return;
        }
        tries--;
        Location headStone = CollectionUtil.getElement(headStones).clone();
        BlockState signState = headStone.getBlock().getState();
        if (signState instanceof Sign) {
            Calendar calendar = Calendar.getInstance();
            // Why the month is zero based I'll never know
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int year = calendar.get(Calendar.YEAR);
            ((Sign) signState).setLine(0, month + "/" + day + "/" + year);
            ((Sign) signState).setLine(1, "RIP");
            ((Sign) signState).setLine(2, name);
            if (itemStacks == null) {
                signState.update();
                return;
            }
            headStone.add(0, -2, 0);
            BlockState chestState = headStone.getBlock().getState();
            if (chestState instanceof Chest) {
                ((Chest) chestState).getInventory().clear();
                ((Chest) chestState).getInventory().addItem(itemStacks);
            } else {
                headStone.add(0, -1, 0);
                chestState = headStone.getBlock().getState();
                if (chestState instanceof Chest) {
                    ((Chest) chestState).getInventory().clear();
                    ((Chest) chestState).getInventory().addItem(itemStacks);
                } else {
                    org.bukkit.material.Sign sign =
                            new org.bukkit.material.Sign(BlockID.WALL_SIGN, signState.getRawData());
                    BlockFace attachedFace = sign.getAttachedFace();
                    headStone = headStone.getBlock().getRelative(attachedFace, 2).getLocation();
                    headStone.add(0, 2, 0);
                    chestState = headStone.getBlock().getState();
                    if (chestState instanceof Chest) {
                        ((Chest) chestState).getInventory().clear();
                        ((Chest) chestState).getInventory().addItem(itemStacks);
                    } else {
                        headStone.add(0, -1, 0);
                        chestState = headStone.getBlock().getState();
                        if (chestState instanceof Chest) {
                            ((Chest) chestState).getInventory().clear();
                            ((Chest) chestState).getInventory().addItem(itemStacks);
                        } else {
                            makeGraveRec(name, itemStacks, tries);
                            return;
                        }
                    }
                }
            }
            signState.update();
            log.info("Made a Grave for: " + name + " at: " + headStone.getBlockX() + ", " + headStone.getBlockY() + ", " + headStone.getBlockZ());
        } else {
            makeGraveRec(name, itemStacks, tries);
        }
    }

    private void findHeadStones() {
        headStones.clear();
        final List<Chunk> chunkList = new ArrayList<>();
        com.sk89q.worldedit.Vector min = getRegion().getMinimumPoint();
        com.sk89q.worldedit.Vector max = getRegion().getMaximumPoint();
        final int minX = min.getBlockX();
        final int minZ = min.getBlockZ();
        final int minY = min.getBlockY();
        final int maxX = max.getBlockX();
        final int maxZ = max.getBlockZ();
        Chunk c;
        for (int x = minX; x <= maxX; x += 16) {
            for (int z = minZ; z <= maxZ; z += 16) {
                c = getWorld().getBlockAt(x, minY, z).getChunk();
                if (!chunkList.contains(c)) chunkList.add(c);
            }
        }
        for (final Chunk aChunk : chunkList) {
            try {
                server.getScheduler().runTaskLater(inst, () -> {
                    for (BlockState aSign : aChunk.getTileEntities()) {
                        if (!(aSign instanceof Sign)) continue;
                        checkHeadStone((Sign) aSign);
                    }
                }, chunkList.indexOf(aChunk) * 20);
            } catch (NullPointerException ex) {
                findHeadStones();
                log.info("Failed to get head stones for Chunk: " + aChunk.getX() + ", " + aChunk.getZ() + ".");
                return;
            }
        }
    }

    private boolean checkHeadStone(Sign sign) {
        Location l = sign.getLocation();
        if ((l.getBlockY() != 81 && l.getBlockY() != 82) || !contains(l)) return false;
        headStones.add(sign.getLocation());
        return true;
    }

    private void findPressurePlateLockLevers() {
        com.sk89q.worldedit.Vector min = pressurePlateLockArea.getMinimumPoint();
        com.sk89q.worldedit.Vector max = pressurePlateLockArea.getMaximumPoint();
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
                        pressurePlateLocks.put(block.getLocation(), !ChanceUtil.getChance(3));
                    }
                }
            }
        }
    }

    public boolean checkPressurePlateLock() {
        for (Map.Entry<Location, Boolean> lever : pressurePlateLocks.entrySet()) {
            if (!lever.getKey().getBlock().getChunk().isLoaded()) return false;
            Lever aLever = (Lever) lever.getKey().getBlock().getState().getData();
            if (aLever.isPowered() != lever.getValue()) return false;
        }
        ChatUtil.send(getTempleContained(Player.class), "You hear a clicking sound.");
        return true;
    }

    protected void resetPressurePlateLock() {
        BlockState state;
        for (Location entry : pressurePlateLocks.keySet()) {
            if (!entry.getBlock().getChunk().isLoaded()) entry.getBlock().getChunk().load();
            state = entry.getBlock().getState();
            Lever lever = (Lever) state.getData();
            lever.setPowered(false);
            state.setData(lever);
            state.update(true);
            pressurePlateLocks.put(entry, !ChanceUtil.getChance(3));
        }
    }

    private void findRewardChest() {
        com.sk89q.worldedit.Vector min = rewards.getMinimumPoint();
        com.sk89q.worldedit.Vector max = rewards.getMaximumPoint();
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
                        rewardChest.add(block.getLocation());
                    }
                }
            }
        }
    }

    protected void resetRewardChest() {
        BlockState block;
        Chest chest;
        for (Location location : rewardChest) {
            block = location.getBlock().getState();
            if (!block.getChunk().isLoaded()) block.getChunk().load();
            chest = (Chest) block;
            chest.getBlockInventory().clear();
            int length = chest.getBlockInventory().getContents().length;
            for (int i = 0; i < length * .6; ++i) {
                int target = ChanceUtil.getRandom(length) - 1;
                if (chest.getBlockInventory().getContents()[target] != null) {
                    i--;
                    continue;
                }
                chest.getBlockInventory().setItem(target, pickRandomItem());
            }
            chest.update();
        }
    }

    private ItemStack pickRandomItem() {
        switch (ChanceUtil.getRandom(39)) {
            case 1:
                return CustomItemCenter.build(CustomItems.GEM_OF_LIFE, 6);
            case 3:
                if (!ChanceUtil.getChance(35)) return null;
                return CustomItemCenter.build(CustomItems.FEAR_SWORD);
            case 4:
                if (!ChanceUtil.getChance(35)) return null;
                return CustomItemCenter.build(CustomItems.FEAR_BOW);
            case 5:
                if (!ChanceUtil.getChance(35)) return null;
                return CustomItemCenter.build(CustomItems.UNLEASHED_SWORD);
            case 6:
                if (!ChanceUtil.getChance(35)) return null;
                return CustomItemCenter.build(CustomItems.UNLEASHED_BOW);
            case 7:
                return CustomItemCenter.build(CustomItems.IMBUED_CRYSTAL, ChanceUtil.getRandom(3));
            case 8:
                return CustomItemCenter.build(CustomItems.GEM_OF_DARKNESS, ChanceUtil.getRandom(3));
            case 9:
                return CustomItemCenter.build(CustomItems.BAT_BOW);
            case 10:
                return CustomItemCenter.build(CustomItems.PHANTOM_GOLD, ChanceUtil.getRandom(64));
            case 11:
                return CustomItemCenter.build(CustomItems.ANCIENT_HELMET);
            case 12:
                return CustomItemCenter.build(CustomItems.ANCIENT_CHESTPLATE);
            case 13:
                return CustomItemCenter.build(CustomItems.ANCIENT_LEGGINGS);
            case 14:
                return CustomItemCenter.build(CustomItems.ANCIENT_BOOTS);
            case 15:
                return CustomItemCenter.build(CustomItems.GOD_HELMET);
            case 16:
                return CustomItemCenter.build(CustomItems.GOD_CHESTPLATE);
            case 17:
                return CustomItemCenter.build(CustomItems.GOD_LEGGINGS);
            case 18:
                return CustomItemCenter.build(CustomItems.GOD_BOOTS);
            case 19:
                return CustomItemCenter.build(CustomItems.GOD_PICKAXE);
            case 20:
                return CustomItemCenter.build(CustomItems.LEGENDARY_GOD_PICKAXE);
            case 21:
                return new ItemStack(ItemID.GOLD_BAR, ChanceUtil.getRandom(64));
            case 22:
                return new ItemStack(ItemID.DIAMOND, ChanceUtil.getRandom(64));
            case 23:
                return new ItemStack(ItemID.EMERALD, ChanceUtil.getRandom(64));
            case 24:
                return new ItemStack(ItemID.REDSTONE_DUST, ChanceUtil.getRandom(64));
            case 25:
                return new ItemStack(ItemID.ENDER_PEARL, ChanceUtil.getRandom(16));
            case 26:
                return new ItemStack(ItemID.GOLD_APPLE, ChanceUtil.getRandom(64), (short) 1);
            case 28:
                return new ItemStack(ItemID.SADDLE);
            case 29:
                return new ItemStack(ItemID.HORSE_ARMOR_IRON);
            case 30:
                return new ItemStack(ItemID.HORSE_ARMOR_GOLD);
            case 31:
                return new ItemStack(ItemID.HORSE_ARMOR_DIAMOND);
            default:
                return CustomItemCenter.build(CustomItems.BARBARIAN_BONE, ChanceUtil.getRandom(5));
        }
    }

    private void breakBlock(Entity e, Location location) {
        int chance = e instanceof Player ? 2 : e instanceof CaveSpider ? 30 : 6;
        Block block = location.getBlock();
        BaseBlock bb = new BaseBlock(block.getTypeId(), block.getData());
        BaseBlock crackedBrick = new BaseBlock(BlockID.STONE_BRICK, 2);
        BlockFace[] targets;
        if (bb.getType() == BlockID.AIR) return;
        if (bb.equals(crackedBrick)) {
            targets = new BlockFace[] {BlockFace.SELF};
        } else {
            targets = EnvironmentUtil.getNearbyBlockFaces();
        }
        for (BlockFace face : targets) {
            if (!ChanceUtil.getChance(chance)) continue;
            final Block aBlock = block.getRelative(face);
            Block bBlock = aBlock.getRelative(BlockFace.DOWN);
            if (!BlockType.canPassThrough(bBlock.getTypeId())) continue;
            BaseBlock aBB = new BaseBlock(aBlock.getTypeId(), aBlock.getData());
            int delay = 20;
            if (aBB.equals(crackedBrick)) {
                delay *= .75;
            }
            server.getScheduler().runTaskLater(inst, () -> {
                BaseBlock uABB = new BaseBlock(aBlock.getTypeId(), aBlock.getData());
                if (!accept(uABB, autoBreakable)) {
                    return;
                }
                generalIndex.addItem(new BlockRecord(aBlock.getLocation(location), uABB));
                aBlock.setTypeId(0);
            }, delay);
        }
    }

    private void fogPlayer(Player player) {
        if (ItemUtil.isItem(player.getInventory().getHelmet(), CustomItems.ELDER_CROWN)
                || ItemUtil.hasItem(player, CustomItems.GEM_OF_DARKNESS)) {
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 6, 1), true);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = server.getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }
}

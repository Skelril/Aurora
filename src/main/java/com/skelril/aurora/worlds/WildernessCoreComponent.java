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

import com.sk89q.commandbook.CommandBook;
import com.sk89q.commandbook.session.PersistentSession;
import com.sk89q.commandbook.session.SessionComponent;
import com.sk89q.commandbook.util.entity.player.PlayerUtil;
import com.sk89q.minecraft.util.commands.*;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.aurora.WishingWellComponent;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.admin.AdminState;
import com.skelril.aurora.combat.PvMComponent;
import com.skelril.aurora.combat.PvPComponent;
import com.skelril.aurora.combat.PvPScope;
import com.skelril.aurora.combat.bosses.*;
import com.skelril.aurora.combat.bosses.detail.WBossDetail;
import com.skelril.aurora.events.PlayerAdminModeChangeEvent;
import com.skelril.aurora.events.apocalypse.ApocalypseLocalSpawnEvent;
import com.skelril.aurora.events.entity.item.DropClearPulseEvent;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.modifier.ModifierType;
import com.skelril.aurora.util.*;
import com.skelril.aurora.util.extractor.entity.CombatantPair;
import com.skelril.aurora.util.extractor.entity.EDBEExtractor;
import com.skelril.aurora.util.item.ItemUtil;
import com.skelril.aurora.util.timer.IntegratedRunnable;
import com.skelril.aurora.util.timer.TimedRunnable;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.InjectComponent;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import com.zachsthings.libcomponents.config.ConfigurationBase;
import com.zachsthings.libcomponents.config.Setting;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Torch;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.skelril.aurora.economic.store.AdminStoreComponent.priceCheck;
import static com.skelril.aurora.modifier.ModifierComponent.getModifierManager;
import static org.bukkit.event.entity.EntityDamageEvent.DamageModifier;

@ComponentInformation(friendlyName = "Wilderness Core", desc = "Operate the wilderness.")
@Depend(components = {AdminComponent.class, SessionComponent.class})
public class WildernessCoreComponent extends BukkitComponent implements Listener, Runnable {

    private final CommandBook inst = CommandBook.inst();
    private final Logger log = inst.getLogger();
    private final Server server = CommandBook.server();

    @InjectComponent
    private AdminComponent adminComponent;
    @InjectComponent
    private SessionComponent sessions;

    private WorldGuardPlugin WG;

    private World primus;
    private World wilderness;
    private World wildernessNether;
    private static Set<World> wildernessWorlds = new HashSet<>();

    private long nextDropTime = 0;
    private PvPScope scope;
    private LocalConfiguration config;

    // Boss Handlers
    private Fangz fangz = new Fangz();
    private FearKnight fearKnight = new FearKnight();
    private LostRogue rogue = new LostRogue();
    private StormBringer stormBringer = new StormBringer();
    private GraveDigger graveDigger = new GraveDigger();

    @Override
    public void enable() {

        config = configure(new LocalConfiguration());
        //noinspection AccessStaticViaInstance
        inst.registerEvents(this);

        // Make sure all worlds are loaded
        server.getScheduler().runTaskLater(inst, this::setup, 1);
        // Start TP/Sync task with 2 tick delay
        server.getScheduler().scheduleSyncRepeatingTask(inst, this, 2, 20 * 20);

        registerScope();
        registerCommands(Commands.class);
    }

    @Override
    public void reload() {

        super.reload();
        configure(config);
        setup();
    }

    @Override
    public void run() {
        for (Entity item : wilderness.getEntitiesByClasses(Item.class)) {

            ItemStack stack = ((Item) item).getItemStack();

            if (stack.getAmount() > 1) continue;

            if (item.getTicksLived() > 20 * 60 && stack.getTypeId() == BlockID.SAPLING) {
                item.getLocation().getBlock().setTypeIdAndData(stack.getTypeId(),
                        stack.getData().getData(), true);
                item.remove();
            }
        }
    }

    private void setup() {
        WG = WorldGuardPlugin.inst();

        primus = Bukkit.getWorld(config.primusWorld);

        // Update Wilderness Worlds
        wildernessWorlds.clear();
        wildernessWorlds.add(wilderness = Bukkit.getWorld(config.wildernessWorld));
        wildernessWorlds.add(wildernessNether = Bukkit.getWorld(config.wildernessWorld + "_nether"));
    }

    private void registerScope() {
        PvPComponent.registerScope(scope = new PvPScope() {
            @Override
            public boolean isApplicable(Player player) {
                return isWildernessWorld(player.getWorld());
            }

            @Override
            public boolean allowed(Player attacker, Player defender) {
                return !sessions.getSession(WildernessSession.class, attacker).isIgnored(defender.getName());
            }
        });
    }

    public static boolean isWildernessWorld(World world) {
        return wildernessWorlds.contains(world);
    }

    private static class LocalConfiguration extends ConfigurationBase {

        @Setting("primus.world")
        public String primusWorld = "Primus";
        @Setting("primus.portal")
        public String primusPortal = "primus-portal";
        @Setting("wilderness.world")
        public String wildernessWorld = "Wilderness";
        @Setting("mini-bosses.lost-rogue")
        public int lostRogueChance = 1000;
        @Setting("mini-bosses.fear-knight")
        public int fearKnightChance = 100;
        @Setting("mini-bosses.fangz")
        public int fangzChance = 100;
        @Setting("mini-bosses.stormbringer")
        public int stormBringerChance = 100;
        @Setting("mini-bosses.gravedigger")
        public int graveDiggerChance = 100;
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
            agent.setCanCreatePortal(portalCreation(to));
            event.setPortalTravelAgent(agent);
            if (agent.getCanCreatePortal()) {
                event.setTo(agent.findOrCreate(to));
            } else {
                event.setTo(to);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        TravelAgent agent = event.getPortalTravelAgent();
        Location to = commonPortal(event.getFrom());

        if (!event.getCause().equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL)) return;
        if (to != null) {
            agent.setCanCreatePortal(portalCreation(to));
            event.setPortalTravelAgent(agent);
            if (agent.getCanCreatePortal()) {
                event.setTo(agent.findOrCreate(to));
            } else {
                event.setTo(to);
            }
        }
    }

    private boolean portalCreation(Location to) {
        return wildernessWorlds.contains(to.getWorld());
    }

    private Location commonPortal(Location from) {
        World fromWorld = from.getWorld();

        Location to = null;

        if (fromWorld.equals(wilderness)) {
            for (ProtectedRegion region : WG.getRegionManager(wilderness).getApplicableRegions(from)) {
                if (region.getId().equals(config.primusPortal)) {
                    to = primus.getSpawnLocation();
                    break;
                }
            }
            if (to == null) {
                to = new Location(wildernessNether, from.getBlockX() / 8, from.getY(), from.getZ() / 8);
            }
        } else if (fromWorld.equals(wildernessNether)) {
            to = new Location(wilderness, from.getBlockX() / 8, from.getY(), from.getZ() / 8);
        }
        return to;
    }

    @EventHandler
    public void onAdminModeChange(PlayerAdminModeChangeEvent event) {

        World world = event.getPlayer().getWorld();

        if (event.getNewAdminState().equals(AdminState.SYSOP)) return;
        if (!event.getNewAdminState().equals(AdminState.MEMBER) && isWildernessWorld(world)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {

        if (event.getTo().getWorld() != event.getFrom().getWorld()) {
            check(event.getPlayer(), event.getTo().getWorld());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldChange(PlayerChangedWorldEvent event) {

        Player player = event.getPlayer();

        check(player, player.getWorld());
    }

    public void check(Player player, World world) {

        if (isWildernessWorld(world) && adminComponent.isAdmin(player)) {
            adminComponent.deadmin(player);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (isWildernessWorld(player.getWorld())) {
            // Catch possible escapes
            if (adminComponent.isAdmin(player)) {
                adminComponent.deadmin(player);
                return;
            }

            if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
            ItemStack held = player.getItemInHand();
            if (held == null || !ItemUtil.isPickAxe(held.getTypeId())) return;
            Block origin = event.getClickedBlock();
            Block target = origin.getRelative(event.getBlockFace());
            if (origin.getType().isSolid() && target.getType() == Material.AIR && WGBukkit.getPlugin().canBuild(player, target)) {
                if (!player.getInventory().removeItem(new ItemStack(BlockID.TORCH)).isEmpty()) return;
                player.updateInventory();
                target.setType(Material.TORCH);
                BlockState torchState = target.getState();
                if (torchState.getData() instanceof Torch) {
                    Torch torch = (Torch) torchState.getData();
                    torch.setFacingDirection(event.getBlockFace());
                    torchState.update();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent event) {

        LivingEntity entity = event.getEntity();

        if (!(entity instanceof Monster)) return;

        Location location = event.getLocation();
        int level = getLevel(location);
        if (isWildernessWorld(location.getWorld()) && level > 1) {
            double max = entity.getMaxHealth();

            // We want the health to be boosted only after level 1
            // but not skip an increment
            entity.setMaxHealth(max * 5 * (level - 1));
            entity.setHealth(max * 5 * (level - 1));

            if (entity instanceof Zombie) {
                if (ChanceUtil.getChance(config.lostRogueChance)) {
                    rogue.bind((Zombie) entity, new WBossDetail(level));
                } else if (ChanceUtil.getChance(config.fearKnightChance)) {
                    fearKnight.bind((Zombie) entity, new WBossDetail(level));
                }
            } else if (entity instanceof Spider) {
                if (ChanceUtil.getChance(config.fangzChance)) {
                    fangz.bind((Spider) entity, new WBossDetail(level));
                }
            } else if (entity instanceof Skeleton) {
                if (ChanceUtil.getChance(config.stormBringerChance)) {
                    stormBringer.bind((Skeleton) entity, new WBossDetail(level));
                } else if (ChanceUtil.getChance(config.graveDiggerChance)) {
                    graveDigger.bind((Skeleton) entity, new WBossDetail(level));
                }
            }
        }
    }

    private static Set<EntityDamageEvent.DamageCause> ignoredDamage = new HashSet<>();

    static {
        ignoredDamage.add(EntityDamageEvent.DamageCause.WITHER);
        ignoredDamage.add(EntityDamageEvent.DamageCause.FIRE_TICK);
        ignoredDamage.add(EntityDamageEvent.DamageCause.POISON);
        ignoredDamage.add(EntityDamageEvent.DamageCause.CONTACT);
        ignoredDamage.add(EntityDamageEvent.DamageCause.DROWNING);
        ignoredDamage.add(EntityDamageEvent.DamageCause.FALL);
        ignoredDamage.add(EntityDamageEvent.DamageCause.STARVATION);
        ignoredDamage.add(EntityDamageEvent.DamageCause.SUFFOCATION);
        ignoredDamage.add(EntityDamageEvent.DamageCause.VOID);
        ignoredDamage.add(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION);
        ignoredDamage.add(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {

        final Entity entity = event.getEntity();

        Location location = entity.getLocation();
        if (!isWildernessWorld(location.getWorld())) return;

        if (event instanceof EntityDamageByEntityEvent) {
            if (onPlayerDamage((EntityDamageByEntityEvent) event)) {
                return;
            }
        }

        int level = getLevel(location);

        // Make Skeletons & Zombies burn faster
        if (entity instanceof Zombie || entity instanceof Skeleton) {
            if (event.getCause().equals(EntityDamageEvent.DamageCause.FIRE_TICK)
                    && location.getBlock().getLightFromSky() == 15) {
                event.setDamage(event.getDamage() * 5 * level);
                return;
            }
        }

        if (!(entity instanceof Player) || ignoredDamage.contains(event.getCause())) return;

        event.setDamage(DamageModifier.BASE, event.getDamage() + ChanceUtil.getRandom(ChanceUtil.getRandom(level)) - 1);
        if (((Player) entity).isFlying() && event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
            DamageUtil.multiplyFinalDamage(event, 2);
        }
    }

    private static EDBEExtractor<Player, LivingEntity, Projectile> extractor = new EDBEExtractor<>(
            Player.class,
            LivingEntity.class,
            Projectile.class
    );

    public boolean onPlayerDamage(EntityDamageByEntityEvent event) {

        CombatantPair<Player, LivingEntity, Projectile> result = extractor.extractFrom(event);

        if (result == null) return false;

        final Player attacker = result.getAttacker();
        LivingEntity defender = result.getDefender();

        // The defender is a player, we don't want to print the HP, but at the same time
        // we don't want to allow the processing of onEntityDamage to continue,
        // so we return true instead of false to end the cycle.
        if (defender instanceof Player) {
            if (!scope.checkFor(attacker, (Player) defender)) {
                event.setCancelled(true);
            } else if (!scope.checkFor((Player) defender, attacker)) {
                // Auto unignore players when they successfully attack a player who is ignoring them
                sessions.getSession(WildernessSession.class, (Player) defender).unignore(attacker.getName());
            }
            return true;
        }

        PvMComponent.printHealth(attacker, defender);
        return true;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onMobDeath(EntityDeathEvent event) {

        Entity entity = event.getEntity();

        if (!(entity instanceof Monster) || event.getDroppedExp() < 1) return;

        Location location = entity.getLocation();
        int level = getLevel(location);
        if (isWildernessWorld(location.getWorld()) && level > 1) {

            List<ItemStack> drops = new ArrayList<>();
            double diffLevel = Math.max(1, level * .63);
            for (int i = 0; i < Math.pow(diffLevel, 3); i++) {
                if (ChanceUtil.getChance(100000)) {
                    drops.add(CustomItemCenter.build(CustomItems.RED_FEATHER));
                }
                if (ChanceUtil.getChance(2000)) {
                    drops.add(CustomItemCenter.build(CustomItems.POTION_OF_RESTITUTION));
                }
                if (ChanceUtil.getChance(2000)) {
                    drops.add(CustomItemCenter.build(CustomItems.SCROLL_OF_SUMMATION));
                }
            }

            drops.addAll(
                    WishingWellComponent.getCalculatedLoot(server.getConsoleSender(), 1, Math.pow(level, 2) * 64)
            );
            if (getModifierManager().isActive(ModifierType.DOUBLE_WILD_DROPS)) {
                drops.addAll(drops.stream().map(ItemStack::clone).collect(Collectors.toList()));
            }
            event.getDrops().addAll(drops);
            event.setDroppedExp(event.getDroppedExp() * level);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();

        if (!isWildernessWorld(player.getWorld()) || adminComponent.isAdmin(player)) return;

        boolean hasChest = false;

        List<ItemStack> grave = new ArrayList<>();
        event.getDrops().sort((o1, o2) -> (int) (priceCheck(o2, false) - priceCheck(o1, false)));
        Iterator<ItemStack> it = event.getDrops().iterator();
        int kept = 9;
        while (it.hasNext()) {
            ItemStack next = it.next();
            if (next.getTypeId() == BlockID.CHEST) {
                hasChest = true;
                if (next.getAmount() > 1) {
                    next.setAmount(next.getAmount() - 1);
                } else {
                    it.remove();
                    continue;
                }
            }
            if (kept > 0) {
                it.remove();
                grave.add(next);
                kept--;
            }
        }

        Location location = player.getLocation();
        Block block = location.getBlock();
        if (hasChest && WGBukkit.getPlugin().canBuild(player, block)) {
            try {
                graveSupplier:
                {
                    checkGrave:
                    {
                        for (BlockFace face : BlockFace.values()) {
                            if (face.getModY() != 0) continue;
                            Block aBlock = block.getRelative(face);
                            if (aBlock.getTypeId() == BlockID.CHEST) {
                                block = aBlock;
                                break checkGrave;
                            }
                        }
                        block.setTypeIdAndData(BlockID.CHEST, (byte) 0, true);
                    }
                    Chest chest = (Chest) block.getState();
                    it = grave.iterator();
                    Inventory blockInv = chest.getInventory();
                    for (int i = 0; it.hasNext(); ++i) {
                        while (blockInv.getItem(i) != null) {
                            ++i;
                            if (i >= blockInv.getSize()) {
                                ChatUtil.sendError(player, "Some items could not be added to your grave!");
                                break graveSupplier;
                            }
                        }
                        blockInv.setItem(i, it.next());
                        it.remove();
                    }
                    ChatUtil.send(player, "A grave has been created where you died.");
                }
            } catch (Exception ex) {
                log.warning("Location could not be found to create a grave for: " + player.getName());
                ex.printStackTrace();
            }
        }
        event.getDrops().addAll(grave);
    }

    public int getLevel(Location location) {

        // Not in Wilderness
        if (!isWildernessWorld(location.getWorld())) {
            return 0;
        }

        // In Wilderness
        return Math.max(0, Math.max(Math.abs(location.getBlockX()), Math.abs(location.getBlockZ())) / 1000) + 1;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        final BlockState block = event.getBlock().getState();

        if (!isWildernessWorld(player.getWorld())) return;

        if (isEffectedOre(block.getTypeId())) {

            ItemStack stack = player.getItemInHand();

            addPool(block, ItemUtil.fortuneLevel(stack), stack.containsEnchantment(Enchantment.SILK_TOUCH));
        }  /* else if (block.getTypeId() == BlockID.SAPLING) {
            event.setCancelled(true);
            ChatUtil.sendError(player, "You cannot break that here.");
        }
        */

        event.setExpToDrop(event.getExpToDrop() * getLevel(block.getLocation()));

        /*
        if (isTree(block)) {

            Block treeBase = block.getBlock();
            while (treeBase.getTypeId() != BlockID.GRASS && treeBase.getTypeId() != BlockID.DIRT) {
                treeBase = treeBase.getRelative(BlockFace.DOWN);
                if (treeBase.getY() < 3) break;
            }

            treeBase = treeBase.getRelative(BlockFace.UP);

            treeBase.breakNaturally();
            treeBase.setTypeIdAndData(BlockID.SAPLING, block.getData().getData(), true);
        }
        */
    }

    private boolean isTree(BlockState block) {

        int worldHeightLimit = block.getWorld().getMaxHeight();
        if (block.getTypeId() == BlockID.LOG) {

            Block testBlock = block.getBlock();
            do {
                testBlock = testBlock.getRelative(BlockFace.UP);
                Location[] nearbyLocs = LocationUtil.getNearbyLocations(testBlock.getLocation(), 1);
                for (Location aLocation : nearbyLocs) {
                    if (aLocation.getBlock().getTypeId() == BlockID.LEAVES) return true;
                }
            } while (testBlock.getY() < Math.min(worldHeightLimit, testBlock.getY() + 20));
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {

        Entity entity = event.getEntity();
        if (!(entity instanceof Creeper || entity instanceof Fireball)) return;

        Location loc = entity.getLocation();
        if (!isWildernessWorld(loc.getWorld())) return;

        float min = event.getRadius();
        event.setRadius(Math.min(entity instanceof Fireball ? 4 : 9, Math.max(min, (min + getLevel(loc)) / 2)));

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplosion(EntityExplodeEvent event) {

        if (!isWildernessWorld(event.getLocation().getWorld())) return;

        event.setYield(.1F);

        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Block next = it.next();
            if (isEffectedOre(next.getTypeId())) {
                addPool(next.getState(), 0, false);
                continue;
            }

            if (next.getTypeId() == BlockID.CHEST) {
                it.remove();
                continue;
            }
        }
    }

    @EventHandler
    public void onDropClearPulse(DropClearPulseEvent event) {
        if (isWildernessWorld(event.getWorld())) {
            nextDropTime = System.currentTimeMillis() + (event.getSecondsLeft() * 1000);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLocalSpawn(ApocalypseLocalSpawnEvent event) {

        Location location = event.getLocation();

        if (!isWildernessWorld(location.getWorld())) return;

        int level = getLevel(location);
        if (!ChanceUtil.getChance(level * level)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {

        Player player = event.getPlayer();

        if (isWildernessWorld(player.getWorld()) && !adminComponent.isAdmin(player)) {
            int typeId = event.getBlock().getTypeId();
            if (isEffectedOre(typeId)) {
                event.setCancelled(true);
                ChatUtil.sendError(player, "You find yourself unable to place that ore.");
            } /* else if (typeId == BlockID.SAPLING) {
                event.setCancelled(true);
                ChatUtil.sendError(player, "You cannot plant that here.");
            }
            */
        }
    }

    public class Commands {

        @Command(aliases = {"wlevel", "wl"},
                usage = "[target level]", desc = "Get current wilderness level",
                flags = "", min = 0, max = 1)
        public void lostPotionOfRestitutionCmd(CommandContext args, CommandSender sender) throws CommandException {

            int level;
            if (args.argsLength() > 0) {
                level = args.getInteger(0);
            } else {
                level = Math.max(1, getLevel(PlayerUtil.checkPlayer(sender).getLocation()));
            }
            ChatUtil.send(sender, "Current Level: " + (level == 0 ? ChatColor.RED + "Not available" : level)
                    + ChatColor.YELLOW + ", Target Level: " + level + ".\n");

            DecimalFormat df = new DecimalFormat("#.#");

            ChatUtil.send(sender, "Damage Modifier: +" + (level - 1));
            ChatUtil.send(sender, "Ore Pool Modifier: " + df.format(getOreMod(level)) + "x");
            ChatUtil.send(sender, "Mob Health Modifier: " + (level > 1 ? 5 * (level - 1) : 1) + "x");
        }

        @Command(aliases = {"wparty", "wp"}, desc = "Party Commands")
        @NestedCommand({PartyCommands.class})
        public void partyCommands(CommandContext args, CommandSender sender) throws CommandException {

        }

        @Command(aliases = {"wboss"}, desc = "Boss Commands")
        @NestedCommand({BossCommands.class})
        @CommandPermissions("aurora.wilderness.boss")
        public void bossCommands(CommandContext args, CommandSender sender) throws CommandException {

        }
    }

    public class PartyCommands {

        @Command(aliases = {"add"},
                usage = "<player[, player]>", desc = "Ignore a player",
                flags = "", min = 1, max = 1)
        public void ignore(CommandContext args, CommandSender sender) throws CommandException {
            WildernessSession session = sessions.getSession(WildernessSession.class, PlayerUtil.checkPlayer(sender));
            String[] targets = args.getString(0).split(",");
            for (String target : targets) {
                session.ignore(target);
                ChatUtil.send(sender, "You will no longer be able to damage " + target + ", unless attacked first.");
            }
        }

        @Command(aliases = {"remove"},
                usage = "<player[, player]>", desc = "Unignore a player",
                flags = "", min = 1, max = 1)
        public void unignore(CommandContext args, CommandSender sender) throws CommandException {
            WildernessSession session = sessions.getSession(WildernessSession.class, PlayerUtil.checkPlayer(sender));
            String[] targets = args.getString(0).split(",");
            for (String target : targets) {
                session.unignore(target);
                ChatUtil.send(sender, "You will now be able to damage " + target + ".");
            }
        }

    }

    public class BossCommands {

        @Command(aliases = {"lostrogue"},
                usage = "[level]", desc = "Spawn a lost rogue",
                flags = "", min = 0, max = 1)
        public void lostRogue(CommandContext args, CommandSender sender) throws CommandException {
            Player player = PlayerUtil.checkPlayer(sender);
            int level = args.argsLength() < 1 ? getLevel(player.getLocation()) : args.getInteger(0);
            if (level < 1) {
                throw new CommandException("You are not in a Wilderness world, please specify a level.");
            }
            Zombie zombie = player.getLocation().getWorld().spawn(player.getLocation(), Zombie.class);
            WBossDetail detail = new WBossDetail(level);
            rogue.bind(zombie, detail);
        }

        @Command(aliases = {"fangz"},
                usage = "[level]", desc = "Spawn a fangz",
                flags = "", min = 0, max = 1)
        public void fangz(CommandContext args, CommandSender sender) throws CommandException {
            Player player = PlayerUtil.checkPlayer(sender);
            int level = args.argsLength() < 1 ? getLevel(player.getLocation()) : args.getInteger(0);
            if (level < 1) {
                throw new CommandException("You are not in a Wilderness world, please specify a level.");
            }
            Spider spider = player.getLocation().getWorld().spawn(player.getLocation(), Spider.class);
            WBossDetail detail = new WBossDetail(level);
            fangz.bind(spider, detail);
        }

        @Command(aliases = {"fearknight"},
                usage = "[level]", desc = "Spawn a Fear Knight",
                flags = "", min = 0, max = 1)
        public void fearKnight(CommandContext args, CommandSender sender) throws CommandException {
            Player player = PlayerUtil.checkPlayer(sender);
            int level = args.argsLength() < 1 ? getLevel(player.getLocation()) : args.getInteger(0);
            if (level < 1) {
                throw new CommandException("You are not in a Wilderness world, please specify a level.");
            }
            Zombie zombie = player.getLocation().getWorld().spawn(player.getLocation(), Zombie.class);
            WBossDetail detail = new WBossDetail(level);
            fearKnight.bind(zombie, detail);
        }

        @Command(aliases = {"stormbringer"},
                usage = "[level]", desc = "Spawn a Storm Bringer",
                flags = "", min = 0, max = 1)
        public void stormBringer(CommandContext args, CommandSender sender) throws CommandException {
            Player player = PlayerUtil.checkPlayer(sender);
            int level = args.argsLength() < 1 ? getLevel(player.getLocation()) : args.getInteger(0);
            if (level < 1) {
                throw new CommandException("You are not in a Wilderness world, please specify a level.");
            }
            Skeleton skeleton = player.getLocation().getWorld().spawn(player.getLocation(), Skeleton.class);
            skeleton.getEquipment().setItemInHand(new ItemStack(Material.BOW));
            WBossDetail detail = new WBossDetail(level);
            stormBringer.bind(skeleton, detail);
        }

        @Command(aliases = {"gravedigger"},
                usage = "[level]", desc = "Spawn a Grave Digger",
                flags = "", min = 0, max = 1)
        public void graveDigger(CommandContext args, CommandSender sender) throws CommandException {
            Player player = PlayerUtil.checkPlayer(sender);
            int level = args.argsLength() < 1 ? getLevel(player.getLocation()) : args.getInteger(0);
            if (level < 1) {
                throw new CommandException("You are not in a Wilderness world, please specify a level.");
            }
            Skeleton skeleton = player.getLocation().getWorld().spawn(player.getLocation(), Skeleton.class);
            skeleton.getEquipment().setItemInHand(new ItemStack(Material.BOW));
            WBossDetail detail = new WBossDetail(level);
            graveDigger.bind(skeleton, detail);
        }
    }

    private int getOreMod(int level) {
        double modifier = Math.max(1, (level * 3));
        if (getModifierManager().isActive(ModifierType.DOUBLE_WILD_ORES)) {
            modifier *= 2;
        }
        return (int) modifier;
    }

    private void addPool(final BlockState block, final int fortuneLevel, final boolean hasSilkTouch) {

        final Location location = block.getLocation();
        final World world = location.getWorld();

        ItemStack generalDrop = EnvironmentUtil.getOreDrop(block.getTypeId(), hasSilkTouch);
        final int fortune = EnvironmentUtil.isOre(generalDrop.getTypeId()) ? 0 : fortuneLevel;
        final int times = ChanceUtil.getRandom(getOreMod(getLevel(location)));
        final float vol = ((float) 1 / times);
        IntegratedRunnable dropper = new IntegratedRunnable() {
            @Override
            public boolean run(int timesL) {

                if (nextDropTime != 0 && System.currentTimeMillis() < nextDropTime) return false;

                for (int i = 0; i < ItemUtil.fortuneModifier(block.getTypeId(), fortune); i++) {
                    world.dropItem(location, EnvironmentUtil.getOreDrop(block.getTypeId(), hasSilkTouch));
                }
                world.playSound(location, Sound.BLAZE_BREATH, Math.min(1, (((float) timesL / times) * .6F) + vol), 0);
                return true;
            }

            @Override
            public void end() {

                world.playSound(location, Sound.BLAZE_DEATH, .2F, 0);
            }
        };

        TimedRunnable runnable = new TimedRunnable(dropper, times);

        BukkitTask task = server.getScheduler().runTaskTimer(inst, runnable, 20, 20);

        runnable.setTask(task);
    }

    /*
    private static final int[] ores = {
            BlockID.GOLD_ORE
    };
    */

    private boolean isEffectedOre(int typeId) {

        /*
        for (int ore : ores) {
            if (ore == typeId) return true;
        }
        */
        return EnvironmentUtil.isOre(typeId);
    }

    private static class WildernessSession extends PersistentSession {

        public static final long MAX_AGE = TimeUnit.DAYS.toMillis(1);

        private UUID lastAttacked = null;
        private Set<String> ignored = new HashSet<>();

        protected WildernessSession() {
            super(MAX_AGE);
        }

        public boolean checkLast(UUID lastAttacked) {

            if (this.lastAttacked == lastAttacked) {
                return true;
            }

            this.lastAttacked = lastAttacked;

            return false;
        }

        public boolean isIgnored(String player) {
            return ignored.contains(player);
        }

        public void ignore(String player) {
            ignored.add(player);
        }

        public void unignore(String player) {
            ignored.remove(player);
        }
    }
}
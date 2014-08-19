/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.CursedMine;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.admin.AdminState;
import com.skelril.aurora.events.PlayerInstanceDeathEvent;
import com.skelril.aurora.events.PrayerApplicationEvent;
import com.skelril.aurora.events.shard.PartyActivateEvent;
import com.skelril.aurora.modifier.ModifierType;
import com.skelril.aurora.shard.instance.CursedMine.hitlist.HitList;
import com.skelril.aurora.shard.instance.ShardListener;
import com.skelril.aurora.util.*;
import com.skelril.aurora.util.item.BookUtil;
import com.skelril.aurora.util.item.ItemUtil;
import com.skelril.aurora.util.player.PlayerRespawnProfile_1_7_10;
import com.skelril.aurora.util.restoration.BlockRecord;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.skelril.aurora.modifier.ModifierComponent.getModifierCenter;

public class CursedMineListener extends ShardListener<CursedMine> {
    public CursedMineListener(CursedMine shard) {
        super(shard);
    }

    @EventHandler
    public void onPartyActivate(PartyActivateEvent event) {
        if (!event.hasInstance() && shard.matchesShard(event.getShard())) {
            CursedMineInstance instance;
            if (shard.getInstances().isEmpty()) {
                instance = shard.makeInstance();
            } else {
                instance = CollectionUtil.getElement(shard.getInstances());
            }
            instance.teleportTo(shard.wrapPlayers(event.getPlayers()));
            event.setInstance(instance);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        CursedMineInstance inst = shard.getInstance(player);
        if (inst == null) return;

        InventoryType.SlotType st = event.getSlotType();
        if (st.equals(InventoryType.SlotType.CRAFTING)) {
            event.setResult(Event.Result.DENY);
            ChatUtil.sendWarning(player, "You cannot use that here.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDrag(InventoryDragEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        CursedMineInstance inst = shard.getInstance(player);
        if (inst == null) return;

        event.setResult(Event.Result.DENY);
        ChatUtil.sendWarning(player, "You cannot do that here.");
    }

    private static Set<Integer> triggerBlocks = new HashSet<>();
    private static Set<Action> triggerInteractions = new HashSet<>();

    static {
        triggerBlocks.add(BlockID.STONE_BUTTON);
        triggerBlocks.add(BlockID.TRIPWIRE);

        triggerInteractions.add(Action.PHYSICAL);
        triggerInteractions.add(Action.RIGHT_CLICK_BLOCK);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        final Block block = event.getClickedBlock();

        CursedMineInstance inst = shard.getInstance(block);
        if (inst != null && triggerBlocks.contains(block.getTypeId()) && triggerInteractions.contains(event.getAction())) {
            inst.activatePumps();
        }
    }

    private boolean hasSilkTouch(ItemStack item) {
        return item.containsEnchantment(Enchantment.SILK_TOUCH);
    }

    private boolean hasFortune(ItemStack item) {
        return item.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final int typeId = block.getTypeId();

        ItemStack itemInHand = player.getItemInHand();
        CursedMineInstance inst = shard.getInstance(block);

        if (inst != null) {
            AdminComponent admin = inst.getMaster().getAdmin();
            if (!admin.isAdmin(player, AdminState.ADMIN)) {
                if (EnvironmentUtil.isValuableOre(block)
                        && itemInHand != null
                        && ItemUtil.isPickAxe(itemInHand.getTypeId())) {
                    if (ChanceUtil.getChance(4)) {
                        ItemStack rawDrop = EnvironmentUtil.getOreDrop(block.getTypeId(), hasSilkTouch(itemInHand));

                        if (inst().hasPermission(player, "aurora.mining.burningember") && !hasSilkTouch(itemInHand)) {
                            switch (typeId) {
                                case BlockID.GOLD_ORE:
                                    rawDrop.setTypeId(ItemID.GOLD_BAR);
                                    break;
                                case BlockID.IRON_ORE:
                                    rawDrop.setTypeId(ItemID.IRON_BAR);
                                    break;
                            }
                        }

                        if (hasFortune(itemInHand) && !EnvironmentUtil.isOre(rawDrop.getTypeId())) {
                            rawDrop.setAmount(rawDrop.getAmount()
                                    * ItemUtil.fortuneModifier(typeId, ItemUtil.fortuneLevel(itemInHand)));
                        }
                        rawDrop.setAmount(rawDrop.getAmount() * ChanceUtil.getRangedRandom(4, 8));

                        player.getInventory().addItem(rawDrop);
                        if (getModifierCenter().isActive(ModifierType.DOUBLE_CURSED_ORES)) {
                            player.getInventory().addItem(rawDrop.clone());
                        }
                    }
                    event.setExpToDrop((70 - player.getLocation().getBlockY()) / 2);

                    if (ChanceUtil.getChance(3000)) {
                        ChatUtil.sendNotice(player, "You feel as though a spirit is trying to tell you something...");
                        player.getInventory().addItem(BookUtil.Lore.Areas.theGreatMine());
                    }

                    inst.eatFood(player);
                    inst.poison(player, 6);
                    inst.ghost(player, typeId);

                    inst.recordBlockBreak(player, new BlockRecord(block));
                    inst.getMaster().getRestoreUtil().blockAndLogEvent(event);
                } else {
                    event.setCancelled(true);
                    ChatUtil.sendWarning(player, "You cannot break this block for some reason.");
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {

        Player player = event.getPlayer();
        CursedMineInstance inst = shard.getInstance(player);
        if (inst != null && !inst.getMaster().getAdmin().isAdmin(player, AdminState.ADMIN)) {
            event.setCancelled(true);
            ChatUtil.sendNotice(player, ChatColor.DARK_RED, "You don't have permission for this area.");
        }
    }

    private static List<PlayerTeleportEvent.TeleportCause> accepted = new ArrayList<>();

    static {
        accepted.add(PlayerTeleportEvent.TeleportCause.UNKNOWN);
        accepted.add(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (!accepted.contains(event.getCause())) {
            CursedMineInstance inst = shard.getInstance(player);
            HitList hitList = shard.getShard().getHitList();
            if ((inst != null && inst.hasRecordForPlayer(player)) || hitList.isOnHitList(player)) {
                event.setCancelled(true);
                ChatUtil.sendWarning(player, "You have been tele-blocked!");
            }
        }
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
            agent.setCanCreatePortal(false);
            event.setPortalTravelAgent(agent);
            event.setTo(to);
        }
    }

    private Location commonPortal(Location from) {
        CursedMineInstance inst = shard.getInstance(from);
        return inst == null ? null : shard.getManager().getPrimusSpawn();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Block block = event.getBlockClicked();
        CursedMineInstance inst = shard.getInstance(block);
        if (inst != null) {
            inst.recordBlockBreak(event.getPlayer(), new BlockRecord(block));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockClicked();
        CursedMineInstance inst = shard.getInstance(block);
        if (inst != null && !inst.getMaster().getAdmin().isAdmin(player)) {
            event.setCancelled(true);
            ChatUtil.sendNotice(player, ChatColor.DARK_RED, "You don't have permission for this area.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrayerApplication(PrayerApplicationEvent event) {
        Player player = event.getPlayer();
        if (event.getCause().getEffect().getType().isHoly()) {
            CursedMineInstance inst = shard.getInstance(player);
            if (inst != null) {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onPlayerInstDeath(PlayerInstanceDeathEvent event) {
        CursedMineInstance inst = shard.getInstance(event.getPlayer());
        if (inst == null) return;
        PlayerRespawnProfile_1_7_10 profile = event.getProfile();
        profile.setArmorAction(KeepAction.DROP);
        profile.setInvAction(KeepAction.DROP);
        profile.setLevelAction(KeepAction.DESTROY);
        profile.setExperienceAction(KeepAction.DESTROY);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity().getPlayer();
        CursedMineInstance inst = shard.getInstance(player);
        HitList hitList = shard.getShard().getHitList();
        if (inst != null || hitList.isOnHitList(player)) {
            CursedMineInstance effectiveInstance = inst;
            if (effectiveInstance == null) {
                effectiveInstance = hitList.getAssigningInstance(player);
            }

            effectiveInstance.revertPlayer(player);

            if (inst != null && ChanceUtil.getChance(500)) {
                ChatUtil.sendNotice(player, "You feel as though a spirit is trying to tell you something...");
                event.getDrops().add(BookUtil.Lore.Areas.theGreatMine());
            }

            if (hitList.isOnHitList(player)) {
                hitList.remPlayer(player);
            }

            switch (ChanceUtil.getRandom(11)) {
                case 1:
                    event.setDeathMessage(player.getName() + " was killed by Dave");
                    break;
                case 2:
                    event.setDeathMessage(player.getName() + " got on Dave's bad side");
                    break;
                case 3:
                    event.setDeathMessage(player.getName() + " was slain by an evil spirit");
                    break;
                case 4:
                    event.setDeathMessage(player.getName() + " needs to stay away from the cursed mine");
                    break;
                case 5:
                    event.setDeathMessage(player.getName() + " enjoys death a little too much");
                    break;
                case 6:
                    event.setDeathMessage(player.getName() + " seriously needs to stop mining");
                    break;
                case 7:
                    event.setDeathMessage(player.getName() + " angered an evil spirit");
                    break;
                case 8:
                    event.setDeathMessage(player.getName() + " doesn't get a cookie from COOKIE");
                    break;
                case 9:
                    event.setDeathMessage(player.getName() + " should stay away");
                    break;
                case 10:
                    event.setDeathMessage(player.getName() + " needs to consider retirement");
                    break;
                case 11:
                    event.setDeathMessage(player.getName() + "'s head is now on Dave's mantel");
                    break;
            }
            //addSkull(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        HitList hitList = shard.getShard().getHitList();
        if (hitList.isOnHitList(player)) {
            player.setHealth(0);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CursedMineInstance inst = shard.getInstance(player);
        if (inst != null && inst.hasRecordForPlayer(player)) {
            inst.getMaster().getHitList().addPlayer(player, inst);
        }
    }
}

/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.items;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.commandbook.session.SessionComponent;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.anticheat.AntiCheatCompatibilityComponent;
import com.skelril.aurora.combat.PvPComponent;
import com.skelril.aurora.events.custom.item.HymnSingEvent;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.items.generic.AbstractItemFeatureImpl;
import com.skelril.aurora.items.implementations.*;
import com.skelril.aurora.items.implementations.combotools.*;
import com.skelril.aurora.prayer.PrayerComponent;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.ItemCondenser;
import com.skelril.aurora.util.item.InventoryUtil;
import com.skelril.aurora.util.item.ItemUtil;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.InjectComponent;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

/**
 * Author: Turtle9598
 */
@ComponentInformation(friendlyName = "Global Items Component", desc = "Global Custom Item effects")
@Depend(components = {SessionComponent.class, AdminComponent.class,
        AntiCheatCompatibilityComponent.class, PvPComponent.class,
        PrayerComponent.class
})
public class GlobalItemsComponent extends BukkitComponent implements Listener {

    private static final CommandBook inst = CommandBook.inst();
    private static final Server server = CommandBook.server();

    @InjectComponent
    protected static AdminComponent admin;
    @InjectComponent
    protected static SessionComponent sessions;
    @InjectComponent
    protected static PrayerComponent prayers;

    private static ItemCondenser goldCondenser = new ItemCondenser();

    static {
        goldCondenser.addSupport(new ItemStack(ItemID.GOLD_NUGGET, 9), new ItemStack(ItemID.GOLD_BAR, 1));
        goldCondenser.addSupport(new ItemStack(ItemID.GOLD_BAR, 9), new ItemStack(BlockID.GOLD_BLOCK, 1));
    }

    private static ItemCondenser summationCondenser = new ItemCondenser();

    static {
        // Coal
        summationCondenser.addSupport(new ItemStack(ItemID.COAL, 9), new ItemStack(BlockID.COAL_BLOCK, 1));

        // Iron
        summationCondenser.addSupport(new ItemStack(ItemID.IRON_BAR, 9), new ItemStack(BlockID.IRON_BLOCK, 1));

        // Gold
        summationCondenser.addSupport(new ItemStack(ItemID.GOLD_NUGGET, 9), new ItemStack(ItemID.GOLD_BAR, 1));
        summationCondenser.addSupport(new ItemStack(ItemID.GOLD_BAR, 9), new ItemStack(BlockID.GOLD_BLOCK, 1));

        // Redstone
        summationCondenser.addSupport(new ItemStack(ItemID.REDSTONE_DUST, 9), new ItemStack(BlockID.REDSTONE_BLOCK, 1));

        // Lapis
        summationCondenser.addSupport(new ItemStack(ItemID.INK_SACK, 9, (byte) 4), new ItemStack(BlockID.LAPIS_LAZULI_BLOCK, 1));

        // Diamond
        summationCondenser.addSupport(new ItemStack(ItemID.DIAMOND, 9), new ItemStack(BlockID.DIAMOND_BLOCK, 1));

        // Emerald
        summationCondenser.addSupport(new ItemStack(ItemID.EMERALD, 9), new ItemStack(BlockID.EMERALD_BLOCK, 1));
    }

    @Override
    public void enable() {

        //noinspection AccessStaticViaInstance
        inst.registerEvents(this);
        loadResources();
        registerSpecWeapons();
        registerHymns();
        registerTools();
        registerGeneral();
    }

    private Economy getEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = server().getServicesManager().getRegistration(net.milkbowl
                .vault.economy.Economy.class);
        if (economyProvider != null) {
            return economyProvider.getProvider();
        }
        return null;
    }

    private <T extends Listener> T handle(T component) {
        //noinspection AccessStaticViaInstance
        inst.registerEvents(component);
        return component;
    }

    private void loadResources() {
        AbstractItemFeatureImpl.applyResource(admin);
        AbstractItemFeatureImpl.applyResource(sessions);
        AbstractItemFeatureImpl.applyResource(prayers);
    }

    private void registerSpecWeapons() {
        WeaponSysImpl wepSys = handle(new WeaponSysImpl());
        wepSys.addRanged(CustomItems.FEAR_BOW, handle(new FearBowImpl()));
        wepSys.addRanged(CustomItems.UNLEASHED_BOW, handle(new UnleashedBowImpl()));

        wepSys.addMelee(CustomItems.MASTER_SWORD, handle(new MasterSwordImpl()));
        wepSys.addMelee(CustomItems.CORRUPT_MASTER_SWORD, handle(new CorruptMasterSwordImpl()));
        wepSys.addMelee(CustomItems.FEAR_SWORD, handle(new FearSwordImpl()));
        wepSys.addMelee(CustomItems.UNLEASHED_SWORD, handle(new UnleashedSwordImpl()));
    }

    private void registerHymns() {
        HymnImpl hymnImpl = handle(new HymnImpl());
        hymnImpl.addHymn(CustomItems.PHANTOM_HYMN, HymnSingEvent.Hymn.PHANTOM);
        hymnImpl.addHymn(CustomItems.CHICKEN_HYMN, HymnSingEvent.Hymn.CHICKEN);
        hymnImpl.addHymn(CustomItems.HYMN_OF_SUMMATION, HymnSingEvent.Hymn.SUMMATION);

        handle(new ChickenHymnImpl());
        handle(new SummationHymnImpl(summationCondenser));
    }

    private void registerTools() {
        handle(new LinearAxe());
        handle(new LinearPickaxe());
        handle(new LinearShovel());
        handle(new RadialAxe());
        handle(new RadialPickaxe());
        handle(new RadialShovel());
    }

    private void registerGeneral() {
        handle(new AncientArmorImpl());
        handle(new ElderArmorImpl());
        handle(new ElderCrownImpl(goldCondenser));
        handle(new BatBowImpl());
        handle(new ChickenBowImpl());
        handle(new CorruptMasterBowImpl());
        handle(new GodFishImpl());
        handle(new ImbuedCrystalImpl(goldCondenser));
        handle(new MadMilkImpl());
        handle(new MagicBucketImpl());
        handle(new MasterBowImpl());
        handle(new NecrosArmorImpl());
        handle(new NectricArmorImpl());
        handle(new PhantomGoldImpl(getEconomy()));
        handle(new PixieDustImpl());
        handle(new PotionOfRestitutionImpl());
        handle(new RedFeatherImpl());
        handle(new SummationScrollImpl(summationCondenser));
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        InventoryType type = event.getInventory().getType();
        InventoryAction action = event.getAction();

        if (type.equals(InventoryType.ANVIL)) {
            if (action.equals(InventoryAction.NOTHING)) return;
            if (InventoryUtil.getMoveClicks().contains(event.getClick())) {
                event.setResult(Event.Result.DENY);
                ChatUtil.sendError(player, "You cannot move that here.");
                return;
            }

            int rawSlot = event.getRawSlot();

            if (rawSlot < 2) {
                if (InventoryUtil.getPlaceActions().contains(action) && ItemUtil.isNamed(cursorItem)) {
                    boolean isCustomItem = ItemUtil.isAuthenticCustomItem(cursorItem.getItemMeta().getDisplayName());

                    if (!isCustomItem) return;

                    event.setResult(Event.Result.DENY);
                    ChatUtil.sendError(player, "You cannot place that here.");
                }
            } else if (rawSlot == 2) {
                if (InventoryUtil.getPickUpActions().contains(action) && ItemUtil.isNamed(currentItem)) {
                    boolean isCustomItem = ItemUtil.isAuthenticCustomItem(currentItem.getItemMeta().getDisplayName());

                    if (!isCustomItem) return;

                    event.setResult(Event.Result.DENY);
                    ChatUtil.sendError(player, "You cannot name this item that name.");
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDrag(InventoryDragEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) return;

        if (event.getInventory().getType().equals(InventoryType.ANVIL)) {

            for (int i : event.getRawSlots()) {
                if (i + 1 <= event.getInventory().getSize()) {
                    event.setResult(Event.Result.DENY);
                    return;
                }
            }
        }
    }
}

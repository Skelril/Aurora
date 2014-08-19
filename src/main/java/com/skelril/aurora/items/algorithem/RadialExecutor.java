/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.items.algorithem;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.skelril.aurora.events.anticheat.RapidBlockBreakEvent;
import com.skelril.aurora.items.custom.CustomItem;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.items.custom.Tag;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.item.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

import static com.zachsthings.libcomponents.bukkit.BasePlugin.callEvent;

public abstract class RadialExecutor {

    private CustomItems itemType;

    public RadialExecutor(CustomItems itemType) {
        this.itemType = itemType;
    }

    public abstract boolean accepts(int type, int data);

    public void process(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!ItemUtil.isItem(item, itemType)) return;

        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
                handleRightClick(player, item);
                break;
            case LEFT_CLICK_BLOCK:
                handleLeftClick(player, item, event);
                break;
        }
    }

    private int getRadius(ItemStack item) {
        return Integer.parseInt(ItemUtil.getItemTags(item).get(ChatColor.RED + "Radius"));
    }

    private int getMaxRadius(ItemStack item) {
        return Integer.parseInt(ItemUtil.getItemTags(item).get(ChatColor.RED + "Max Radius"));
    }

    private void handleRightClick(Player player, ItemStack item) {
        final int radius = getRadius(item);
        final int maxRadius = getMaxRadius(item);
        final short dur = item.getDurability();
        Map<Enchantment, Integer> enchants = item.getItemMeta().getEnchants();

        CustomItem cItem = CustomItemCenter.get(itemType);
        for (Tag tag : cItem.getTags()) {
            if (tag.getKey().equals("Radius")) {
                int newRadius = radius + 1;
                if (newRadius > maxRadius) {
                    newRadius = 0;
                }
                tag.setProp(String.valueOf(newRadius));
                ChatUtil.sendNotice(player, "Radius set to: " + newRadius);
            }
        }
        ItemStack result = cItem.build();
        result.setDurability(dur);
        ItemMeta meta = result.getItemMeta();
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }
        result.setItemMeta(meta);
        player.setItemInHand(result);
    }

    private void handleLeftClick(Player player, ItemStack item, PlayerInteractEvent event) {

        Block clicked = event.getClickedBlock();
        final World world = clicked.getWorld();
        final int initialType = clicked.getTypeId();
        final int initialData = clicked.getData();

        if (!accepts(initialType, initialData)) return;

        event.setCancelled(true);

        final int radius = getRadius(item);
        Vector min = new Vector(clicked.getX(), clicked.getY(), clicked.getZ());
        Vector max = new Vector(clicked.getX(), clicked.getY(), clicked.getZ());
        switch (event.getBlockFace()) {
            case NORTH:
            case SOUTH:
                min = min.add(new Vector(
                        BlockFace.EAST.getModX(),
                        BlockFace.DOWN.getModY(),
                        BlockFace.EAST.getModZ()
                ).multiply(radius));
                max = max.add(new Vector(
                        BlockFace.WEST.getModX(),
                        BlockFace.UP.getModY(),
                        BlockFace.WEST.getModZ()
                ).multiply(radius));
                break;
            case EAST:
            case WEST:
                min = min.add(new Vector(
                        BlockFace.NORTH.getModX(),
                        BlockFace.DOWN.getModY(),
                        BlockFace.NORTH.getModZ()
                ).multiply(radius));
                max = max.add(new Vector(
                        BlockFace.SOUTH.getModX(),
                        BlockFace.UP.getModY(),
                        BlockFace.SOUTH.getModZ()
                ).multiply(radius));
                break;
            case UP:
            case DOWN:
                min = min.add(new Vector(
                        BlockFace.NORTH_EAST.getModX(),
                        0,
                        BlockFace.NORTH_EAST.getModZ()
                ).multiply(radius));
                max = max.add(new Vector(
                        BlockFace.SOUTH_WEST.getModX(),
                        0,
                        BlockFace.SOUTH_WEST.getModZ()
                ).multiply(radius));
                break;
            default:
                return;
        }

        CuboidRegion region = new CuboidRegion(min, max);
        min = region.getMinimumPoint();
        max = region.getMaximumPoint();

        callEvent(new RapidBlockBreakEvent(player));
        short breaks = 0;
        short curDur = item.getDurability();
        short maxDur = item.getType().getMaxDurability();
        blockBreaker:
        {
            for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
                    for (int y = min.getBlockY(); y <= max.getBlockY(); ++y) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getTypeId() != initialType || block.getData() != initialData) continue;

                        if (curDur + breaks > maxDur) {
                            break blockBreaker;
                        }

                        if (breakBlock(block, player, item)) {
                            ++breaks;
                        }
                    }
                }
            }
        }

        if (curDur + breaks >= maxDur) {
            player.setItemInHand(null);
        } else {
            item.setDurability((short) (curDur + breaks));
        }
    }

    private boolean breakBlock(Block b, Player p, ItemStack i) {
        BlockBreakEvent event = new BlockBreakEvent(b, p);
        callEvent(event);
        return !event.isCancelled() && b.breakNaturally(i);
    }
}
